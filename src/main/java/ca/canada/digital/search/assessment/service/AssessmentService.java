package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.api.AssessmentResponse;
import ca.canada.digital.search.assessment.api.CreateAssessmentRequest;
import ca.canada.digital.search.assessment.config.AssessmentConfiguration;
import ca.canada.digital.search.assessment.dao.AssessmentDao;
import ca.canada.digital.search.assessment.dao.TermListDao;
import ca.canada.digital.search.assessment.model.Assessment;
import ca.canada.digital.search.assessment.model.TermAssessment;
import ca.canada.digital.search.assessment.model.TermList;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.object.Language;
import ca.canada.digital.search.assessment.object.MetadataHighlight;
import ca.canada.digital.search.assessment.process.LanguageProcess;
import ca.canada.digital.search.assessment.util.DateUtil;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AssessmentService {
    private final AssessmentDao assessmentDao;
    private final TermListDao termListDao;
    private final AssessmentConfiguration config;

    public AssessmentService(AssessmentDao assessmentDao, TermListDao termListDao, AssessmentConfiguration config) {
        this.assessmentDao = assessmentDao;
        this.termListDao = termListDao;
        this.config = config;
    }

    /**
     * Anyone can fetch an assessment (with its termAssessments eagerly initialized).
     */
    public Assessment get(Integer id) {
        Assessment a = assessmentDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Assessment not found: " + id));

        // Ensure the collection is initialized for serialization
        Hibernate.initialize(a.getTermAssessments());
        // If you serialize Metadata, also initialize:
        a.getTermAssessments().forEach(ta -> {
            if (ta.getMetadata() != null) Hibernate.initialize(ta.getMetadata());
        });
        return a;
    }

    /**
     * Anyone can list assessments for a list.
     */
    public List<Assessment> listByListId(Integer listId) {
        // Optional: verify list exists to return 404 instead of empty by mistake
        termListDao.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found: " + listId));
        return assessmentDao.findByListId(listId);
    }

    /**
     * Create an assessment for a given list.
     * Any member of the list's department is allowed.
     */
    public Assessment create(UserEntity requester, CreateAssessmentRequest req, List<TermAssessment> terms) {
        TermList list = termListDao.findById(req.getListId())
                .orElseThrow(() -> new NotFoundException("List not found: " + req.getListId()));

        enforceDepartmentMembership(requester, list);

        Assessment a = new Assessment();
        a.setTermList(list);
        a.setName(req.getName());
        a.setDate(req.getDate() != null ? req.getDate() : LocalDateTime.now());

        for (TermAssessment ta : terms) {
            ta.setId(null);
            ta.setAssessment(a);             // owning side

            if (ta.getMetadata() != null) {
                ta.getMetadata().setId(null);
                ta.getMetadata().setTermAssessment(ta);
            }
            a.getTermAssessments().add(ta);  // inverse side
        }

        return assessmentDao.save(a);
    }

    /**
     * Delete an assessment. Must be a member of the list's department.
     */
    public void delete(UserEntity requester, Integer assessmentId) {
        Assessment a = assessmentDao.findById(assessmentId)
                .orElseThrow(() -> new NotFoundException("Assessment not found: " + assessmentId));

        enforceDepartmentMembership(requester, a.getTermList());
        assessmentDao.delete(a);
    }

    public AssessmentResponse getAssessmentResponse(Integer id) {
        Assessment assessment = get(id);
        boolean hasSpecificSearch = assessment.getTermAssessments().stream()
                .anyMatch(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC);
        boolean hasGoogleSearch = assessment.getTermAssessments().stream()
                .anyMatch(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE);
        Language lang = Language.FRENCH.getCode().equalsIgnoreCase(assessment.getTermList().getLanguage().getCode()) ? Language.FRENCH : Language.ENGLISH;
        long count = assessment.getTermAssessments().stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL)
                .count();
        long internalPasses = assessment.getTermAssessments().stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL && term.getPass())
                .count();
        long internalSpecificPasses = assessment.getTermAssessments().stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC && term.getPass())
                .count();
        long googlePasses = assessment.getTermAssessments().stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE && term.getPass())
                .count();
        List<TermAssessment> internalTerms = assessment.getTermAssessments().stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL)
                .sorted(Comparator.comparingInt(TermAssessment::getSequence))
                .collect(Collectors.toList());


        AssessmentResponse assessmentResponse = new AssessmentResponse();
        assessmentResponse.setList(assessment.getTermList());
        assessmentResponse.setName(assessment.getName());
        assessmentResponse.setDate(DateUtil.dateToString(assessment.getDate()));
        assessmentResponse.setHasSpecificSearch(hasSpecificSearch);
        assessmentResponse.setInternalUrl(
                "fr".equalsIgnoreCase(lang.getCode()) ? config.getSearchPage().getGlobalFr() : config.getSearchPage().getGlobalEn());
        assessmentResponse.setInternalScore((internalPasses > 0) ? (String.format("%.01f", (internalPasses / count) * 100) + "%") : "0%");
        assessmentResponse.setInternalTerms(internalTerms);
        if (hasSpecificSearch) {
            assessmentResponse.setInternalSpecificScore(internalSpecificPasses > 0 ? String.format("%.01f", (internalSpecificPasses / count) * 100) + "%" : "0%");
            assessmentResponse.setInternalSpecificTerms(assessment.getTermAssessments().stream()
                    .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC)
                    .sorted(Comparator.comparingInt(TermAssessment::getSequence))
                    .collect(Collectors.toList()));
        }
        if (hasGoogleSearch) {
            assessmentResponse.setGoogleUrl(config.getSearchPage().getGoogle());
            assessmentResponse.setGoogleScore(googlePasses > 0 ? String.format("%.01f", (googlePasses / count) * 100) + "%" : "0%");
            assessmentResponse.setGoogleTerms(assessment.getTermAssessments().stream()
                    .filter(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE)
                    .sorted(Comparator.comparingInt(TermAssessment::getSequence))
                    .collect(Collectors.toList()));
        }
        LanguageProcess lp;
        List<MetadataHighlight> metadataHighlights = new ArrayList<>();
        for (TermAssessment term : internalTerms) {
            lp = new LanguageProcess(term.getTerm(), lang);
            metadataHighlights.add(lp.getHighlightedMetadata(term.getMetadata()));
        }
        assessmentResponse.setHighlightedMetadata(metadataHighlights);

        return assessmentResponse;

    }

    private void enforceDepartmentMembership(UserEntity requester, TermList list) {
        Integer requesterDeptId = requester.getDepartment() != null ? requester.getDepartment().getId() : null;
        Integer listDeptId = (list.getDepartment() != null) ? list.getDepartment().getId() : null;

        if (requesterDeptId == null || listDeptId == null || !Objects.equals(requesterDeptId, listDeptId)) {
            throw new ForbiddenException("Only the list's department members can modify its assessments.");
        }
    }
}

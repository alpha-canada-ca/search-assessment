package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.api.UrlAssessmentResponse;
import ca.canada.digital.search.assessment.dao.LanguageDao;
import ca.canada.digital.search.assessment.dao.TermAssessmentDao;
import ca.canada.digital.search.assessment.model.Department;
import ca.canada.digital.search.assessment.model.Language;
import ca.canada.digital.search.assessment.model.TermAssessment;
import ca.canada.digital.search.assessment.object.MetadataHighlight;
import ca.canada.digital.search.assessment.process.LanguageProcess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TermAssessmentService {

    private final TermAssessmentDao termAssessmentDao;
    private final LanguageDao languageDao;

    public TermAssessmentService(TermAssessmentDao termAssessmentDao, LanguageDao languageDao) {
        this.termAssessmentDao = termAssessmentDao;
        this.languageDao = languageDao;
    }

    public List<TermAssessment> getTermAssessmentsByUrl(String targetUrl, Integer deptId, Integer langId) {
        return termAssessmentDao.findByTargetUrl(targetUrl, deptId, langId);
    }

    public UrlAssessmentResponse getUrlAssessmentResponse(String url, Integer deptId, Integer langId) {
        List<TermAssessment> termAssessments = getTermAssessmentsByUrl(url, deptId, langId);

        long internalCount = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL)
                .count();
        long internalSpecificCount = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC)
                .count();
        long googleCount = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE)
                .count();

        float internalPasses = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL && term.getPass())
                .count();
        float internalSpecificPasses = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC && term.getPass())
                .count();
        float googlePasses = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE && term.getPass())
                .count();

        List<TermAssessment> internalTerms = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL)
                .sorted(
                        Comparator.comparing(
                                TermAssessment::getSequence,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ).thenComparing(TermAssessment::getId)
                )
                .collect(Collectors.toList());


        UrlAssessmentResponse urlAssessmentResponse = new UrlAssessmentResponse();
        urlAssessmentResponse.setUrl(url);
        if (internalCount > 0) {
            urlAssessmentResponse.setInternalPasses((int) internalPasses);
            urlAssessmentResponse.setInternalScore(internalPasses > 0 ? String.format("%.2f%%", (float) (internalPasses / internalCount) * 100) : "0%");
            urlAssessmentResponse.setInternalTerms(internalTerms);

            LanguageProcess lp;
            Optional<Language> language = languageDao.findById(langId);
            List<MetadataHighlight> metadataHighlights = new ArrayList<>();
            for (TermAssessment term : internalTerms) {
                lp = new LanguageProcess(term.getTerm(), language.get());
                metadataHighlights.add(lp.getHighlightedMetadata(term.getMetadata()));
            }
            urlAssessmentResponse.setHighlightedMetadata(metadataHighlights);
        }
        if (internalSpecificCount > 0) {
            urlAssessmentResponse.setInternalSpecificPasses((int) internalSpecificPasses);
            urlAssessmentResponse.setInternalSpecificScore(internalSpecificPasses > 0 ? String.format("%.2f%%", (float) (internalSpecificPasses / internalSpecificCount) * 100) : "0%");
            urlAssessmentResponse.setInternalSpecificTerms(termAssessments.stream()
                    .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC)
                    .sorted(
                            Comparator.comparing(
                                    TermAssessment::getSequence,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            ).thenComparing(TermAssessment::getId)
                    )
                    .collect(Collectors.toList()));
        }
        if (googleCount > 0) {
            urlAssessmentResponse.setGooglePasses((int) googlePasses);
            urlAssessmentResponse.setGoogleScore(googlePasses > 0 ? String.format("%.2f%%", (float) (googlePasses / googleCount) * 100) : "0%");
            urlAssessmentResponse.setGoogleTerms(termAssessments.stream()
                    .filter(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE)
                    .sorted(
                            Comparator.comparing(
                                    TermAssessment::getSequence,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            ).thenComparing(TermAssessment::getId)
                    )
                    .collect(Collectors.toList()));
        }

        return urlAssessmentResponse;

    }

}

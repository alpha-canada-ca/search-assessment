package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.api.UrlAssessmentResponse;
import ca.canada.digital.search.assessment.dao.TermAssessmentDao;
import ca.canada.digital.search.assessment.model.TermAssessment;
import ca.canada.digital.search.assessment.object.Language;
import ca.canada.digital.search.assessment.object.MetadataHighlight;
import ca.canada.digital.search.assessment.process.LanguageProcess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TermAssessmentService {

    private TermAssessmentDao termAssessmentDao;

    public TermAssessmentService(TermAssessmentDao termAssessmentDao) {
        this.termAssessmentDao = termAssessmentDao;
    }

    public List<TermAssessment> getTermAssessmentsByUrl(String targetUrl) {
        return termAssessmentDao.findByTargetUrl(targetUrl);
    }

    public UrlAssessmentResponse getUrlAssessmentResponse(String url, Language lang) {
        List<TermAssessment> termAssessments = getTermAssessmentsByUrl(url);

        long internalCount = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL)
                .count();
        long internalSpecificCount = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC)
                .count();
        long googleCount = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE)
                .count();

        long internalPasses = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL && term.getPass())
                .count();
        long internalSpecificPasses = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC && term.getPass())
                .count();
        long googlePasses = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE && term.getPass())
                .count();

        List<TermAssessment> internalTerms = termAssessments.stream()
                .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL)
                .sorted(Comparator.comparingInt(TermAssessment::getSequence))
                .collect(Collectors.toList());


        UrlAssessmentResponse urlAssessmentResponse = new UrlAssessmentResponse();
        urlAssessmentResponse.setUrl(url);
        if (internalCount > 0) {
            urlAssessmentResponse.setInternalScore(internalPasses > 0 ? String.format("%.01f", (internalPasses / internalCount) * 100) + "%" : "0%");
            urlAssessmentResponse.setInternalTerms(internalTerms);

            LanguageProcess lp;
            List<MetadataHighlight> metadataHighlights = new ArrayList<>();
            for (TermAssessment term : internalTerms) {
                lp = new LanguageProcess(term.getTerm(), lang);
                metadataHighlights.add(lp.getHighlightedMetadata(term.getMetadata()));
            }
            urlAssessmentResponse.setHighlightedMetadata(metadataHighlights);
        }
        if (internalSpecificCount > 0) {
            urlAssessmentResponse.setInternalSpecificScore(internalSpecificPasses > 0 ? String.format("%.01f", (internalSpecificPasses / internalSpecificCount) * 100) + "%" : "0%");
            urlAssessmentResponse.setInternalSpecificTerms(termAssessments.stream()
                    .filter(term -> term.getSearchType() == TermAssessment.SearchType.INTERNAL_SPECIFIC)
                    .sorted(Comparator.comparingInt(TermAssessment::getSequence))
                    .collect(Collectors.toList()));
        }
        if (googleCount > 0) {
            urlAssessmentResponse.setGoogleScore(googlePasses > 0 ? String.format("%.01f", (googlePasses / googleCount) * 100) + "%" : "0%");
            urlAssessmentResponse.setGoogleTerms(termAssessments.stream()
                    .filter(term -> term.getSearchType() == TermAssessment.SearchType.GOOGLE)
                    .sorted(Comparator.comparingInt(TermAssessment::getSequence))
                    .collect(Collectors.toList()));
        }

        return urlAssessmentResponse;

    }

}

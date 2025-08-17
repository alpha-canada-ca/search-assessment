package ca.canada.digital.search.assessment.api;

import ca.canada.digital.search.assessment.model.TermAssessment;
import ca.canada.digital.search.assessment.object.MetadataHighlight;

import java.util.List;

public class UrlAssessmentResponse {
    String url;
    String internalSpecificScore;
    String internalScore;
    String googleScore;
    int internalSpecificPasses;
    int internalPasses;
    int googlePasses;
    List<TermAssessment> internalSpecificTerms;
    List<TermAssessment> internalTerms;
    List<TermAssessment> googleTerms;
    List<MetadataHighlight> highlightedMetadata;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInternalSpecificScore() {
        return internalSpecificScore;
    }

    public void setInternalSpecificScore(String internalSpecificScore) {
        this.internalSpecificScore = internalSpecificScore;
    }

    public String getInternalScore() {
        return internalScore;
    }

    public void setInternalScore(String internalScore) {
        this.internalScore = internalScore;
    }

    public String getGoogleScore() {
        return googleScore;
    }

    public void setGoogleScore(String googleScore) {
        this.googleScore = googleScore;
    }

    public int getInternalSpecificPasses() {
        return internalSpecificPasses;
    }

    public void setInternalSpecificPasses(int internalSpecificPasses) {
        this.internalSpecificPasses = internalSpecificPasses;
    }

    public int getInternalPasses() {
        return internalPasses;
    }

    public void setInternalPasses(int internalPasses) {
        this.internalPasses = internalPasses;
    }

    public int getGooglePasses() {
        return googlePasses;
    }

    public void setGooglePasses(int googlePasses) {
        this.googlePasses = googlePasses;
    }

    public List<TermAssessment> getInternalSpecificTerms() {
        return internalSpecificTerms;
    }

    public void setInternalSpecificTerms(List<TermAssessment> internalSpecificTerms) {
        this.internalSpecificTerms = internalSpecificTerms;
    }

    public List<TermAssessment> getInternalTerms() {
        return internalTerms;
    }

    public void setInternalTerms(List<TermAssessment> internalTerms) {
        this.internalTerms = internalTerms;
    }

    public List<TermAssessment> getGoogleTerms() {
        return googleTerms;
    }

    public void setGoogleTerms(List<TermAssessment> googleTerms) {
        this.googleTerms = googleTerms;
    }

    public List<MetadataHighlight> getHighlightedMetadata() {
        return highlightedMetadata;
    }

    public void setHighlightedMetadata(List<MetadataHighlight> highlightedMetadata) {
        this.highlightedMetadata = highlightedMetadata;
    }
}

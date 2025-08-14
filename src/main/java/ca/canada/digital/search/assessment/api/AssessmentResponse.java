package ca.canada.digital.search.assessment.api;

import ca.canada.digital.search.assessment.model.TermAssessment;
import ca.canada.digital.search.assessment.model.TermList;
import ca.canada.digital.search.assessment.object.Highlight;
import ca.canada.digital.search.assessment.object.MetadataHighlight;

import java.util.List;
import java.util.Map;

public class AssessmentResponse {
    TermList list;
    String name;
    String date;
    boolean hasSpecificSearch;
    String internalSpecificScore;
    String internalScore;
    String internalUrl;
    String googleScore;
    String googleUrl;
    List<TermAssessment> internalSpecificTerms;
    List<TermAssessment> internalTerms;
    List<TermAssessment> googleTerms;
    List<MetadataHighlight> highlightedMetadata;

    public TermList getList() {
        return list;
    }

    public void setList(TermList list) {
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isHasSpecificSearch() {
        return hasSpecificSearch;
    }

    public void setHasSpecificSearch(boolean hasSpecificSearch) {
        this.hasSpecificSearch = hasSpecificSearch;
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

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public String getGoogleScore() {
        return googleScore;
    }

    public void setGoogleScore(String googleScore) {
        this.googleScore = googleScore;
    }

    public String getGoogleUrl() {
        return googleUrl;
    }

    public void setGoogleUrl(String googleUrl) {
        this.googleUrl = googleUrl;
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

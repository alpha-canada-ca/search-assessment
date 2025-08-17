package ca.canada.digital.search.assessment.api;

import ca.canada.digital.search.assessment.model.TermAssessment;
import ca.canada.digital.search.assessment.model.TermList;
import ca.canada.digital.search.assessment.object.MetadataHighlight;

import java.util.List;

public class AssessmentResponse {
    Integer id;
    TermList list;
    String name;
    String date;
    boolean hasSpecificSearch;
    String internalSpecificScore;
    String internalScore;
    String googleScore;
    int internalSpecificPasses;
    int internalPasses;
    int googlePasses;
    String internalSpecificUrl;
    String internalUrl;
    String googleUrl;
    List<TermAssessment> internalSpecificTerms;
    List<TermAssessment> internalTerms;
    List<TermAssessment> googleTerms;
    List<MetadataHighlight> highlightedMetadata;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getInternalSpecificUrl() {
        return internalSpecificUrl;
    }

    public void setInternalSpecificUrl(String internalSpecificUrl) {
        this.internalSpecificUrl = internalSpecificUrl;
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
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

package ca.canada.digital.search.assessment.object;

import ca.canada.digital.search.assessment.model.Term;
import ca.canada.digital.search.assessment.model.TermAssessment;

import java.util.List;

public class SearchResult {
    private Term term;
    private TermAssessment.SearchType searchType;
    private List<String> returnedUrls;

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public TermAssessment.SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(TermAssessment.SearchType searchType) {
        this.searchType = searchType;
    }

    public List<String> getReturnedUrls() {
        return returnedUrls;
    }

    public void setReturnedUrls(List<String> returnedUrls) {
        this.returnedUrls = returnedUrls;
    }
}

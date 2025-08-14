package ca.canada.digital.search.assessment.object;

import ca.canada.digital.search.assessment.model.Term;

import java.util.List;

public class SearchResult {
    private Term term;
    private List<String> returnedUrls;


    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public List<String> getReturnedUrls() {
        return returnedUrls;
    }

    public void setReturnedUrls(List<String> returnedUrls) {
        this.returnedUrls = returnedUrls;
    }

}

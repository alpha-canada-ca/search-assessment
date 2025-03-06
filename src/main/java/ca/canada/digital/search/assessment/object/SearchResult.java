package ca.canada.digital.search.assessment.object;

import java.util.List;

public class SearchResult {
	private SearchTerm searchTerm;
	private List<String> returnedUrls;
	
	
	public SearchTerm getSearchTerm() {
		return searchTerm;
	}
	public void setSearchTerm(SearchTerm searchTerm) {
		this.searchTerm = searchTerm;
	}
	public List<String> getReturnedUrls() {
		return returnedUrls;
	}
	public void setReturnedUrls(List<String> returnedUrls) {
		this.returnedUrls = returnedUrls;
	}
	
}

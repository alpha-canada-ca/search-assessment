package ca.canada.digital.search.assessment.object;

public class TermEvaluation {
	
	private boolean pass;
	private int passUrlPosition;
	private Metadata targetUrlMetadata;
	private SearchTerm searchTerm;
	private SearchType searchType;
		
	public boolean isPass() {
		return pass;
	}

	public void setPass(boolean pass) {
		this.pass = pass;
	}

	public int getPassUrlPosition() {
		return passUrlPosition;
	}

	public void setPassUrlPosition(int passUrlPosition) {
		this.passUrlPosition = passUrlPosition;
	}

	public Metadata getTargetUrlMetadata() {
		return targetUrlMetadata;
	}

	public void setTargetUrlMetadata(Metadata targetUrlMetadata) {
		this.targetUrlMetadata = targetUrlMetadata;
	}

	public SearchTerm getSearchTerm() {
		return searchTerm;
	}
	
	public void setSearchTerm(SearchTerm searchTerm) {
		this.searchTerm = searchTerm;
	}

	public SearchType getSearchType() {
		return searchType;
	}

	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}

}
package ca.canada.digital.search.assessment.object;

public enum SearchType {
	GLOBAL("Global"),
	CONTEXTUAL("Contextual"),
	GOOGLE("Google");
	
	private String name;
	
	private SearchType(String name) {
		this.name = name;
	}
		
	@Override
	public String toString() {
		return this.name;
	}

}

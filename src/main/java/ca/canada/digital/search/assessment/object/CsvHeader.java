package ca.canada.digital.search.assessment.object;

public enum CsvHeader {
	ORDER("Order"),
	SEARCH_TERMS("Search terms"),
	RESULT("Result"),
	TARGET_URL("Target URL"),
	PAGE_TITLE("Page title"),
	DESCRIPTION("Description"),
	KEYWORDS("Keywords"),
	LAST_UPDATE("Last update"),
	POSITION("Position"),
	TYPE("Search Type");
	
	
	
	private String header;
	
	private CsvHeader(String header) {
		this.header = header;
	}
	
	public String getHeader() {
		return header;
	}
	
	@Override
	public String toString() {
		return header;
	}
}

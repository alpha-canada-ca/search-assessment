package ca.canada.digital.search.assessment.object;

public enum Language {
	ENGLISH("en"),
	FRENCH("fr");
	
	private String code;
	
	private Language(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}

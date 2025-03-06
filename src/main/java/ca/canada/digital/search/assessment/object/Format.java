package ca.canada.digital.search.assessment.object;

public enum Format {
	JSON("json"),
	CSV("csv");
	
	private String format;
	
	private Format(String format) {
		this.format = format;
	}
	
	public String getFormat() {
		return format;
	}
}

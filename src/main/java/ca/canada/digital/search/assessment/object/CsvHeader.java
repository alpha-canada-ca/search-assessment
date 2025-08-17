package ca.canada.digital.search.assessment.object;

public enum CsvHeader {
    SEQUENCE("Sequence"),
    SEARCH_TERMS("Search terms"),
    RESULT("Result"),
    TARGET_URL("Target URL"),
    PAGE_TITLE("Page title"),
    DESCRIPTION("Description"),
    H1("H1"),
    LAST_UPDATE("Last update"),
    POSITION("Position"),
    TYPE("Search Type");


    private final String header;

    CsvHeader(String header) {
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

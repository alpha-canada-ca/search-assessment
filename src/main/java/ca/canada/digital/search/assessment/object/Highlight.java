package ca.canada.digital.search.assessment.object;

import java.util.Map;

public class Highlight {
    private String text;
    private Map<String, Object> matches;
    private String highlightedText;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getMatches() {
        return matches;
    }

    public void setMatches(Map<String, Object> matches) {
        this.matches = matches;
    }

    public String getHighlightedText() {
        return highlightedText;
    }

    public void setHighlightedText(String highlightedText) {
        this.highlightedText = highlightedText;
    }
}

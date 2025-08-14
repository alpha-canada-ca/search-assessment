package ca.canada.digital.search.assessment.object;

public class MetadataHighlight {
    private Highlight title;
    private Highlight description;
    private Highlight h1;
    private Highlight lastUpdate;

    public Highlight getTitle() {
        return title;
    }

    public void setTitle(Highlight title) {
        this.title = title;
    }

    public Highlight getDescription() {
        return description;
    }

    public void setDescription(Highlight description) {
        this.description = description;
    }

    public Highlight getH1() {
        return h1;
    }

    public void setH1(Highlight h1) {
        this.h1 = h1;
    }

    public Highlight getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Highlight lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}

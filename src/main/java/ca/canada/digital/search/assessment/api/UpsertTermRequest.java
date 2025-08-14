package ca.canada.digital.search.assessment.api;

import ca.canada.digital.search.assessment.model.TargetUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UpsertTermRequest {
    private Integer id;               // null ⇒ new term; non-null ⇒ update existing
    @NotBlank
    private String term;
    @NotNull
    private Integer position;
    @NotNull
    private List<TargetUrl> targetUrls;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public List<TargetUrl> getTargetUrls() {
        return targetUrls;
    }

    public void setTargetUrls(List<TargetUrl> targetUrls) {
        this.targetUrls = targetUrls;
    }
}

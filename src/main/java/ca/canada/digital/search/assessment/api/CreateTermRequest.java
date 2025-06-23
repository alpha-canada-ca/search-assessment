package ca.canada.digital.search.assessment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateTermRequest {
    @NotBlank(message = "Term text is required")
    @JsonProperty
    private String term;

    @NotNull(message = "Index is required")
    @Min(value = 1, message = "Index must be at least 1")
    @JsonProperty
    private Integer index;

    @JsonProperty
    private List<@NotBlank(message = "Target URL cannot be blank") String> targetUrls;

    public CreateTermRequest() {
    }

    public String getTerm() {
        return term;
    }

    public Integer getIndex() {
        return index;
    }

    public List<String> getTargetUrls() {
        return targetUrls;
    }
}

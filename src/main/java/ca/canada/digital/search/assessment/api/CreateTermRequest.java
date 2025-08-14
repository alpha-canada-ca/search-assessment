package ca.canada.digital.search.assessment.api;

import ca.canada.digital.search.assessment.model.TargetUrl;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateTermRequest {
    @NotBlank(message = "Term text is required")
    @JsonProperty
    private String term;

    @NotNull(message = "Index is required")
    @Min(value = 1, message = "Index must be at least 1")
    @JsonProperty
    private Integer position;

    @NotEmpty(message = "You must supply at least one target URL")
    @Valid
    @JsonProperty("targetUrls")
    private List<String> targetUrls;

    public CreateTermRequest() {
    }

    public String getTerm() {
        return term;
    }

    public Integer getPosition() {
        return position;
    }

    public List<String> getTargetUrls() {
        return targetUrls;
    }
}

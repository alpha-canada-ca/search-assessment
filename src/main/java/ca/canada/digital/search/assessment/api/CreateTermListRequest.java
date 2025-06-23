package ca.canada.digital.search.assessment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTermListRequest {
    @NotBlank(message = "List name is required")
    @JsonProperty
    private String name;

    @NotNull(message = "Language is required")
    @JsonProperty
    private Integer languageId;

    public CreateTermListRequest() {
    }

    public String getName() {
        return name;
    }

    public Integer getLanguageId() {
        return languageId;
    }
}

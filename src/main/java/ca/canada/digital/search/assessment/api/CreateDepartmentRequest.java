package ca.canada.digital.search.assessment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateDepartmentRequest {

    @NotBlank(message = "English name is required")
    @Size(max = 70, message = "English name must be at most 70 characters")
    @JsonProperty
    private String nameEn;

    @NotBlank(message = "French name is required")
    @Size(max = 70, message = "French name must be at most 70 characters")
    @JsonProperty
    private String nameFr;

    @NotBlank(message = "English acronym is required")
    @Size(max = 10, message = "English acronym must be at most 10 characters")
    @JsonProperty
    private String acronymEn;

    @NotBlank(message = "French acronym is required")
    @Size(max = 10, message = "French acronym must be at most 10 characters")
    @JsonProperty
    private String acronymFr;

    @JsonProperty
    private String searchUrlEn;

    @JsonProperty
    private String searchUrlFr;

    public CreateDepartmentRequest() {
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameFr() {
        return nameFr;
    }

    public String getAcronymEn() {
        return acronymEn;
    }

    public String getAcronymFr() {
        return acronymFr;
    }

    public String getSearchUrlEn() {
        return searchUrlEn;
    }

    public String getSearchUrlFr() {
        return searchUrlFr;
    }

}

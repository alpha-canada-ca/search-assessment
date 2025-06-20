package ca.canada.digital.search.assessment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class CreateDepartmentRequest {

    @NotBlank(message = "English name is required")
    @JsonProperty
    private String nameEn;

    @NotBlank(message = "French name is required")
    @JsonProperty
    private String nameFr;

    @NotBlank(message = "English acronym is required")
    @JsonProperty
    private String acronymEn;

    @NotBlank(message = "French acronym is required")
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

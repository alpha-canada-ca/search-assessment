package ca.canada.digital.search.assessment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateAssessmentRequest {
    @NotNull @JsonProperty
    private Integer listId;

    @NotBlank @Size(max = 100) @JsonProperty
    private String name;

    @JsonProperty
    private LocalDateTime date; // optional; default now if null

    public Integer getListId() { return listId; }
    public String getName() { return name; }
    public LocalDateTime getDate() { return date; }
}
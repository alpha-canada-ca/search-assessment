package ca.canada.digital.search.assessment.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPayload {

    @NotBlank(message = "Token must be provided")
    @JsonProperty
    private String token;

    @NotBlank(message = "New password must not be blank")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @JsonProperty
    private String newPassword;

    // Jackson needs a no-arg constructor
    public ResetPayload() {
    }

    public String getToken() {
        return token;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
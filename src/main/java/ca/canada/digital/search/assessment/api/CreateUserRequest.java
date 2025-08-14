package ca.canada.digital.search.assessment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @JsonProperty
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @JsonProperty
    private String password;

    @NotBlank(message = "First name is required")
    @JsonProperty
    private String firstName;

    @NotBlank(message = "Last name is required")
    @JsonProperty
    private String lastName;

    @NotNull(message = "Department ID is required")
    @JsonProperty
    private Integer departmentId;

    @JsonProperty
    private Boolean admin;


    // Jackson needs a no-args constructor
    public CreateUserRequest() {
    }

    // Getters (no setters needed unless you want mutability)
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

}
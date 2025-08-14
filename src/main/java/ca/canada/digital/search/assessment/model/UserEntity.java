package ca.canada.digital.search.assessment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_entity",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserEntity implements Serializable, Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(name = "first_name", length = 35)
    private String firstName;

    @Column(name = "last_name", length = 35)
    private String lastName;

    @Column(name = "is_admin", nullable = false)
    private Boolean admin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GenericTerm> genericTerms = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TermList> termLists = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Term> terms = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean isAdmin() {
        return Boolean.TRUE.equals(admin);
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<GenericTerm> getGenericTerms() {
        return genericTerms;
    }

    public void setGenericTerms(List<GenericTerm> genericTerms) {
        this.genericTerms = genericTerms;
    }

    public List<TermList> getTermLists() {
        return termLists;
    }

    public void setTermLists(List<TermList> termLists) {
        this.termLists = termLists;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    // --- Password encryption ---

    /**
     * Set and hash the user's password using BCrypt.
     *
     * @param plainPassword the plaintext password to hash
     */
    public void setPassword(String plainPassword) {
        this.password = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Check a plaintext password against the stored hash.
     *
     * @param plainPassword the plaintext password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean checkPassword(String plainPassword) {
        if (this.password == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, this.password);
    }

    @Override
    public String getName() {
        return email;
    }
}

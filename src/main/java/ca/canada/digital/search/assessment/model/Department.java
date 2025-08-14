package ca.canada.digital.search.assessment.model;

import ca.canada.digital.search.assessment.AssessmentApplication;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "department",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "acronym_en"),
                @UniqueConstraint(columnNames = "acronym_fr")
        })
public class Department implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "name_en", nullable = false, length = 70)
    private String nameEn;

    @Column(name = "name_fr", nullable = false, length = 70)
    private String nameFr;

    @Column(name = "acronym_en", nullable = false, length = 10)
    private String acronymEn;

    @Column(name = "acronym_fr", nullable = false, length = 10)
    private String acronymFr;

    @Column(name = "search_url_en", length = 2083)
    private String searchUrlEn;

    @Column(name = "search_url_fr", length = 2083)
    private String searchUrlFr;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UserEntity> users = new ArrayList<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TermList> termLists = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameFr() {
        return nameFr;
    }

    public void setNameFr(String nameFr) {
        this.nameFr = nameFr;
    }

    public String getAcronymEn() {
        return acronymEn;
    }

    public void setAcronymEn(String acronymEn) {
        this.acronymEn = acronymEn;
    }

    public String getAcronymFr() {
        return acronymFr;
    }

    public void setAcronymFr(String acronymFr) {
        this.acronymFr = acronymFr;
    }

    public String getSearchUrlEn() {
        return searchUrlEn;
    }

    public void setSearchUrlEn(String searchUrlEn) {
        this.searchUrlEn = searchUrlEn;
    }

    public String getSearchUrlFr() {
        return searchUrlFr;
    }

    public void setSearchUrlFr(String searchUrlFr) {
        this.searchUrlFr = searchUrlFr;
    }

    public List<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }

    public List<TermList> getTermLists() {
        return termLists;
    }

    public void setTermLists(List<TermList> termLists) {
        this.termLists = termLists;
    }

    public String getSearchUrl(TermAssessment.SearchType type, Language lang) {
        if (type == TermAssessment.SearchType.GOOGLE) {
            return AssessmentApplication.getConfig().getSearchPage().getGoogle();
        } else if (type == TermAssessment.SearchType.INTERNAL_SPECIFIC && !StringUtils.isEmpty(getSearchUrlEn()) && !StringUtils.isEmpty(getSearchUrlFr())) {
            if ("fr".equalsIgnoreCase(lang.getCode())) {
                return getSearchUrlFr();
            } else {
                return getSearchUrlEn();
            }
        } else {
            if ("fr".equalsIgnoreCase(lang.getCode())) {
                return AssessmentApplication.getConfig().getSearchPage().getGlobalFr();
            } else {
                return AssessmentApplication.getConfig().getSearchPage().getGlobalEn();
            }
        }
    }
}

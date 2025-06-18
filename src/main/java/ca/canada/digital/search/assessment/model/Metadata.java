package ca.canada.digital.search.assessment.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "metadata")
public class Metadata implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(nullable = false, length = 2083)
    private String url;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    @Column(length = 3200)
    private String title;

    @Column(length = 3200)
    private String description;

    @Column(length = 3200)
    private String h1;

    @Column(columnDefinition = "TEXT")
    private String body;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_assessment_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TermAssessment termAssessment;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getH1() {
        return h1;
    }

    public void setH1(String h1) {
        this.h1 = h1;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public TermAssessment getTermAssessment() {
        return termAssessment;
    }

    public void setTermAssessment(TermAssessment termAssessment) {
        this.termAssessment = termAssessment;
    }
}
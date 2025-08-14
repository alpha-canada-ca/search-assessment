package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.TermAssessment;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TermAssessmentDao extends AbstractDAO<TermAssessment> {
    public TermAssessmentDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<TermAssessment> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public List<TermAssessment> findAll() {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TermAssessment> cq = cb.createQuery(TermAssessment.class);
        Root<TermAssessment> root = cq.from(TermAssessment.class);
        cq.select(root);
        return list(session.createQuery(cq));
    }

    /**
     * Exact match on target_url
     */
    public List<TermAssessment> findByTargetUrl(String url) {
        return list(
                currentSession().createQuery(
                        "from TermAssessment ta where ta.targetUrl = :url",
                        TermAssessment.class
                ).setParameter("url", url)
        );
    }

    /**
     * Exact match, for multiple URLs
     */
    public List<TermAssessment> findByTargetUrls(Collection<String> urls) {
        if (urls == null || urls.isEmpty()) return List.of();
        return list(
                currentSession().createQuery(
                        "from TermAssessment ta where ta.targetUrl in (:urls)",
                        TermAssessment.class
                ).setParameterList("urls", urls)
        );
    }

    /**
     * Substring/contains match on target_url (case-sensitive depending on DB collation)
     */
    public List<TermAssessment> findByTargetUrlContaining(String fragment) {
        if (fragment == null || fragment.isEmpty()) return List.of();

        // Escape LIKE wildcards
        String escaped = fragment
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return list(
                currentSession().createQuery(
                        "from TermAssessment ta " +
                                "where ta.targetUrl like :pattern escape '\\'",
                        TermAssessment.class
                ).setParameter("pattern", "%" + escaped + "%")
        );
    }

    public TermAssessment save(TermAssessment assessment) {
        return persist(assessment);
    }

    public void delete(TermAssessment assessment) {
        currentSession().delete(assessment);
    }
}

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
    public List<TermAssessment> findByTargetUrl(String url, Integer departmentId, Integer languageId) {
        return list(
                currentSession()
                        .createQuery(
                                "select ta " +
                                        "from TermAssessment ta " +
                                        "where ta.targetUrl = :url " +
                                        "  and ta.assessment.termList.department.id = :deptId " +
                                        "  and ta.assessment.termList.language.id = :langId",
                                TermAssessment.class
                        )
                        .setParameter("url", url)
                        .setParameter("deptId", departmentId)
                        .setParameter("langId", languageId)
        );
    }

    public TermAssessment save(TermAssessment assessment) {
        return persist(assessment);
    }

    public void delete(TermAssessment assessment) {
        currentSession().delete(assessment);
    }
}

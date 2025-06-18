package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.Assessment;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class AssessmentDao extends AbstractDAO<Assessment> {
    public AssessmentDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<Assessment> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public List<Assessment> findAll() {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Assessment> cq = cb.createQuery(Assessment.class);
        Root<Assessment> root = cq.from(Assessment.class);
        cq.select(root);
        return list(session.createQuery(cq));
    }

    public Assessment save(Assessment assessment) {
        return persist(assessment);
    }

    public void delete(Assessment assessment) {
        currentSession().delete(assessment);
    }
}
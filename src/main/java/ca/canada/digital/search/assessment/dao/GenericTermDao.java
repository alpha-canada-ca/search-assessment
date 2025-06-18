package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.GenericTerm;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class GenericTermDao extends AbstractDAO<GenericTerm> {
    public GenericTermDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<GenericTerm> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public List<GenericTerm> findAll() {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GenericTerm> cq = cb.createQuery(GenericTerm.class);
        Root<GenericTerm> root = cq.from(GenericTerm.class);
        cq.select(root);
        return list(session.createQuery(cq));
    }

    public GenericTerm save(GenericTerm term) {
        return persist(term);
    }

    public void delete(GenericTerm term) {
        currentSession().delete(term);
    }
}

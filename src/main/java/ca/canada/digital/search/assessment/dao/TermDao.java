package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.Term;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class TermDao extends AbstractDAO<Term> {
    public TermDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<Term> findById(Integer id) {
        return Optional.ofNullable(get(id));
    }

    public List<Term> findByTermList(Integer listId) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Term> cq = cb.createQuery(Term.class);
        Root<Term> root = cq.from(Term.class);
        cq.select(root)
                .where(cb.equal(root.get("termList").get("id"), listId));
        return list(session.createQuery(cq));
    }

    public List<Term> findAll() {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Term> cq = cb.createQuery(Term.class);
        Root<Term> root = cq.from(Term.class);
        cq.select(root);
        return list(session.createQuery(cq));
    }

    public Term save(Term term) {
        if (term.getId() != null) {
            return currentSession().merge(term);
        } else {
            return persist(term);
        }
    }

    public void delete(Term term) {
        currentSession().delete(term);
    }
}


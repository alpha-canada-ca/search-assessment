package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.TermList;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TermListDao extends AbstractDAO<TermList> {
    public TermListDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<TermList> findById(Integer id) {
        return Optional.ofNullable(get(id));
    }

    public List<TermList> findByDepartment(Integer departmentId, Integer languageIdOrNull) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TermList> cq = cb.createQuery(TermList.class);
        Root<TermList> root = cq.from(TermList.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("department").get("id"), departmentId));
        if (languageIdOrNull != null) {
            predicates.add(cb.equal(root.get("language").get("id"), languageIdOrNull));
        }

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));   // newest first

        return list(session.createQuery(cq));
    }

    public List<TermList> findByUser(Integer userId) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TermList> cq = cb.createQuery(TermList.class);
        Root<TermList> root = cq.from(TermList.class);
        cq.select(root)
                .where(cb.equal(root.get("user").get("id"), userId));
        return list(session.createQuery(cq));
    }


    public TermList save(TermList termList) {
        return persist(termList);
    }

    public void delete(TermList termList) {
        currentSession().delete(termList);
    }
}


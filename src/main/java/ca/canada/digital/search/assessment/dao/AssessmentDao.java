package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.Assessment;
import ca.canada.digital.search.assessment.model.TermList;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssessmentDao extends AbstractDAO<Assessment> {
    public AssessmentDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<Assessment> findById(Integer id) {
        Session s = currentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Assessment> cq = cb.createQuery(Assessment.class);
        Root<Assessment> root = cq.from(Assessment.class);
        // fetch details
        var taFetch = root.fetch("termAssessments", JoinType.LEFT);
        taFetch.fetch("metadata", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(root.get("id"), id))
                .distinct(true);

        return Optional.of(uniqueResult(s.createQuery(cq)));
    }

    public List<Assessment> findByListId(Integer listId) {
        return list(
                currentSession()
                        .createQuery(
                                "from Assessment a where a.termList.id = :listId order by a.date desc, a.id desc",
                                Assessment.class
                        )
                        .setParameter("listId", listId)
        );
    }

    public List<Assessment> findByDepartment(Integer deptId, Integer languageIdOrNull) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Assessment> cq = cb.createQuery(Assessment.class);
        Root<Assessment> root = cq.from(Assessment.class);

        // join to TermList, so we can filter by department (and language)
        Join<Assessment, TermList> listJoin = root.join("termList", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(listJoin.get("department").get("id"), deptId));

        if (languageIdOrNull != null) {
            predicates.add(cb.equal(listJoin.get("language").get("id"), languageIdOrNull));
        }

        cq.select(root)
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .orderBy(cb.desc(root.get("date")), cb.desc(root.get("id")));

        return list(session.createQuery(cq));
    }


    public Assessment save(Assessment assessment) {
        return persist(assessment);
    }

    public void delete(Assessment assessment) {
        currentSession().delete(assessment);
    }
}
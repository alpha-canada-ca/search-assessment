package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.Assessment;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class AssessmentDao extends AbstractDAO<Assessment> {
    public AssessmentDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<Assessment> findById(Integer id) {
        return Optional.ofNullable(get(id));
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

    public List<Assessment> findByDepartmentId(Integer deptId) {
        return list(
                currentSession()
                        .createQuery(
                                "from Assessment a where a.termList.department.id = :deptId order by a.date desc, a.id desc",
                                Assessment.class
                        )
                        .setParameter("deptId", deptId)
        );
    }


    public Assessment save(Assessment assessment) {
        return persist(assessment);
    }

    public void delete(Assessment assessment) {
        currentSession().delete(assessment);
    }
}
package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.Department;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class DepartmentDao extends AbstractDAO<Department> {
    public DepartmentDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<Department> findById(Integer id) {
        return Optional.ofNullable(get(id));
    }

    public List<Department> findAll(int offset, int limit) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        Root<Department> root = cq.from(Department.class);
        cq.select(root);

        return list(
                session.createQuery(cq).setFirstResult(offset)
                        .setMaxResults(limit)
        );
    }

    public Department save(Department department) {
        return persist(department);
    }

    public void delete(Department department) {
        currentSession().delete(department);
    }
}

package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.UserEntity;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class UserEntityDao extends AbstractDAO<UserEntity> {
    public UserEntityDao(SessionFactory factory) {
        super(factory);
    }

    public List<UserEntity> findAll(int offset, int limit) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = cq.from(UserEntity.class);
        cq.select(root);

        return list(
                session.createQuery(cq).setFirstResult(offset)
                        .setMaxResults(limit)
        );
    }

    public Optional<UserEntity> findById(Integer id) {
        return Optional.ofNullable(get(id));
    }

    public Optional<UserEntity> findByEmail(String email) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = cq.from(UserEntity.class);
        cq.select(root).where(cb.equal(root.get("email"), email));
        List<UserEntity> results = list(session.createQuery(cq));
        return results.stream().findFirst();
    }

    public List<UserEntity> findByDepartment(Integer departmentId) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = cq.from(UserEntity.class);
        cq.select(root)
                .where(cb.equal(root.get("department").get("id"), departmentId));
        return list(session.createQuery(cq));
    }

    public UserEntity save(UserEntity user) {
        return persist(user);
    }

    public void delete(UserEntity user) {
        currentSession().delete(user);
    }
}
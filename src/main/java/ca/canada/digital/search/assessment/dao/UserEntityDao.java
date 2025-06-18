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

    public Optional<UserEntity> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public List<UserEntity> findAll() {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = cq.from(UserEntity.class);
        cq.select(root);
        return list(session.createQuery(cq));
    }

    public UserEntity save(UserEntity user) {
        return persist(user);
    }

    public void delete(UserEntity user) {
        currentSession().delete(user);
    }
}
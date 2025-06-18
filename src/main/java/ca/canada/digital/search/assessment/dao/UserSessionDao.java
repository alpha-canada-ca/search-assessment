package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.UserSession;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Optional;

public class UserSessionDao extends AbstractDAO<UserSession> {
    public UserSessionDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<UserSession> findByToken(String token) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserSession> cq = cb.createQuery(UserSession.class);
        Root<UserSession> root = cq.from(UserSession.class);
        cq.select(root)
                .where(cb.equal(root.get("sessionToken"), token));
        return list(session.createQuery(cq))
                .stream()
                .findFirst();
    }

    public UserSession save(UserSession sessionEntity) {
        return persist(sessionEntity);
    }

    public void delete(UserSession sessionEntity) {
        currentSession().delete(sessionEntity);
    }

    /**
     * Clean up expired sessions in bulk (e.g. scheduled job)
     */
    public int deleteExpired() {
        String hql = "DELETE FROM UserSession s WHERE s.expiresAt <= :now";
        return currentSession()
                .createQuery(hql)
                .setParameter("now", java.time.LocalDateTime.now())
                .executeUpdate();
    }
}


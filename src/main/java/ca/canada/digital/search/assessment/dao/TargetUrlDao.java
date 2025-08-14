package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.TargetUrl;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class TargetUrlDao extends AbstractDAO<TargetUrl> {
    public TargetUrlDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<TargetUrl> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public List<TargetUrl> findAll() {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TargetUrl> cq = cb.createQuery(TargetUrl.class);
        Root<TargetUrl> root = cq.from(TargetUrl.class);
        cq.select(root);
        return list(session.createQuery(cq));
    }

    public TargetUrl save(TargetUrl url) {
        if (url.getId() != null) {
            return currentSession().merge(url);
        } else {
            return persist(url);
        }
    }

    public void delete(TargetUrl url) {
        currentSession().delete(url);
    }
}
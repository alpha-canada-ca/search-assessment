package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.Metadata;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class MetadataDao extends AbstractDAO<Metadata> {
    public MetadataDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<Metadata> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public List<Metadata> findAll() {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Metadata> cq = cb.createQuery(Metadata.class);
        Root<Metadata> root = cq.from(Metadata.class);
        cq.select(root);
        return list(session.createQuery(cq));
    }

    public Metadata save(Metadata metadata) {
        return persist(metadata);
    }

    public void delete(Metadata metadata) {
        currentSession().delete(metadata);
    }
}
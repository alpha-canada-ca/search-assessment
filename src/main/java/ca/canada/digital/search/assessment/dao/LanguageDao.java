package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.Language;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class LanguageDao extends AbstractDAO<Language> {
    public LanguageDao(SessionFactory factory) {
        super(factory);
    }

    public Optional<Language> findById(Integer id) {
        return Optional.ofNullable(get(id));
    }

    public List<Language> findAll() {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Language> cq = cb.createQuery(Language.class);
        Root<Language> root = cq.from(Language.class);
        cq.select(root);
        return list(session.createQuery(cq));
    }

    public Language save(Language language) {
        return persist(language);
    }

    public void delete(Language language) {
        currentSession().delete(language);
    }
}



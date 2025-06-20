package ca.canada.digital.search.assessment.dao;

import ca.canada.digital.search.assessment.model.PasswordResetToken;
import ca.canada.digital.search.assessment.model.UserEntity;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class PasswordResetTokenDao extends AbstractDAO<PasswordResetToken> {
    public PasswordResetTokenDao(SessionFactory factory) {
        super(factory);
    }

    public PasswordResetToken create(UserEntity user, Duration ttl) {
        PasswordResetToken t = new PasswordResetToken(
                user,
                LocalDateTime.now().plus(ttl)
        );
        return persist(t);
    }

    public Optional<PasswordResetToken> findValid(String token) {
        return list(
                currentSession().createNamedQuery("PasswordResetToken.findByToken", PasswordResetToken.class)
                        .setParameter("token", token)
        ).stream()
                .filter(t -> !t.getConsumed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .findFirst();
    }

    public void markConsumed(PasswordResetToken t) {
        t.setConsumed(true);
        persist(t);
    }
}
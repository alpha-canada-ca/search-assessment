package ca.canada.digital.search.assessment.auth;

import ca.canada.digital.search.assessment.dao.UserSessionDao;
import ca.canada.digital.search.assessment.model.UserEntity;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.hibernate.UnitOfWork;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class SessionTokenAuthenticator implements Authenticator<String, UserEntity> {
    private final UserSessionDao sessionDao;

    public SessionTokenAuthenticator(UserSessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Override
    @UnitOfWork
    public Optional<UserEntity> authenticate(String token) {
        return sessionDao.findByToken(token)
                .filter(s -> s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(s -> {
                    if (Duration.between(s.getLastAccessed(), LocalDateTime.now()).toMinutes() >= 5) { // avoid hitting the db more often
                        s.setLastAccessed(LocalDateTime.now());
                        sessionDao.save(s);
                    }
                    return s.getUser();
                });
    }

}


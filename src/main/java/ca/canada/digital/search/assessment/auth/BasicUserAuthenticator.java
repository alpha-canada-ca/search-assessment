package ca.canada.digital.search.assessment.auth;

import ca.canada.digital.search.assessment.dao.UserEntityDao;
import ca.canada.digital.search.assessment.model.UserEntity;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.hibernate.UnitOfWork;

import java.util.Optional;

public class BasicUserAuthenticator implements Authenticator<BasicCredentials, UserEntity> {
    private final UserEntityDao userDao;

    public BasicUserAuthenticator(UserEntityDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @UnitOfWork
    public Optional<UserEntity> authenticate(BasicCredentials credentials) {
        return userDao.findByEmail(credentials.getUsername())
                .filter(user -> user.checkPassword(credentials.getPassword()));
    }
}

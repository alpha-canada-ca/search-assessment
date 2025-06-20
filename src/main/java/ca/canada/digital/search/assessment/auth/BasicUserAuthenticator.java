package ca.canada.digital.search.assessment.auth;

import ca.canada.digital.search.assessment.dao.UserEntityDao;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.object.Credential;
import io.dropwizard.auth.Authenticator;

import java.util.Optional;

public class BasicUserAuthenticator implements Authenticator<Credential, UserEntity> {
    private final UserEntityDao userDao;

    public BasicUserAuthenticator(UserEntityDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<UserEntity> authenticate(Credential credentials) {
        return userDao.findByEmail(credentials.getUsername())
                .filter(user -> user.checkPassword(credentials.getPassword()));
    }
}

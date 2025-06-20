package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.dao.UserEntityDao;
import ca.canada.digital.search.assessment.dao.UserSessionDao;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.model.UserSession;
import jakarta.ws.rs.NotAuthorizedException;

import java.time.Duration;
import java.time.LocalDateTime;

public class AuthService {
    private final UserEntityDao userDao;
    private final UserSessionDao sessionDao;

    public AuthService(UserEntityDao userDao, UserSessionDao sessionDao) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
    }

    /**
     * Validates credentials and creates a new session token if successful.
     *
     * @param email         the user's email
     * @param plainPassword the user's plaintext password
     * @param ipAddress     the client IP address
     * @param userAgent     the client User-Agent header
     * @return the new session token
     * @throws NotAuthorizedException if credentials are invalid
     */
    public String login(String email,
                        String plainPassword,
                        String ipAddress,
                        String userAgent) {
        UserEntity user = userDao.findByEmail(email)
                .orElseThrow(() -> new NotAuthorizedException("Invalid credentials"));

        if (!user.checkPassword(plainPassword)) {
            throw new NotAuthorizedException("Invalid credentials");
        }

        // Create session that expires in 30 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(Duration.ofDays(30));

        UserSession session = new UserSession(user, expiresAt, ipAddress, userAgent);
        sessionDao.save(session);
        return session.getSessionToken();
    }

    /**
     * Invalidates a session token, logging the user out.
     *
     * @param token the session token to invalidate
     */
    public void logout(String token) {
        sessionDao.findByToken(token)
                .ifPresent(sessionDao::delete);
    }
}

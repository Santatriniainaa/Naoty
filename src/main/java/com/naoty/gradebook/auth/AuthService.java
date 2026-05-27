package com.naoty.gradebook.auth;

import com.naoty.gradebook.config.AppConfig;
import com.naoty.gradebook.model.Role;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Authentification "champ unique" :
 * <ul>
 *   <li>Si la saisie correspond au code admin (insensible à la casse), session ADMIN.</li>
 *   <li>Sinon, on cherche un User PROF avec cet id ; si trouvé, session PROF.</li>
 *   <li>Sinon, échec ({@link Optional#empty()}).</li>
 * </ul>
 */
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AppConfig config;
    private final UserRepository users;
    private final Session session;

    public AuthService(AppConfig config, UserRepository users, Session session) {
        this.config = config;
        this.users = users;
        this.session = session;
    }

    public Optional<Session> login(String input) {
        if (input == null) return Optional.empty();
        String code = input.trim();
        if (code.isEmpty()) return Optional.empty();

        if (code.equalsIgnoreCase(config.adminCode())) {
            session.setAdmin();
            log.info("Logged in as ADMIN");
            return Optional.of(session);
        }

        Optional<User> user = users.findById(code);
        if (user.isPresent() && user.get().role() == Role.PROF) {
            session.setProf(user.get());
            log.info("Logged in as PROF {}", user.get().id());
            return Optional.of(session);
        }

        log.warn("Login failed for input '{}'", code);
        return Optional.empty();
    }

    public void logout() {
        log.info("Logout (was {})", session.displayId());
        session.clear();
    }
}

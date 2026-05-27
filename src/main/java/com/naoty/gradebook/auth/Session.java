package com.naoty.gradebook.auth;

import com.naoty.gradebook.model.Role;
import com.naoty.gradebook.model.User;

import java.util.Optional;

/**
 * Session courante. Le rôle ADMIN n'est pas matérialisé par un User
 * (login = mot fixe), donc {@link #currentUser()} est vide pour un admin.
 */
public class Session {

    private Role role;
    private User currentUser;

    public Optional<User> currentUser()   { return Optional.ofNullable(currentUser); }
    public Optional<Role> role()           { return Optional.ofNullable(role); }
    public boolean isAdmin()               { return role == Role.ADMIN; }
    public boolean isProf()                { return role == Role.PROF; }
    public boolean isAuthenticated()       { return role != null; }

    public String displayName() {
        if (isAdmin()) return "Administrateur";
        return currentUser != null ? currentUser.fullName() : "—";
    }

    public String displayId() {
        if (isAdmin()) return "ADMIN";
        return currentUser != null ? currentUser.id() : "—";
    }

    void setAdmin() {
        this.role = Role.ADMIN;
        this.currentUser = null;
    }

    void setProf(User user) {
        this.role = Role.PROF;
        this.currentUser = user;
    }

    public void clear() {
        this.role = null;
        this.currentUser = null;
    }
}

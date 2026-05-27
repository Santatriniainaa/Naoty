package com.naoty.gradebook.service;

import com.naoty.gradebook.event.EventBus;
import com.naoty.gradebook.exception.DuplicateEntityException;
import com.naoty.gradebook.model.Role;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.repository.UserRepository;
import com.naoty.gradebook.validation.UserValidator;
import com.naoty.gradebook.validation.ValidationResult;

import java.util.List;
import java.util.Optional;

public class UserService {

    public static final String TOPIC = "users.changed";

    private final UserRepository repo;
    private final UserValidator validator;
    private final EventBus bus;

    public UserService(UserRepository repo, UserValidator validator, EventBus bus) {
        this.repo = repo;
        this.validator = validator;
        this.bus = bus;
    }

    public List<User> findAll()                    { return repo.findAll(); }
    public Optional<User> findById(String id)      { return repo.findById(id); }
    public List<User> findProfs()                  { return repo.findByRole(Role.PROF); }

    public ValidationResult create(User u) {
        ValidationResult vr = validator.validate(u);
        if (!vr.valid()) return vr;
        if (repo.findById(u.id()).isPresent()) {
            throw new DuplicateEntityException("user.duplicate:" + u.id());
        }
        repo.save(u);
        bus.publish(TOPIC, u);
        return ValidationResult.ok();
    }

    public ValidationResult update(User u) {
        ValidationResult vr = validator.validate(u);
        if (!vr.valid()) return vr;
        repo.save(u);
        bus.publish(TOPIC, u);
        return ValidationResult.ok();
    }

    public void delete(String id) {
        repo.deleteById(id);
        bus.publish(TOPIC, id);
    }
}

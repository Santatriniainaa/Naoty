package com.naoty.gradebook.service;

import com.naoty.gradebook.event.EventBus;
import com.naoty.gradebook.exception.DuplicateEntityException;
import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Status;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.repository.StudentRepository;
import com.naoty.gradebook.validation.StudentValidator;
import com.naoty.gradebook.validation.ValidationResult;

import java.util.List;
import java.util.Optional;

public class StudentService {

    public static final String TOPIC = "students.changed";

    private final StudentRepository repo;
    private final StudentValidator validator;
    private final EventBus bus;

    public StudentService(StudentRepository repo, StudentValidator validator, EventBus bus) {
        this.repo = repo;
        this.validator = validator;
        this.bus = bus;
    }

    public List<Student> findAll()                  { return repo.findAll(); }
    public Optional<Student> findById(String id)    { return repo.findById(id); }

    public List<Student> findBy(Niveau niveau, Formation formation, Status status, String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        return findAll().stream()
                .filter(s -> niveau == null || s.niveau() == niveau)
                .filter(s -> formation == null || s.formation() == formation)
                .filter(s -> status == null || s.status() == status)
                .filter(s -> q.isEmpty()
                        || s.nom().toLowerCase().contains(q)
                        || s.prenom().toLowerCase().contains(q)
                        || s.id().toLowerCase().contains(q))
                .toList();
    }

    public List<Student> findCohorte(Niveau niveau, Formation formation) {
        return findAll().stream()
                .filter(s -> s.niveau() == niveau && s.formation() == formation)
                .toList();
    }

    public ValidationResult create(Student s) {
        ValidationResult vr = validator.validate(s);
        if (!vr.valid()) return vr;
        if (repo.findById(s.id()).isPresent()) {
            throw new DuplicateEntityException("student.duplicate:" + s.id());
        }
        repo.save(s);
        bus.publish(TOPIC, s);
        return ValidationResult.ok();
    }

    public ValidationResult update(Student s) {
        ValidationResult vr = validator.validate(s);
        if (!vr.valid()) return vr;
        repo.save(s);
        bus.publish(TOPIC, s);
        return ValidationResult.ok();
    }

    public void delete(String id) {
        repo.deleteById(id);
        bus.publish(TOPIC, id);
    }
}

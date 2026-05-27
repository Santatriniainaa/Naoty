package com.naoty.gradebook.service;

import com.naoty.gradebook.event.EventBus;
import com.naoty.gradebook.exception.DuplicateEntityException;
import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Semestre;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.repository.SubjectRepository;
import com.naoty.gradebook.validation.SubjectValidator;
import com.naoty.gradebook.validation.ValidationResult;

import java.util.List;
import java.util.Optional;

public class SubjectService {

    public static final String TOPIC = "subjects.changed";

    private final SubjectRepository repo;
    private final SubjectValidator validator;
    private final EventBus bus;

    public SubjectService(SubjectRepository repo, SubjectValidator validator, EventBus bus) {
        this.repo = repo;
        this.validator = validator;
        this.bus = bus;
    }

    public List<Subject> findAll()                  { return repo.findAll(); }
    public Optional<Subject> findById(String id)    { return repo.findById(id); }

    public List<Subject> findByProf(String profId) {
        return findAll().stream().filter(s -> s.profId().equals(profId)).toList();
    }

    public List<Subject> findBy(Niveau niveau, Semestre semestre, Formation formation, String profId) {
        return findAll().stream()
                .filter(s -> niveau == null || s.niveau() == niveau)
                .filter(s -> semestre == null || s.semestre() == semestre)
                .filter(s -> formation == null || s.formation() == formation)
                .filter(s -> profId == null || s.profId().equals(profId))
                .toList();
    }

    /** Matières applicables à un étudiant pour un semestre donné. */
    public List<Subject> findForStudent(Student student, Semestre semestre) {
        return findAll().stream()
                .filter(s -> s.niveau() == student.niveau())
                .filter(s -> s.semestre() == semestre)
                .filter(s -> s.formation().matches(student.formation()))
                .toList();
    }

    public ValidationResult create(Subject s) {
        ValidationResult vr = validator.validate(s);
        if (!vr.valid()) return vr;
        if (repo.findById(s.id()).isPresent()) {
            throw new DuplicateEntityException("subject.duplicate:" + s.id());
        }
        repo.save(s);
        bus.publish(TOPIC, s);
        return ValidationResult.ok();
    }

    public ValidationResult update(Subject s) {
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

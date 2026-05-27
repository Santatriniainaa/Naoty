package com.naoty.gradebook.service;

import com.naoty.gradebook.event.EventBus;
import com.naoty.gradebook.model.Grade;
import com.naoty.gradebook.repository.GradeRepository;
import com.naoty.gradebook.validation.GradeValidator;
import com.naoty.gradebook.validation.ValidationResult;

import java.util.List;
import java.util.Optional;

public class GradeService {

    public static final String TOPIC = "grades.changed";

    private final GradeRepository repo;
    private final GradeValidator validator;
    private final EventBus bus;

    public GradeService(GradeRepository repo, GradeValidator validator, EventBus bus) {
        this.repo = repo;
        this.validator = validator;
        this.bus = bus;
    }

    public List<Grade> findAll()                            { return repo.findAll(); }
    public Optional<Grade> findById(String id)              { return repo.findById(id); }
    public List<Grade> findByStudentId(String studentId)    { return repo.findByStudentId(studentId); }
    public List<Grade> findBySubjectId(String subjectId)    { return repo.findBySubjectId(subjectId); }
    public List<Grade> findByProfId(String profId)          { return repo.findByProfId(profId); }

    public ValidationResult create(Grade g) {
        ValidationResult vr = validator.validate(g);
        if (!vr.valid()) return vr;
        repo.save(g);
        bus.publish(TOPIC, g);
        return ValidationResult.ok();
    }

    public ValidationResult update(Grade g) {
        ValidationResult vr = validator.validate(g);
        if (!vr.valid()) return vr;
        repo.save(g);
        bus.publish(TOPIC, g);
        return ValidationResult.ok();
    }

    public void delete(String id) {
        repo.deleteById(id);
        bus.publish(TOPIC, id);
    }
}

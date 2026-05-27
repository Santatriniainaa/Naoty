package com.naoty.gradebook.repository;

import com.naoty.gradebook.model.Grade;

import java.util.List;

public interface GradeRepository extends Repository<Grade, String> {
    List<Grade> findByStudentId(String studentId);
    List<Grade> findBySubjectId(String subjectId);
    List<Grade> findByProfId(String profId);
}

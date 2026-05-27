package com.naoty.gradebook.repository.csv;

import com.naoty.gradebook.model.Grade;
import com.naoty.gradebook.model.GradeType;
import com.naoty.gradebook.repository.GradeRepository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CsvGradeRepository extends AbstractCsvRepository<Grade, String>
        implements GradeRepository {

    public CsvGradeRepository(Path file, Path backupDir) {
        super(file, backupDir, new Mapper(), Grade::id);
    }

    public static CsvMapper<Grade> mapper() { return new Mapper(); }

    @Override public List<Grade> findAll()                  { return super.findAll(); }
    @Override public Optional<Grade> findById(String id)    { return super.findById(id); }
    @Override public Grade save(Grade e)                    { return super.save(e); }
    @Override public void deleteById(String id)             { super.deleteById(id); }

    @Override
    public List<Grade> findByStudentId(String studentId) {
        return findAll().stream().filter(g -> g.studentId().equals(studentId)).toList();
    }

    @Override
    public List<Grade> findBySubjectId(String subjectId) {
        return findAll().stream().filter(g -> g.subjectId().equals(subjectId)).toList();
    }

    @Override
    public List<Grade> findByProfId(String profId) {
        return findAll().stream().filter(g -> g.profId().equals(profId)).toList();
    }

    private static class Mapper implements CsvMapper<Grade> {
        @Override public String[] headers() {
            return new String[]{"id", "studentId", "subjectId", "profId", "type", "value", "date"};
        }
        @Override public String[] toRow(Grade g) {
            return new String[]{
                    g.id(), g.studentId(), g.subjectId(), g.profId(),
                    g.type().name(),
                    String.valueOf(g.value()),
                    g.date().toString()
            };
        }
        @Override public Grade fromRow(String[] r) {
            return new Grade(
                    r[0], r[1], r[2], r[3],
                    GradeType.valueOf(r[4]),
                    Double.parseDouble(r[5]),
                    LocalDate.parse(r[6])
            );
        }
    }
}

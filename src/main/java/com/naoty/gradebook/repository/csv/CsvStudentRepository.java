package com.naoty.gradebook.repository.csv;

import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Gender;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Status;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.repository.StudentRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class CsvStudentRepository extends AbstractCsvRepository<Student, String>
        implements StudentRepository {

    public CsvStudentRepository(Path file, Path backupDir) {
        super(file, backupDir, new Mapper(), Student::id);
    }

    public static CsvMapper<Student> mapper() { return new Mapper(); }

    @Override public List<Student> findAll()                  { return super.findAll(); }
    @Override public Optional<Student> findById(String id)    { return super.findById(id); }
    @Override public Student save(Student e)                  { return super.save(e); }
    @Override public void deleteById(String id)               { super.deleteById(id); }

    private static class Mapper implements CsvMapper<Student> {
        @Override public String[] headers() {
            return new String[]{"id", "nom", "prenom", "sexe", "niveau", "formation", "status"};
        }
        @Override public String[] toRow(Student s) {
            return new String[]{
                    s.id(), s.nom(), s.prenom(),
                    s.sexe().name(),
                    s.niveau().name(),
                    s.formation().name(),
                    s.status().name()
            };
        }
        @Override public Student fromRow(String[] r) {
            return new Student(
                    r[0], r[1], r[2],
                    Gender.valueOf(r[3]),
                    Niveau.valueOf(r[4]),
                    Formation.valueOf(r[5]),
                    Status.valueOf(r[6])
            );
        }
    }
}

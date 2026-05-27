package com.naoty.gradebook.repository.csv;

import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Semestre;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.repository.SubjectRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class CsvSubjectRepository extends AbstractCsvRepository<Subject, String>
        implements SubjectRepository {

    public CsvSubjectRepository(Path file, Path backupDir) {
        super(file, backupDir, new Mapper(), Subject::id);
    }

    public static CsvMapper<Subject> mapper() { return new Mapper(); }

    @Override public List<Subject> findAll()                  { return super.findAll(); }
    @Override public Optional<Subject> findById(String id)    { return super.findById(id); }
    @Override public Subject save(Subject e)                  { return super.save(e); }
    @Override public void deleteById(String id)               { super.deleteById(id); }

    private static class Mapper implements CsvMapper<Subject> {
        @Override public String[] headers() {
            return new String[]{"id", "nom", "coefficient", "niveau", "semestre",
                                "formation", "profId", "weightDS", "weightTP", "weightExam"};
        }
        @Override public String[] toRow(Subject s) {
            return new String[]{
                    s.id(), s.nom(), String.valueOf(s.coefficient()),
                    s.niveau().name(), s.semestre().name(),
                    s.formation().name(), s.profId(),
                    String.valueOf(s.weightDS()),
                    String.valueOf(s.weightTP()),
                    String.valueOf(s.weightExam())
            };
        }
        @Override public Subject fromRow(String[] r) {
            return new Subject(
                    r[0], r[1], Integer.parseInt(r[2]),
                    Niveau.valueOf(r[3]),
                    Semestre.valueOf(r[4]),
                    Formation.valueOf(r[5]),
                    r[6],
                    Double.parseDouble(r[7]),
                    Double.parseDouble(r[8]),
                    Double.parseDouble(r[9])
            );
        }
    }
}

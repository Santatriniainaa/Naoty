package com.naoty.gradebook.service;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.repository.csv.CsvGradeRepository;
import com.naoty.gradebook.repository.csv.CsvMapper;
import com.naoty.gradebook.repository.csv.CsvStudentRepository;
import com.naoty.gradebook.repository.csv.CsvSubjectRepository;
import com.naoty.gradebook.repository.csv.CsvUserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Parse un fichier CSV et l'applique au repository correspondant.
 * Mode "merge" : pour chaque ligne valide, upsert (création ou mise à jour
 * si l'ID existe déjà). Les lignes invalides sont rapportées avec un
 * message d'erreur lisible.
 */
public class CsvImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);

    private final AppContext context;

    public CsvImportService(AppContext context) {
        this.context = context;
    }

    public enum Target { PROFS, STUDENTS, SUBJECTS, GRADES }

    public record ParsedRow(int lineNumber, String[] raw, Object entity, String error) {
        public boolean isValid() { return entity != null; }
    }

    public record ImportPreview(Target target, String[] expectedHeaders, List<ParsedRow> rows) {
        public long validCount()   { return rows.stream().filter(ParsedRow::isValid).count(); }
        public long invalidCount() { return rows.size() - validCount(); }
    }

    public ImportPreview preview(Target target, Path file) {
        return switch (target) {
            case PROFS    -> parse(target, file, CsvUserRepository.mapper());
            case STUDENTS -> parse(target, file, CsvStudentRepository.mapper());
            case SUBJECTS -> parse(target, file, CsvSubjectRepository.mapper());
            case GRADES   -> parse(target, file, CsvGradeRepository.mapper());
        };
    }

    public int apply(ImportPreview preview) {
        Consumer<Object> saver = switch (preview.target()) {
            case PROFS    -> obj -> context.userService().create(    (com.naoty.gradebook.model.User)    obj);
            case STUDENTS -> obj -> context.studentService().create((com.naoty.gradebook.model.Student) obj);
            case SUBJECTS -> obj -> context.subjectService().create((com.naoty.gradebook.model.Subject) obj);
            case GRADES   -> obj -> context.gradeService().create(  (com.naoty.gradebook.model.Grade)   obj);
        };
        Function<Object, String> idOf = switch (preview.target()) {
            case PROFS    -> obj -> ((com.naoty.gradebook.model.User)    obj).id();
            case STUDENTS -> obj -> ((com.naoty.gradebook.model.Student) obj).id();
            case SUBJECTS -> obj -> ((com.naoty.gradebook.model.Subject) obj).id();
            case GRADES   -> obj -> ((com.naoty.gradebook.model.Grade)   obj).id();
        };
        Consumer<String> deleter = switch (preview.target()) {
            case PROFS    -> context.userService()::delete;
            case STUDENTS -> context.studentService()::delete;
            case SUBJECTS -> context.subjectService()::delete;
            case GRADES   -> context.gradeService()::delete;
        };

        int applied = 0;
        for (ParsedRow row : preview.rows()) {
            if (!row.isValid()) continue;
            try {
                // upsert : delete by id puis create (le service gère sa propre
                // publication EventBus à chaque opération)
                String id = idOf.apply(row.entity());
                deleter.accept(id);
                saver.accept(row.entity());
                applied++;
            } catch (Exception e) {
                log.warn("Failed to apply row {}: {}", row.lineNumber(), e.getMessage());
            }
        }
        return applied;
    }

    private <T> ImportPreview parse(Target target, Path file, CsvMapper<T> mapper) {
        List<ParsedRow> rows = new ArrayList<>();
        String[] expectedHeaders = mapper.headers();

        try (Reader reader = Files.newBufferedReader(file);
             CSVReader csv = new CSVReader(reader)) {
            String[] header = csv.readNext();
            if (header == null) {
                rows.add(new ParsedRow(1, new String[0], null, "Fichier vide"));
                return new ImportPreview(target, expectedHeaders, rows);
            }
            if (!matchesHeaders(header, expectedHeaders)) {
                rows.add(new ParsedRow(1, header, null,
                        "En-tête invalide. Attendu : " + String.join(", ", expectedHeaders)));
                return new ImportPreview(target, expectedHeaders, rows);
            }

            String[] raw;
            int lineNum = 1;
            while ((raw = csv.readNext()) != null) {
                lineNum++;
                if (raw.length == 0 || (raw.length == 1 && raw[0].isBlank())) continue;
                try {
                    T entity = mapper.fromRow(raw);
                    rows.add(new ParsedRow(lineNum, raw, entity, null));
                } catch (Exception e) {
                    rows.add(new ParsedRow(lineNum, raw, null, errorMessage(e)));
                }
            }
        } catch (IOException | CsvValidationException e) {
            rows.add(new ParsedRow(0, new String[0], null, "Lecture impossible : " + e.getMessage()));
        }
        return new ImportPreview(target, expectedHeaders, rows);
    }

    private static boolean matchesHeaders(String[] actual, String[] expected) {
        if (actual.length != expected.length) return false;
        for (int i = 0; i < actual.length; i++) {
            if (!actual[i].trim().equalsIgnoreCase(expected[i])) return false;
        }
        return true;
    }

    private static String errorMessage(Exception e) {
        String msg = e.getMessage();
        return msg == null ? e.getClass().getSimpleName() : msg;
    }
}

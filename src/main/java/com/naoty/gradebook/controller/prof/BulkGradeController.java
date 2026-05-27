package com.naoty.gradebook.controller.prof;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.Grade;
import com.naoty.gradebook.model.GradeType;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.i18n.I18n;
import com.naoty.gradebook.validation.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tableau éditable pour saisir une note par étudiant pour une matière
 * et un type donné. Une seule note (DS/TP/EXAM) par session de saisie.
 * Si une note de ce type existe déjà pour l'étudiant, elle est pré-remplie
 * et mise à jour à l'enregistrement.
 */
public class BulkGradeController {

    private final AppContext context;
    private Subject subject;

    @FXML private Label titleLabel, subtitleLabel, dateLabel, countLabel;
    @FXML private ComboBox<GradeType> typeCombo;
    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row, String> colId, colNom, colPrenom, colStatus, colNote, colState;

    private final ObservableList<Row> rows = FXCollections.observableArrayList();

    public BulkGradeController(AppContext context) {
        this.context = context;
    }

    public static final class Row {
        final Student student;
        final SimpleStringProperty note;
        final SimpleStringProperty state;
        Grade existingGrade;

        Row(Student student, String existingValue, Grade existing) {
            this.student = student;
            this.note = new SimpleStringProperty(existingValue);
            this.state = new SimpleStringProperty(existing != null ? "Saisi" : "");
            this.existingGrade = existing;
        }
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().student.id()));
        colNom.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().student.nom()));
        colPrenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().student.prenom()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().student.status().name()));
        colNote.setCellValueFactory(c   -> c.getValue().note);
        colState.setCellValueFactory(c  -> c.getValue().state);

        colNote.setCellFactory(TextFieldTableCell.forTableColumn());
        colNote.setOnEditCommit(event -> {
            String newValue = event.getNewValue() == null ? "" : event.getNewValue().trim();
            Row row = event.getRowValue();
            row.note.set(newValue);
            row.state.set(validateValue(newValue));
            table.refresh();
        });

        typeCombo.getItems().setAll(GradeType.values());
        typeCombo.setValue(GradeType.DS);
        typeCombo.valueProperty().addListener((o, a, b) -> reloadRows());

        dateLabel.setText(LocalDate.now().toString());
        table.setItems(rows);
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        titleLabel.setText(I18n.get("bulk.title", subject.nom()));
        subtitleLabel.setText(subject.niveau() + " · " + subject.semestre()
                + " · " + subject.formation()
                + " · coef " + subject.coefficient());
        reloadRows();
    }

    private void reloadRows() {
        if (subject == null) return;
        GradeType type = typeCombo.getValue();

        // Étudiants applicables : même niveau, formation compatible
        List<Student> students = context.studentService().findAll().stream()
                .filter(s -> s.niveau() == subject.niveau()
                        && subject.formation().matches(s.formation()))
                .toList();

        // Notes existantes (subjectId, type) indexées par studentId
        Map<String, Grade> existing = new HashMap<>();
        for (Grade g : context.gradeService().findBySubjectId(subject.id())) {
            if (g.type() == type) existing.put(g.studentId(), g);
        }

        List<Row> newRows = new ArrayList<>(students.size());
        for (Student s : students) {
            Grade g = existing.get(s.id());
            newRows.add(new Row(s, g == null ? "" : String.valueOf(g.value()), g));
        }
        rows.setAll(newRows);
        countLabel.setText(I18n.get("bulk.studentCount", students.size()));
    }

    /** Renvoie un libellé visible si la valeur n'est pas dans 0-20. */
    private String validateValue(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            double v = Double.parseDouble(value.replace(',', '.'));
            if (v < 0 || v > 20) return "⚠ 0-20";
            return "✓";
        } catch (NumberFormatException e) {
            return "⚠ NaN";
        }
    }

    @FXML
    private void onSaveAll() {
        if (subject == null) return;
        User prof = context.session().currentUser().orElse(null);
        if (prof == null) return;

        GradeType type = typeCombo.getValue();
        LocalDate today = LocalDate.now();

        int saved = 0;
        int errors = 0;
        List<String> errorMessages = new ArrayList<>();

        for (Row row : rows) {
            String text = row.note.get();
            if (text == null || text.isBlank()) {
                // Si la note existait et a été vidée → suppression
                if (row.existingGrade != null) {
                    context.gradeService().delete(row.existingGrade.id());
                    row.existingGrade = null;
                    row.state.set("Supprimé");
                    saved++;
                }
                continue;
            }
            double value;
            try {
                value = Double.parseDouble(text.replace(',', '.'));
            } catch (NumberFormatException e) {
                errors++;
                errorMessages.add(row.student.fullName() + " : valeur invalide");
                continue;
            }
            if (value < 0 || value > 20) {
                errors++;
                errorMessages.add(row.student.fullName() + " : note hors 0-20");
                continue;
            }

            try {
                Grade candidate;
                if (row.existingGrade != null) {
                    candidate = new Grade(row.existingGrade.id(), row.student.id(),
                            subject.id(), prof.id(), type, value, today);
                    ValidationResult r = context.gradeService().update(candidate);
                    if (r.valid()) { row.state.set("✓ Mis à jour"); saved++; }
                    else { errors++; }
                } else {
                    candidate = new Grade("G-" + UUID.randomUUID().toString().substring(0, 8),
                            row.student.id(), subject.id(), prof.id(), type, value, today);
                    ValidationResult r = context.gradeService().create(candidate);
                    if (r.valid()) { row.existingGrade = candidate; row.state.set("✓ Enregistré"); saved++; }
                    else { errors++; }
                }
            } catch (IllegalArgumentException e) {
                errors++;
                errorMessages.add(row.student.fullName() + " : " + e.getMessage());
            }
        }

        table.refresh();

        if (errors == 0) {
            AlertUtils.info("bulk.saved.title",
                    saved > 0 ? "bulk.saved.success" : "bulk.saved.nothing");
            if (saved > 0) close();
        } else {
            AlertUtils.error("bulk.saved.title",
                    "bulk.saved.partial");
        }
    }

    @FXML private void onCancel() { close(); }
    private void close() { ((Stage) table.getScene().getWindow()).close(); }
}

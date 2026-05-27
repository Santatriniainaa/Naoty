package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.GradeType;
import com.naoty.gradebook.model.Semestre;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.ui.AlertUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class BulletinsController {

    private static final Logger log = LoggerFactory.getLogger(BulletinsController.class);

    private final AppContext context;

    @FXML private ComboBox<Student> studentCombo;
    @FXML private ComboBox<Semestre> semestreCombo;
    @FXML private TableView<Row> previewTable;
    @FXML private TableColumn<Row, String> colMatiere, colDS, colTP, colEX, colMoy;
    @FXML private TableColumn<Row, Number> colCoef;
    @FXML private Label moyenneLabel, mentionLabel, rangLabel, previewTitleLabel;
    @FXML private Button generateButton;

    private final ObservableList<Row> rows = FXCollections.observableArrayList();

    public BulletinsController(AppContext context) {
        this.context = context;
    }

    public record Row(String matiere, int coef, String ds, String tp, String exam, String moy) {}

    @FXML
    public void initialize() {
        // Setup combos
        studentCombo.getItems().setAll(context.studentService().findAll());
        studentCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Student s) {
                return s == null ? "" : s.fullName() + "  ·  " + s.niveau() + " " + s.formation();
            }
            @Override public Student fromString(String string) { return null; }
        });

        semestreCombo.getItems().setAll(Semestre.values());

        // Table bindings
        colMatiere.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().matiere()));
        colCoef.setCellValueFactory((TableColumn.CellDataFeatures<Row, Number> c) ->
                (ObservableValue) new SimpleIntegerProperty(c.getValue().coef()));
        colDS.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().ds()));
        colTP.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().tp()));
        colEX.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().exam()));
        colMoy.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().moy()));
        previewTable.setItems(rows);

        // Listeners
        studentCombo.valueProperty().addListener((o, a, b) -> autoSelectSemestre());
        semestreCombo.valueProperty().addListener((o, a, b) -> recompute());

        context.eventBus().subscribe("grades.changed", e -> recompute());
        context.eventBus().subscribe("subjects.changed", e -> recompute());
    }

    /** Si l'étudiant change, prendre le premier semestre de son niveau. */
    private void autoSelectSemestre() {
        Student s = studentCombo.getValue();
        if (s != null && semestreCombo.getValue() == null) {
            semestreCombo.setValue(Semestre.of(s.niveau())[0]);
        } else {
            recompute();
        }
    }

    private void recompute() {
        rows.clear();
        Student student = studentCombo.getValue();
        Semestre semestre = semestreCombo.getValue();
        if (student == null || semestre == null) {
            generateButton.setDisable(true);
            moyenneLabel.setText("—");
            mentionLabel.setText("—");
            rangLabel.setText("—");
            previewTitleLabel.setText("");
            return;
        }

        previewTitleLabel.setText(student.fullName() + " — " + student.niveau() + " "
                + student.formation() + " — " + semestre);

        List<Subject> subjects = context.subjectService().findForStudent(student, semestre);
        for (Subject sub : subjects) {
            Map<GradeType, Double> avgByType = context.statsService().averagesByType(student.id(), sub.id());
            OptionalDouble subAvg = context.statsService().subjectAverage(student.id(), sub);
            rows.add(new Row(
                    sub.nom(),
                    sub.coefficient(),
                    format(avgByType.get(GradeType.DS)),
                    format(avgByType.get(GradeType.TP)),
                    format(avgByType.get(GradeType.EXAM)),
                    subAvg.isPresent() ? String.format("%.2f", subAvg.getAsDouble()) : "—"
            ));
        }

        OptionalDouble semAvg = context.statsService().semestreAverage(student, semestre);
        if (semAvg.isPresent()) {
            moyenneLabel.setText(String.format("%.2f / 20", semAvg.getAsDouble()));
            mentionLabel.setText(context.statsService().mention(semAvg.getAsDouble())
                    .name().replace('_', ' '));
            int r = context.statsService().rank(student, semestre);
            rangLabel.setText(r > 0 ? String.valueOf(r) : "—");
            generateButton.setDisable(false);
        } else {
            moyenneLabel.setText("—");
            mentionLabel.setText("—");
            rangLabel.setText("—");
            generateButton.setDisable(rows.isEmpty());
        }
    }

    private static String format(Double v) {
        return v == null ? "—" : String.format("%.2f", v);
    }

    @FXML
    private void onGenerate() {
        Student student = studentCombo.getValue();
        Semestre semestre = semestreCombo.getValue();
        if (student == null || semestre == null) return;
        try {
            Path output = Path.of("bulletins",
                    "bulletin-" + student.id() + "-" + semestre.name() + ".pdf");
            context.bulletinService().generate(student, semestre, output);
            AlertUtils.info("bulletin.saved.title", "bulletin.saved.content");
        } catch (RuntimeException e) {
            log.error("Failed to generate bulletin", e);
            AlertUtils.error("bulletin.error.title", "bulletin.error.content");
        }
    }
}

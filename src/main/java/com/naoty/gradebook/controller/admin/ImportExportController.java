package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.service.CsvImportService.Target;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.Modal;
import com.naoty.gradebook.ui.i18n.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ImportExportController {

    private static final Logger log = LoggerFactory.getLogger(ImportExportController.class);

    private final AppContext context;

    @FXML private Label profsCountLabel, studentsCountLabel, subjectsCountLabel, gradesCountLabel;

    public ImportExportController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        refresh();
        context.eventBus().subscribe("users.changed",    e -> refresh());
        context.eventBus().subscribe("students.changed", e -> refresh());
        context.eventBus().subscribe("subjects.changed", e -> refresh());
        context.eventBus().subscribe("grades.changed",   e -> refresh());
    }

    private void refresh() {
        profsCountLabel.setText(I18n.get("io.count.profs",    context.userService().findProfs().size()));
        studentsCountLabel.setText(I18n.get("io.count.students", context.studentService().findAll().size()));
        subjectsCountLabel.setText(I18n.get("io.count.subjects", context.subjectService().findAll().size()));
        gradesCountLabel.setText(I18n.get("io.count.grades",   context.gradeService().findAll().size()));
    }

    // ---------- EXPORT ----------

    @FXML private void onExportProfs()    { export(context.config().usersFile(),    "users.csv"); }
    @FXML private void onExportStudents() { export(context.config().studentsFile(), "students.csv"); }
    @FXML private void onExportSubjects() { export(context.config().subjectsFile(), "subjects.csv"); }
    @FXML private void onExportGrades()   { export(context.config().gradesFile(),   "grades.csv"); }

    private void export(Path source, String defaultName) {
        if (!Files.exists(source)) {
            AlertUtils.error("io.export.noData.title", "io.export.noData.content");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("io.export"));
        chooser.setInitialFileName(defaultName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        File chosen = chooser.showSaveDialog(currentWindow());
        if (chosen == null) return;
        try {
            Files.copy(source, chosen.toPath(), StandardCopyOption.REPLACE_EXISTING);
            AlertUtils.info("io.export.done.title", "io.export.done.content");
        } catch (Exception e) {
            log.error("Export failed", e);
            AlertUtils.error("io.export.error.title", "io.export.error.content");
        }
    }

    // ---------- IMPORT ----------

    @FXML private void onImportProfs()    { startImport(Target.PROFS); }
    @FXML private void onImportStudents() { startImport(Target.STUDENTS); }
    @FXML private void onImportSubjects() { startImport(Target.SUBJECTS); }
    @FXML private void onImportGrades()   { startImport(Target.GRADES); }

    private void startImport(Target target) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("io.import"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        File chosen = chooser.showOpenDialog(currentWindow());
        if (chosen == null) return;

        Modal.open(context, currentWindow(),
                "/fxml/admin/import-wizard.fxml",
                "/css/theme-" + context.currentTheme() + ".css",
                (ImportWizardController c) -> c.setSource(target, chosen.toPath()));
    }

    private Window currentWindow() {
        return profsCountLabel.getScene().getWindow();
    }
}

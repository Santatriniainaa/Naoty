package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.exception.DuplicateEntityException;
import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Gender;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Status;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.StringUtils;
import com.naoty.gradebook.ui.i18n.I18n;
import com.naoty.gradebook.validation.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

public class StudentFormController {

    private final AppContext context;
    private Student editing;

    @FXML private Label titleLabel;
    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ComboBox<Gender> sexeCombo;
    @FXML private ComboBox<Niveau> niveauCombo;
    @FXML private ComboBox<Formation> formationCombo;
    @FXML private ComboBox<Status> statusCombo;

    public StudentFormController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        sexeCombo.getItems().setAll(Gender.values());
        niveauCombo.getItems().setAll(Niveau.values());
        formationCombo.getItems().setAll(Formation.ACADEMIQUE, Formation.PROFESSIONNEL);
        statusCombo.getItems().setAll(Status.values());
    }

    public void setStudent(Student s) {
        this.editing = s;
        if (s == null) {
            titleLabel.setText(I18n.get("form.student.title.add"));
            idField.setText("STU-" + UUID.randomUUID().toString().substring(0, 8));
            niveauCombo.setValue(Niveau.L1);
            formationCombo.setValue(Formation.ACADEMIQUE);
            statusCombo.setValue(Status.PASSANT);
        } else {
            titleLabel.setText(I18n.get("form.student.title.edit"));
            idField.setText(s.id());
            idField.setDisable(true);
            nomField.setText(s.nom());
            prenomField.setText(s.prenom());
            sexeCombo.setValue(s.sexe());
            niveauCombo.setValue(s.niveau());
            formationCombo.setValue(s.formation());
            statusCombo.setValue(s.status());
        }
    }

    @FXML
    private void onSave() {
        try {
            Student candidate = new Student(
                    StringUtils.trim(idField.getText()),
                    StringUtils.trim(nomField.getText()),
                    StringUtils.trim(prenomField.getText()),
                    sexeCombo.getValue(),
                    niveauCombo.getValue(),
                    formationCombo.getValue(),
                    statusCombo.getValue()
            );
            ValidationResult r = (editing == null) ? tryCreate(candidate)
                                                   : context.studentService().update(candidate);
            if (!r.valid()) { AlertUtils.validationErrors(r); return; }
            close();
        } catch (IllegalArgumentException e) {
            AlertUtils.error("alert.validation.title", "alert.validation.header");
        }
    }

    private ValidationResult tryCreate(Student s) {
        try {
            return context.studentService().create(s);
        } catch (DuplicateEntityException e) {
            AlertUtils.error("alert.duplicate.title", "alert.duplicate.content");
            return ValidationResult.fail("alert.duplicate.content");
        }
    }

    @FXML private void onCancel() { close(); }
    private void close() { ((Stage) idField.getScene().getWindow()).close(); }
}

package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.exception.DuplicateEntityException;
import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Semestre;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.StringUtils;
import com.naoty.gradebook.ui.i18n.I18n;
import com.naoty.gradebook.validation.ValidationResult;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Formulaire admin pour créer/éditer une matière. L'admin gère les
 * caractéristiques (nom, niveau, semestre, formation, coefficient, prof
 * assigné) mais <strong>pas les poids DS/TP/EXAM</strong> — ces derniers
 * sont configurés par le professeur depuis sa page "Mes matières".
 *
 * <p>À la création, des poids par défaut sont assignés : DS 30%, TP 20%,
 * EXAM 50%. Le prof peut ensuite les ajuster.
 */
public class SubjectFormController {

    private static final double DEFAULT_WEIGHT_DS   = 0.30;
    private static final double DEFAULT_WEIGHT_TP   = 0.20;
    private static final double DEFAULT_WEIGHT_EXAM = 0.50;

    private final AppContext context;
    private Subject editing;

    @FXML private Label titleLabel;
    @FXML private TextField idField, nomField;
    @FXML private Spinner<Integer> coefSpinner;
    @FXML private ComboBox<Niveau> niveauCombo;
    @FXML private ComboBox<Semestre> semestreCombo;
    @FXML private ComboBox<Formation> formationCombo;
    @FXML private ComboBox<User> profCombo;

    public SubjectFormController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        coefSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));

        niveauCombo.getItems().setAll(Niveau.values());
        formationCombo.getItems().setAll(Formation.values());

        niveauCombo.valueProperty().addListener((o, a, niveau) -> {
            if (niveau != null) {
                semestreCombo.setItems(FXCollections.observableArrayList(Semestre.of(niveau)));
                semestreCombo.setValue(Semestre.of(niveau)[0]);
            }
        });

        profCombo.getItems().setAll(context.userService().findProfs());
        profCombo.setConverter(new StringConverter<>() {
            @Override public String toString(User u) { return u == null ? "" : u.fullName() + " (" + u.id() + ")"; }
            @Override public User fromString(String s) { return null; }
        });
    }

    public void setSubject(Subject s) {
        this.editing = s;
        if (s == null) {
            titleLabel.setText(I18n.get("form.subject.title.add"));
            niveauCombo.setValue(Niveau.L1);
            formationCombo.setValue(Formation.BOTH);
        } else {
            titleLabel.setText(I18n.get("form.subject.title.edit"));
            idField.setText(s.id());
            idField.setDisable(true);
            nomField.setText(s.nom());
            coefSpinner.getValueFactory().setValue(s.coefficient());
            niveauCombo.setValue(s.niveau());
            semestreCombo.setValue(s.semestre());
            formationCombo.setValue(s.formation());
            context.userService().findById(s.profId()).ifPresent(profCombo::setValue);
        }
    }

    @FXML
    private void onSave() {
        try {
            User prof = profCombo.getValue();
            // Conserve les poids existants si on édite, sinon défauts.
            double wDS   = editing != null ? editing.weightDS()   : DEFAULT_WEIGHT_DS;
            double wTP   = editing != null ? editing.weightTP()   : DEFAULT_WEIGHT_TP;
            double wExam = editing != null ? editing.weightExam() : DEFAULT_WEIGHT_EXAM;

            Subject candidate = new Subject(
                    StringUtils.trim(idField.getText()),
                    StringUtils.trim(nomField.getText()),
                    coefSpinner.getValue(),
                    niveauCombo.getValue(),
                    semestreCombo.getValue(),
                    formationCombo.getValue(),
                    prof == null ? "" : prof.id(),
                    wDS, wTP, wExam
            );
            ValidationResult r = (editing == null) ? tryCreate(candidate)
                                                   : context.subjectService().update(candidate);
            if (!r.valid()) { AlertUtils.validationErrors(r); return; }
            close();
        } catch (IllegalArgumentException e) {
            AlertUtils.error("alert.validation.title", "alert.validation.header");
        }
    }

    private ValidationResult tryCreate(Subject s) {
        try {
            return context.subjectService().create(s);
        } catch (DuplicateEntityException e) {
            AlertUtils.error("alert.duplicate.title", "alert.duplicate.content");
            return ValidationResult.fail("alert.duplicate.content");
        }
    }

    @FXML private void onCancel() { close(); }
    private void close() { ((Stage) idField.getScene().getWindow()).close(); }
}

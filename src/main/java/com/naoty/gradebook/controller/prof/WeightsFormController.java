package com.naoty.gradebook.controller.prof;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.i18n.I18n;
import com.naoty.gradebook.validation.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

/**
 * Modal "Poids des notes" — accessible côté prof uniquement, depuis la page
 * "Mes matières". Permet de configurer la pondération DS/TP/EXAM pour
 * <em>une</em> matière dont le prof est responsable.
 */
public class WeightsFormController {

    private final AppContext context;
    private Subject subject;

    @FXML private Label subjectLabel;
    @FXML private Label totalLabel;
    @FXML private Spinner<Double> weightDS, weightTP, weightExam;

    public WeightsFormController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        weightDS.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.30, 0.05));
        weightTP.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.20, 0.05));
        weightExam.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.50, 0.05));

        weightDS.valueProperty().addListener((o, a, b)   -> refreshTotal());
        weightTP.valueProperty().addListener((o, a, b)   -> refreshTotal());
        weightExam.valueProperty().addListener((o, a, b) -> refreshTotal());
    }

    public void setSubject(Subject s) {
        this.subject = s;
        subjectLabel.setText(s.nom() + "  ·  " + s.niveau() + " " + s.semestre()
                + "  ·  " + s.formation());
        weightDS.getValueFactory().setValue(s.weightDS());
        weightTP.getValueFactory().setValue(s.weightTP());
        weightExam.getValueFactory().setValue(s.weightExam());
        refreshTotal();
    }

    private void refreshTotal() {
        double sum = weightDS.getValue() + weightTP.getValue() + weightExam.getValue();
        totalLabel.setText(I18n.get("form.weights.total", String.format("%.0f", sum * 100)));
    }

    @FXML
    private void onSave() {
        if (subject == null) return;
        try {
            Subject updated = new Subject(
                    subject.id(), subject.nom(), subject.coefficient(),
                    subject.niveau(), subject.semestre(), subject.formation(),
                    subject.profId(),
                    weightDS.getValue(),
                    weightTP.getValue(),
                    weightExam.getValue()
            );
            ValidationResult r = context.subjectService().update(updated);
            if (!r.valid()) { AlertUtils.validationErrors(r); return; }
            close();
        } catch (IllegalArgumentException e) {
            AlertUtils.error("alert.validation.title", "subject.weights.invalid");
        }
    }

    @FXML private void onCancel() { close(); }
    private void close() { ((Stage) weightDS.getScene().getWindow()).close(); }
}

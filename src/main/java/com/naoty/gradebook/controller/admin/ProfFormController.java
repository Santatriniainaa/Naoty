package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.exception.DuplicateEntityException;
import com.naoty.gradebook.model.Role;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.StringUtils;
import com.naoty.gradebook.ui.i18n.I18n;
import com.naoty.gradebook.validation.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProfFormController {

    private final AppContext context;
    private User editing;

    @FXML private Label titleLabel;
    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;

    public ProfFormController(AppContext context) {
        this.context = context;
    }

    public void setUser(User u) {
        this.editing = u;
        if (u == null) {
            titleLabel.setText(I18n.get("form.prof.title.add"));
        } else {
            titleLabel.setText(I18n.get("form.prof.title.edit"));
            idField.setText(u.id());
            idField.setDisable(true);
            nomField.setText(u.nom());
            prenomField.setText(u.prenom());
        }
    }

    @FXML
    private void onSave() {
        User candidate = new User(
                StringUtils.trim(idField.getText()),
                Role.PROF,
                StringUtils.trim(nomField.getText()),
                StringUtils.trim(prenomField.getText())
        );
        ValidationResult result = (editing == null) ? tryCreate(candidate)
                                                    : context.userService().update(candidate);
        if (!result.valid()) { AlertUtils.validationErrors(result); return; }
        close();
    }

    private ValidationResult tryCreate(User u) {
        try {
            return context.userService().create(u);
        } catch (DuplicateEntityException e) {
            AlertUtils.error("alert.duplicate.title", "alert.duplicate.content");
            return ValidationResult.fail("alert.duplicate.content");
        }
    }

    @FXML
    private void onCancel() { close(); }

    private void close() { ((Stage) idField.getScene().getWindow()).close(); }
}

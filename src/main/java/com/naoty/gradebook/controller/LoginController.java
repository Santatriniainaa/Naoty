package com.naoty.gradebook.controller;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.auth.Session;
import com.naoty.gradebook.ui.SceneRouter;
import com.naoty.gradebook.ui.i18n.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    private final AppContext context;

    @FXML private TextField codeField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    public LoginController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        clearError();
        codeField.textProperty().addListener((obs, oldV, newV) -> clearError());
    }

    @FXML
    private void onLogin() {
        String input = codeField.getText() == null ? "" : codeField.getText().trim();
        if (input.isEmpty()) {
            showError("login.error.empty");
            return;
        }
        Optional<Session> result = context.authService().login(input);
        if (result.isEmpty()) {
            showError("login.error.unknown");
            return;
        }
        SceneRouter.toShell(context);
    }

    private void showError(String key) {
        errorLabel.setText(I18n.get(key));
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        codeField.getStyleClass().add("input-error");
    }

    private void clearError() {
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        codeField.getStyleClass().remove("input-error");
    }
}

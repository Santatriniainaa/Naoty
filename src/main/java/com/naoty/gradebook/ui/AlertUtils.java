package com.naoty.gradebook.ui;

import com.naoty.gradebook.ui.i18n.I18n;
import com.naoty.gradebook.validation.ValidationResult;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.stream.Collectors;

public final class AlertUtils {

    private AlertUtils() {}

    public static void info(String titleKey, String contentKey) {
        show(AlertType.INFORMATION, titleKey, contentKey);
    }

    public static void error(String titleKey, String contentKey) {
        show(AlertType.ERROR, titleKey, contentKey);
    }

    public static void validationErrors(ValidationResult result) {
        if (result.valid()) return;
        String content = result.errors().stream()
                .map(I18n::get)
                .collect(Collectors.joining("\n• ", "• ", ""));
        Alert a = new Alert(AlertType.ERROR);
        a.setTitle(I18n.get("alert.validation.title"));
        a.setHeaderText(I18n.get("alert.validation.header"));
        a.setContentText(content);
        a.showAndWait();
    }

    public static boolean confirm(String titleKey, String contentKey) {
        Alert a = new Alert(AlertType.CONFIRMATION);
        a.setTitle(I18n.get(titleKey));
        a.setHeaderText(null);
        a.setContentText(I18n.get(contentKey));
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    private static void show(AlertType type, String titleKey, String contentKey) {
        Alert a = new Alert(type);
        a.setTitle(I18n.get(titleKey));
        a.setHeaderText(null);
        a.setContentText(I18n.get(contentKey));
        a.showAndWait();
    }
}

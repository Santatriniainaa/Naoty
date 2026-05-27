package com.naoty.gradebook.controller;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.ui.SceneRouter;
import com.naoty.gradebook.ui.i18n.I18n;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.Locale;

public class SettingsController {

    private final AppContext context;

    @FXML private Button lightBtn, darkBtn, frBtn, enBtn, mgBtn;
    @FXML private Label aboutLabel;

    public SettingsController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        aboutLabel.setText(I18n.get("settings.about.body",
                "Naoty Gradebook 0.1.0-SNAPSHOT",
                System.getProperty("java.version")));
        refreshActiveButtons();
    }

    private void refreshActiveButtons() {
        markActive(lightBtn, "light".equals(context.currentTheme()));
        markActive(darkBtn,  "dark".equals(context.currentTheme()));

        String tag = I18n.currentLocale().toLanguageTag();
        markActive(frBtn, tag.startsWith("fr"));
        markActive(enBtn, tag.startsWith("en"));
        markActive(mgBtn, tag.startsWith("mg"));
    }

    private void markActive(Button b, boolean active) {
        b.getStyleClass().remove("settings-pick-active");
        if (active) b.getStyleClass().add("settings-pick-active");
    }

    // ---------- THEME ----------

    @FXML private void onPickLight() { applyTheme("light"); }
    @FXML private void onPickDark()  { applyTheme("dark"); }

    private void applyTheme(String theme) {
        context.setCurrentTheme(theme);
        Scene scene = lightBtn.getScene();
        SceneRouter.applyTheme(scene, context);
        refreshActiveButtons();
    }

    // ---------- LANGUAGE ----------

    @FXML private void onPickFr() { applyLocale(Locale.FRANCE); }
    @FXML private void onPickEn() { applyLocale(Locale.ENGLISH); }
    @FXML private void onPickMg() { applyLocale(Locale.forLanguageTag("mg-MG")); }

    private void applyLocale(Locale locale) {
        I18n.setLocale(locale);
        // Recharge le shell pour que toutes les FXML utilisent le nouveau bundle.
        SceneRouter.toShell(context);
    }
}

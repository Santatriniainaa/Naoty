package com.naoty.gradebook.controller;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.ui.SceneRouter;
import com.naoty.gradebook.ui.i18n.I18n;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ShellController {

    private static final Logger log = LoggerFactory.getLogger(ShellController.class);

    private final AppContext context;
    private final List<Button> navButtons = new ArrayList<>();

    @FXML private StackPane contentArea;
    @FXML private VBox navContainer;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    public ShellController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        userNameLabel.setText(context.session().displayName());
        userRoleLabel.setText(context.session().isAdmin()
                ? I18n.get("shell.user.admin")
                : I18n.get("shell.user.prof") + " · " + context.session().displayId());

        if (context.session().isAdmin()) buildAdminNav();
        else                              buildProfNav();
        addSharedNav();

        if (!navButtons.isEmpty()) {
            activate(navButtons.get(0));
            navButtons.get(0).fire();
        }
    }

    private void buildAdminNav() {
        addNav("fth-grid",       "nav.admin.dashboard",  () -> loadPage("/fxml/admin/dashboard.fxml"));
        addNav("fth-user-check", "nav.admin.profs",      () -> loadPage("/fxml/admin/profs.fxml"));
        addNav("fth-users",      "nav.admin.students",   () -> loadPage("/fxml/admin/students.fxml"));
        addNav("fth-book",       "nav.admin.subjects",   () -> loadPage("/fxml/admin/subjects.fxml"));
        addNav("fth-award",      "nav.admin.bulletins",  () -> loadPage("/fxml/admin/bulletins.fxml"));
        addNav("fth-upload",     "nav.admin.import",     () -> loadPage("/fxml/admin/import-export.fxml"));
    }

    private void buildProfNav() {
        addNav("fth-grid",  "nav.prof.dashboard", () -> loadPage("/fxml/prof/dashboard.fxml"));
        addNav("fth-book",  "nav.prof.matieres",  () -> loadPage("/fxml/prof/matieres.fxml"));
        addNav("fth-users", "nav.prof.students",  () -> loadPage("/fxml/prof/etudiants.fxml"));
    }

    /** Entrées présentes pour tous les rôles. */
    private void addSharedNav() {
        addNav("fth-settings", "nav.settings", () -> loadPage("/fxml/settings.fxml"));
    }

    private void addNav(String icon, String labelKey, Runnable action) {
        Button b = new Button(I18n.get(labelKey));
        b.getStyleClass().add("nav-btn");
        FontIcon fi = new FontIcon(icon);
        fi.setIconSize(16);
        b.setGraphic(fi);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> { activate(b); action.run(); });
        navButtons.add(b);
        navContainer.getChildren().add(b);
    }

    private void activate(Button active) {
        for (Button b : navButtons) b.getStyleClass().remove("nav-btn-active");
        if (!active.getStyleClass().contains("nav-btn-active")) {
            active.getStyleClass().add("nav-btn-active");
        }
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxml),
                    ResourceBundle.getBundle("i18n.messages", I18n.currentLocale())
            );
            loader.setControllerFactory(new ControllerFactory(context));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            log.error("Failed to load page {}", fxml, e);
            placeholder("placeholder.coming", "placeholder.phase3");
        }
    }

    private void placeholder(String titleKey, String descKey) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/placeholder.fxml"),
                    ResourceBundle.getBundle("i18n.messages", I18n.currentLocale())
            );
            loader.setControllerFactory(new ControllerFactory(context));
            Parent view = loader.load();
            PlaceholderController ctrl = loader.getController();
            ctrl.setContent(I18n.get(titleKey), I18n.get(descKey));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            log.error("Failed to load placeholder", e);
        }
    }

    @FXML
    private void onLogout() {
        context.authService().logout();
        SceneRouter.toLogin(context);
    }
}

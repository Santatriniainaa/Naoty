package com.naoty.gradebook.ui;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.controller.ControllerFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Bascule de scène sur la fenêtre principale (login → shell, shell → login).
 * Centralise le chargement FXML + thème CSS + i18n bundle.
 */
public final class SceneRouter {

    private SceneRouter() {}

    public static void toLogin(AppContext context) {
        replace(context, "/fxml/login.fxml", 460, 560);
    }

    public static void toShell(AppContext context) {
        replace(context, "/fxml/shell.fxml", 1100, 720);
    }

    private static void replace(AppContext context, String fxml, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneRouter.class.getResource(fxml),
                    ResourceBundle.getBundle("i18n.messages", context.config().locale())
            );
            loader.setControllerFactory(new ControllerFactory(context));
            Parent root = loader.load();

            Stage stage = context.primaryStage();
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root, w, h);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
                stage.setWidth(w);
                stage.setHeight(h);
            }
            applyTheme(scene, context);
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }

    public static void applyTheme(Scene scene, AppContext context) {
        scene.getStylesheets().clear();
        var url = SceneRouter.class.getResource("/css/theme-" + context.currentTheme() + ".css");
        if (url != null) scene.getStylesheets().add(url.toExternalForm());
    }
}

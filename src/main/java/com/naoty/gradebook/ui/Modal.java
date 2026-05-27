package com.naoty.gradebook.ui;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.controller.ControllerFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public final class Modal {

    private Modal() {}

    /**
     * Ouvre un FXML dans une fenêtre modale et appelle l'initialiseur
     * sur le controller avant {@code showAndWait()}.
     */
    public static <C> void open(AppContext context, Window owner, String fxml,
                                String themeCss, Consumer<C> init) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Modal.class.getResource(fxml),
                    ResourceBundle.getBundle("i18n.messages", context.config().locale())
            );
            loader.setControllerFactory(new ControllerFactory(context));
            Parent root = loader.load();
            @SuppressWarnings("unchecked")
            C controller = loader.getController();
            if (init != null) init.accept(controller);

            Scene scene = new Scene(root);
            if (themeCss != null) {
                var url = Modal.class.getResource(themeCss);
                if (url != null) scene.getStylesheets().add(url.toExternalForm());
            }

            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open modal: " + fxml, e);
        }
    }
}

package com.naoty.gradebook.ui;

import javafx.scene.control.TableView;

import java.util.Optional;

public final class ControllerUtils {

    private ControllerUtils() {}

    /**
     * Retourne l'élément sélectionné dans la TableView, ou Optional.empty().
     * Affiche une alerte "alert.noSelection.*" si rien n'est sélectionné.
     */
    public static <T> Optional<T> requireSelection(TableView<T> table) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.info("alert.noSelection.title", "alert.noSelection.content");
            return Optional.empty();
        }
        return Optional.of(selected);
    }
}

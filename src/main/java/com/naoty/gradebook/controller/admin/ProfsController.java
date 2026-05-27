package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.ControllerUtils;
import com.naoty.gradebook.ui.Modal;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ProfsController {

    private final AppContext context;

    @FXML private TableView<User> table;
    @FXML private TableColumn<User, String> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;

    private final ObservableList<User> items = FXCollections.observableArrayList();

    public ProfsController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().id()));
        colNom.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().nom()));
        colPrenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().prenom()));

        table.setItems(items);
        reload();
        context.eventBus().subscribe("users.changed", e -> reload());
    }

    private void reload() {
        items.setAll(context.userService().findProfs());
    }

    @FXML
    private void onAdd() {
        Modal.open(context, table.getScene().getWindow(),
                "/fxml/admin/prof-form.fxml",
                "/css/theme-" + context.currentTheme() + ".css",
                (ProfFormController c) -> c.setUser(null));
    }

    @FXML
    private void onEdit() {
        ControllerUtils.requireSelection(table).ifPresent(selected ->
                Modal.open(context, table.getScene().getWindow(),
                        "/fxml/admin/prof-form.fxml",
                        "/css/theme-" + context.currentTheme() + ".css",
                        (ProfFormController c) -> c.setUser(selected)));
    }

    @FXML
    private void onDelete() {
        ControllerUtils.requireSelection(table).ifPresent(selected -> {
            if (AlertUtils.confirm("alert.delete.title", "alert.delete.content")) {
                context.userService().delete(selected.id());
            }
        });
    }
}

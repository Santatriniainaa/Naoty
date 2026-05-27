package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Status;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.ControllerUtils;
import com.naoty.gradebook.ui.Modal;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class StudentsController {

    private final AppContext context;

    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, String> colId, colNom, colPrenom, colSexe,
                                                 colNiveau, colFormation, colStatus;
    @FXML private TextField searchField;
    @FXML private ComboBox<Niveau> niveauFilter;
    @FXML private ComboBox<Formation> formationFilter;
    @FXML private ComboBox<Status> statusFilter;

    private final ObservableList<Student> items = FXCollections.observableArrayList();

    public StudentsController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c        -> new SimpleStringProperty(c.getValue().id()));
        colNom.setCellValueFactory(c       -> new SimpleStringProperty(c.getValue().nom()));
        colPrenom.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().prenom()));
        colSexe.setCellValueFactory(c      -> new SimpleStringProperty(c.getValue().sexe().name()));
        colNiveau.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().niveau().name()));
        colFormation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().formation().name()));
        colStatus.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().status().name()));

        niveauFilter.getItems().add(null);
        niveauFilter.getItems().addAll(Niveau.values());
        formationFilter.getItems().add(null);
        formationFilter.getItems().add(Formation.ACADEMIQUE);
        formationFilter.getItems().add(Formation.PROFESSIONNEL);
        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(Status.values());

        table.setItems(items);
        reload();

        searchField.textProperty().addListener((o, a, b) -> reload());
        niveauFilter.valueProperty().addListener((o, a, b) -> reload());
        formationFilter.valueProperty().addListener((o, a, b) -> reload());
        statusFilter.valueProperty().addListener((o, a, b) -> reload());

        context.eventBus().subscribe("students.changed", e -> reload());
    }

    private void reload() {
        items.setAll(context.studentService().findBy(
                niveauFilter.getValue(),
                formationFilter.getValue(),
                statusFilter.getValue(),
                searchField.getText()));
    }

    @FXML
    private void onAdd() {
        Modal.open(context, table.getScene().getWindow(),
                "/fxml/admin/student-form.fxml",
                "/css/theme-" + context.currentTheme() + ".css",
                (StudentFormController c) -> c.setStudent(null));
    }

    @FXML
    private void onEdit() {
        ControllerUtils.requireSelection(table).ifPresent(selected ->
                Modal.open(context, table.getScene().getWindow(),
                        "/fxml/admin/student-form.fxml",
                        "/css/theme-" + context.currentTheme() + ".css",
                        (StudentFormController c) -> c.setStudent(selected)));
    }

    @FXML
    private void onDelete() {
        ControllerUtils.requireSelection(table).ifPresent(selected -> {
            if (AlertUtils.confirm("alert.delete.title", "alert.delete.content")) {
                context.studentService().delete(selected.id());
            }
        });
    }
}

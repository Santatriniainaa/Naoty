package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Semestre;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.ControllerUtils;
import com.naoty.gradebook.ui.Modal;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

public class SubjectsController {

    private final AppContext context;

    @FXML private TableView<Subject> table;
    @FXML private TableColumn<Subject, String> colId, colNom, colNiveau, colSemestre,
                                                  colFormation, colProf, colWeights;
    @FXML private TableColumn<Subject, Number> colCoef;
    @FXML private ComboBox<Niveau> niveauFilter;
    @FXML private ComboBox<Semestre> semestreFilter;
    @FXML private ComboBox<Formation> formationFilter;
    @FXML private ComboBox<User> profFilter;

    private final ObservableList<Subject> items = FXCollections.observableArrayList();

    public SubjectsController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c        -> new SimpleStringProperty(c.getValue().id()));
        colNom.setCellValueFactory(c       -> new SimpleStringProperty(c.getValue().nom()));
        colCoef.setCellValueFactory((TableColumn.CellDataFeatures<Subject, Number> c) ->
                (ObservableValue) new SimpleIntegerProperty(c.getValue().coefficient()));
        colNiveau.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().niveau().name()));
        colSemestre.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().semestre().name()));
        colFormation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().formation().name()));
        colProf.setCellValueFactory(c -> new SimpleStringProperty(
                context.userService().findById(c.getValue().profId())
                        .map(User::fullName).orElse(c.getValue().profId())));
        colWeights.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.0f/%.0f/%.0f",
                        c.getValue().weightDS() * 100,
                        c.getValue().weightTP() * 100,
                        c.getValue().weightExam() * 100)));

        niveauFilter.getItems().add(null);
        niveauFilter.getItems().addAll(Niveau.values());
        semestreFilter.getItems().add(null);
        semestreFilter.getItems().addAll(Semestre.values());
        formationFilter.getItems().add(null);
        formationFilter.getItems().addAll(Formation.values());
        profFilter.getItems().add(null);
        profFilter.getItems().addAll(context.userService().findProfs());
        profFilter.setConverter(new StringConverter<>() {
            @Override public String toString(User u) { return u == null ? "Tous les profs" : u.fullName(); }
            @Override public User fromString(String s) { return null; }
        });

        table.setItems(items);
        reload();

        niveauFilter.valueProperty().addListener((o, a, b) -> reload());
        semestreFilter.valueProperty().addListener((o, a, b) -> reload());
        formationFilter.valueProperty().addListener((o, a, b) -> reload());
        profFilter.valueProperty().addListener((o, a, b) -> reload());

        context.eventBus().subscribe("subjects.changed", e -> reload());
        context.eventBus().subscribe("users.changed",    e -> table.refresh());
    }

    private void reload() {
        String profId = profFilter.getValue() == null ? null : profFilter.getValue().id();
        items.setAll(context.subjectService().findBy(
                niveauFilter.getValue(),
                semestreFilter.getValue(),
                formationFilter.getValue(),
                profId));
    }

    @FXML
    private void onAdd() {
        Modal.open(context, table.getScene().getWindow(),
                "/fxml/admin/subject-form.fxml",
                "/css/theme-" + context.currentTheme() + ".css",
                (SubjectFormController c) -> c.setSubject(null));
    }

    @FXML
    private void onEdit() {
        ControllerUtils.requireSelection(table).ifPresent(selected ->
                Modal.open(context, table.getScene().getWindow(),
                        "/fxml/admin/subject-form.fxml",
                        "/css/theme-" + context.currentTheme() + ".css",
                        (SubjectFormController c) -> c.setSubject(selected)));
    }

    @FXML
    private void onDelete() {
        ControllerUtils.requireSelection(table).ifPresent(selected -> {
            if (AlertUtils.confirm("alert.delete.title", "alert.delete.content")) {
                context.subjectService().delete(selected.id());
            }
        });
    }
}

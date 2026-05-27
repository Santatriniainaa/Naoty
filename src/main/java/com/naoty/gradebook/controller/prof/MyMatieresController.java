package com.naoty.gradebook.controller.prof;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.ui.ControllerUtils;
import com.naoty.gradebook.ui.Modal;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MyMatieresController {

    private final AppContext context;

    @FXML private TableView<Subject> table;
    @FXML private TableColumn<Subject, String> colNom, colNiveau, colSemestre,
                                                  colFormation, colWeights, colGrades;
    @FXML private TableColumn<Subject, Number> colCoef;

    private final ObservableList<Subject> items = FXCollections.observableArrayList();

    public MyMatieresController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(c        -> new SimpleStringProperty(c.getValue().nom()));
        colNiveau.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().niveau().name()));
        colSemestre.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().semestre().name()));
        colFormation.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().formation().name()));
        colCoef.setCellValueFactory((TableColumn.CellDataFeatures<Subject, Number> c) ->
                (ObservableValue) new SimpleIntegerProperty(c.getValue().coefficient()));
        colWeights.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.0f / %.0f / %.0f",
                        c.getValue().weightDS() * 100,
                        c.getValue().weightTP() * 100,
                        c.getValue().weightExam() * 100)));
        colGrades.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(context.gradeService().findBySubjectId(c.getValue().id()).size())));

        table.setItems(items);
        reload();
        context.eventBus().subscribe("subjects.changed", e -> reload());
        context.eventBus().subscribe("grades.changed",   e -> table.refresh());
    }

    private void reload() {
        User prof = context.session().currentUser().orElse(null);
        if (prof == null) return;
        items.setAll(context.subjectService().findByProf(prof.id()));
    }

    @FXML
    private void onEnterGrades() {
        ControllerUtils.requireSelection(table).ifPresent(selected ->
                Modal.open(context, table.getScene().getWindow(),
                        "/fxml/prof/bulk-grade.fxml",
                        "/css/theme-" + context.currentTheme() + ".css",
                        (BulkGradeController c) -> c.setSubject(selected)));
    }

    @FXML
    private void onConfigureWeights() {
        ControllerUtils.requireSelection(table).ifPresent(selected ->
                Modal.open(context, table.getScene().getWindow(),
                        "/fxml/prof/weights-form.fxml",
                        "/css/theme-" + context.currentTheme() + ".css",
                        (WeightsFormController c) -> c.setSubject(selected)));
    }
}

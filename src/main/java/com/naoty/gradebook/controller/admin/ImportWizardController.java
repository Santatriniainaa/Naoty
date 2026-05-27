package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.service.CsvImportService.ImportPreview;
import com.naoty.gradebook.service.CsvImportService.ParsedRow;
import com.naoty.gradebook.service.CsvImportService.Target;
import com.naoty.gradebook.ui.AlertUtils;
import com.naoty.gradebook.ui.i18n.I18n;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.nio.file.Path;

public class ImportWizardController {

    private final AppContext context;
    private ImportPreview preview;

    @FXML private Label titleLabel, fileLabel, validLabel, invalidLabel, totalLabel;
    @FXML private TableView<ParsedRow> table;
    @FXML private TableColumn<ParsedRow, Number> colLine;
    @FXML private TableColumn<ParsedRow, String> colStatus, colSummary, colError;
    @FXML private Button importButton;

    private final ObservableList<ParsedRow> rows = FXCollections.observableArrayList();

    public ImportWizardController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        colLine.setCellValueFactory((TableColumn.CellDataFeatures<ParsedRow, Number> c) ->
                (ObservableValue) new SimpleIntegerProperty(c.getValue().lineNumber()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isValid() ? "✓ OK" : "⚠ Erreur"));
        colSummary.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().raw() == null ? "—" : String.join(" · ", c.getValue().raw())));
        colError.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().error() == null ? "" : c.getValue().error()));

        table.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(ParsedRow row, boolean empty) {
                super.updateItem(row, empty);
                getStyleClass().removeAll("row-valid", "row-invalid");
                if (!empty && row != null) {
                    getStyleClass().add(row.isValid() ? "row-valid" : "row-invalid");
                }
            }
        });

        table.setItems(rows);
        importButton.setDisable(true);
    }

    public void setSource(Target target, Path file) {
        titleLabel.setText(I18n.get("io.import.title", labelOf(target)));
        fileLabel.setText(file.toString());
        preview = context.csvImportService().preview(target, file);
        rows.setAll(preview.rows());

        long valid = preview.validCount();
        long invalid = preview.invalidCount();
        validLabel.setText(I18n.get("io.import.valid", valid));
        invalidLabel.setText(I18n.get("io.import.invalid", invalid));
        totalLabel.setText(I18n.get("io.import.total", preview.rows().size()));
        importButton.setDisable(valid == 0);
    }

    private static String labelOf(Target target) {
        return switch (target) {
            case PROFS    -> "professeurs";
            case STUDENTS -> "étudiants";
            case SUBJECTS -> "matières";
            case GRADES   -> "notes";
        };
    }

    @FXML
    private void onImport() {
        if (preview == null) return;
        if (!AlertUtils.confirm("io.import.confirm.title", "io.import.confirm.content")) return;

        int applied = context.csvImportService().apply(preview);
        AlertUtils.info("io.import.done.title",
                applied > 0 ? "io.import.done.content" : "io.import.done.empty");
        close();
    }

    @FXML private void onCancel() { close(); }
    private void close() { ((Stage) table.getScene().getWindow()).close(); }
}

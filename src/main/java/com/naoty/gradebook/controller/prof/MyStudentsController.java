package com.naoty.gradebook.controller.prof;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.ui.i18n.I18n;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Liste des étudiants à qui le prof connecté enseigne.
 * Un étudiant est inclus si le prof possède au moins une matière
 * dont le niveau == niveau de l'étudiant et dont la formation
 * couvre la formation de l'étudiant (BOTH couvre les deux).
 */
public class MyStudentsController {

    private final AppContext context;

    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, String> colId, colNom, colPrenom, colSexe,
                                                  colNiveau, colFormation, colStatus, colMatieres;
    @FXML private TextField searchField;
    @FXML private ComboBox<Niveau> niveauFilter;
    @FXML private ComboBox<Formation> formationFilter;
    @FXML private Label subtitleLabel;

    private final ObservableList<Student> items = FXCollections.observableArrayList();
    private Map<String, Long> subjectCountByStudent = Map.of();

    public MyStudentsController(AppContext context) {
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
        colMatieres.setCellValueFactory(c  -> new SimpleStringProperty(
                String.valueOf(subjectCountByStudent.getOrDefault(c.getValue().id(), 0L))));

        // Filtres dynamiques : on ne propose que les niveaux/formations
        // pour lesquels le prof a effectivement des matières.
        rebuildFilters();
        formationFilter.getItems().add(null);
        formationFilter.getItems().add(Formation.ACADEMIQUE);
        formationFilter.getItems().add(Formation.PROFESSIONNEL);

        table.setItems(items);
        reload();

        searchField.textProperty().addListener((o, a, b) -> reload());
        niveauFilter.valueProperty().addListener((o, a, b) -> reload());
        formationFilter.valueProperty().addListener((o, a, b) -> reload());

        context.eventBus().subscribe("students.changed", e -> reload());
        context.eventBus().subscribe("subjects.changed", e -> { rebuildFilters(); reload(); });
    }

    private void rebuildFilters() {
        User prof = context.session().currentUser().orElse(null);
        if (prof == null) return;

        Set<Niveau> teachingNiveaux = EnumSet.noneOf(Niveau.class);
        for (Subject s : context.subjectService().findByProf(prof.id())) {
            teachingNiveaux.add(s.niveau());
        }

        Niveau selected = niveauFilter.getValue();
        niveauFilter.getItems().setAll((Niveau) null);
        teachingNiveaux.stream().sorted().forEach(niveauFilter.getItems()::add);
        if (selected != null && teachingNiveaux.contains(selected)) {
            niveauFilter.setValue(selected);
        }
    }

    private void reload() {
        User prof = context.session().currentUser().orElse(null);
        if (prof == null) { items.clear(); return; }

        // Construit la cartographie niveau → set de formations enseignées
        Map<Niveau, Set<Formation>> teaching = new EnumMap<>(Niveau.class);
        for (Subject s : context.subjectService().findByProf(prof.id())) {
            Set<Formation> set = teaching.computeIfAbsent(s.niveau(), k -> EnumSet.noneOf(Formation.class));
            if (s.formation() == Formation.BOTH) {
                set.add(Formation.ACADEMIQUE);
                set.add(Formation.PROFESSIONNEL);
            } else {
                set.add(s.formation());
            }
        }

        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        Niveau niveauPick   = niveauFilter.getValue();
        Formation formPick  = formationFilter.getValue();

        List<Student> filtered = context.studentService().findAll().stream()
                .filter(s -> {
                    Set<Formation> formsAtNiveau = teaching.get(s.niveau());
                    return formsAtNiveau != null && formsAtNiveau.contains(s.formation());
                })
                .filter(s -> niveauPick  == null || s.niveau()    == niveauPick)
                .filter(s -> formPick    == null || s.formation() == formPick)
                .filter(s -> q.isEmpty()
                        || s.nom().toLowerCase().contains(q)
                        || s.prenom().toLowerCase().contains(q)
                        || s.id().toLowerCase().contains(q))
                .toList();

        // Pré-calcule le nombre de matières que le prof partage avec chaque étudiant
        Map<String, Long> counts = new java.util.HashMap<>();
        for (Student stu : filtered) {
            long c = context.subjectService().findByProf(prof.id()).stream()
                    .filter(sub -> sub.niveau() == stu.niveau()
                            && sub.formation().matches(stu.formation()))
                    .count();
            counts.put(stu.id(), c);
        }
        subjectCountByStudent = counts;

        items.setAll(filtered);
        subtitleLabel.setText(I18n.get("page.prof.students.subtitle.count", filtered.size()));
    }
}

package com.naoty.gradebook.controller.admin;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.Mention;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Dashboard admin : 4 stat cards + 2 graphiques.
 * <ul>
 *   <li>BarChart : moyenne semestrielle moyenne par niveau (L1→M2),
 *       moyennée sur S1 et S2 de chaque niveau.</li>
 *   <li>PieChart : répartition des mentions chez tous les étudiants
 *       (en utilisant leur moyenne annuelle).</li>
 * </ul>
 */
public class AdminDashboardController {

    private final AppContext context;

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalProfsLabel;
    @FXML private Label totalSubjectsLabel;
    @FXML private Label globalAverageLabel;
    @FXML private BarChart<String, Number> niveauChart;
    @FXML private CategoryAxis chartX;
    @FXML private NumberAxis chartY;
    @FXML private PieChart mentionsChart;

    public AdminDashboardController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        refresh();
        context.eventBus().subscribe("users.changed",    e -> refresh());
        context.eventBus().subscribe("students.changed", e -> refresh());
        context.eventBus().subscribe("subjects.changed", e -> refresh());
        context.eventBus().subscribe("grades.changed",   e -> refresh());
    }

    private void refresh() {
        totalStudentsLabel.setText(String.valueOf(context.studentService().findAll().size()));
        totalProfsLabel.setText(String.valueOf(context.userService().findProfs().size()));
        totalSubjectsLabel.setText(String.valueOf(context.subjectService().findAll().size()));

        context.statsService().globalAverage().ifPresentOrElse(
                avg -> globalAverageLabel.setText(String.format("%.2f / 20", avg)),
                ()  -> globalAverageLabel.setText("—"));

        updateNiveauChart();
        updateMentionsChart();
    }

    /** Moyenne annuelle moyenne par niveau. */
    private void updateNiveauChart() {
        Map<Niveau, double[]> sumCount = new EnumMap<>(Niveau.class);
        for (Niveau n : Niveau.values()) sumCount.put(n, new double[]{0, 0});

        for (Student s : context.studentService().findAll()) {
            OptionalDouble annual = context.statsService().annualAverage(s);
            if (annual.isPresent()) {
                double[] sc = sumCount.get(s.niveau());
                sc[0] += annual.getAsDouble();
                sc[1] += 1;
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Niveau n : Niveau.values()) {
            double[] sc = sumCount.get(n);
            double avg = sc[1] > 0 ? sc[0] / sc[1] : 0;
            series.getData().add(new XYChart.Data<>(n.name(), avg));
        }

        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();
        data.add(series);
        niveauChart.setData(data);
    }

    /** Répartition des mentions sur la moyenne annuelle des étudiants. */
    private void updateMentionsChart() {
        Map<Mention, Long> dist = new EnumMap<>(Mention.class);
        for (Mention m : Mention.values()) dist.put(m, 0L);

        for (Student s : context.studentService().findAll()) {
            OptionalDouble avg = context.statsService().annualAverage(s);
            avg.ifPresent(v -> dist.merge(context.statsService().mention(v), 1L, Long::sum));
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Mention m : Mention.values()) {
            long count = dist.getOrDefault(m, 0L);
            if (count > 0) {
                pieData.add(new PieChart.Data(prettyMention(m) + " (" + count + ")", count));
            }
        }
        if (pieData.isEmpty()) {
            // placeholder pour ne pas avoir un pie complètement vide
            pieData.add(new PieChart.Data("Aucune note", 1));
        }
        mentionsChart.setData(pieData);
    }

    private static String prettyMention(Mention m) {
        return switch (m) {
            case INSUFFISANT -> "Insuffisant";
            case PASSABLE    -> "Passable";
            case ASSEZ_BIEN  -> "Assez bien";
            case BIEN        -> "Bien";
            case TRES_BIEN   -> "Très bien";
            case EXCELLENT   -> "Excellent";
        };
    }

}

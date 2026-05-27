package com.naoty.gradebook.controller.prof;

import com.naoty.gradebook.AppContext;
import com.naoty.gradebook.model.Grade;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.ui.i18n.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfDashboardController {

    private final AppContext context;

    @FXML private Label welcomeLabel;
    @FXML private Label matieresLabel;
    @FXML private Label gradesLabel;
    @FXML private Label studentsLabel;

    public ProfDashboardController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        refresh();
        context.eventBus().subscribe("grades.changed",   e -> refresh());
        context.eventBus().subscribe("subjects.changed", e -> refresh());
    }

    private void refresh() {
        User prof = context.session().currentUser().orElse(null);
        if (prof == null) return;

        welcomeLabel.setText(I18n.get("dashboard.prof.welcome", prof.prenom()));

        List<Subject> mySubjects = context.subjectService().findByProf(prof.id());
        List<Grade> myGrades = context.gradeService().findByProfId(prof.id());
        Set<String> studentIds = new HashSet<>();
        for (Grade g : myGrades) studentIds.add(g.studentId());

        matieresLabel.setText(String.valueOf(mySubjects.size()));
        gradesLabel.setText(String.valueOf(myGrades.size()));
        studentsLabel.setText(String.valueOf(studentIds.size()));
    }
}

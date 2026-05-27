package com.naoty.gradebook.service;

import com.naoty.gradebook.model.Grade;
import com.naoty.gradebook.model.GradeType;
import com.naoty.gradebook.model.Mention;
import com.naoty.gradebook.model.Semestre;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.repository.GradeRepository;
import com.naoty.gradebook.repository.StudentRepository;
import com.naoty.gradebook.repository.SubjectRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Calculs statistiques pour l'application universitaire.
 *
 * <ul>
 *   <li>Moyenne par matière : pondérée par les poids DS/TP/EXAM
 *       configurés sur la matière. Les types absents sont ignorés
 *       et les poids restants se renormalisent.</li>
 *   <li>Moyenne semestrielle : pondérée par les coefficients des
 *       matières applicables à l'étudiant.</li>
 *   <li>Rang : dans la cohorte (mêmes niveau + formation).</li>
 * </ul>
 */
public class StatsService {

    private final StudentRepository students;
    private final SubjectRepository subjects;
    private final GradeRepository grades;
    private final Mention.MentionThresholds thresholds;

    public StatsService(StudentRepository students,
                        SubjectRepository subjects,
                        GradeRepository grades,
                        Mention.MentionThresholds thresholds) {
        this.students = students;
        this.subjects = subjects;
        this.grades = grades;
        this.thresholds = thresholds;
    }

    /** Moyenne par matière : moyenne pondérée par type, avec re-normalisation. */
    public OptionalDouble subjectAverage(String studentId, Subject subject) {
        Map<GradeType, List<Double>> byType = new EnumMap<>(GradeType.class);
        for (Grade g : grades.findByStudentId(studentId)) {
            if (!g.subjectId().equals(subject.id())) continue;
            byType.computeIfAbsent(g.type(), k -> new java.util.ArrayList<>()).add(g.value());
        }
        if (byType.isEmpty()) return OptionalDouble.empty();

        double weightedSum = 0;
        double weightTotal = 0;
        for (var entry : byType.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double w = subject.weight(entry.getKey());
            weightedSum += avg * w;
            weightTotal += w;
        }
        return weightTotal > 0 ? OptionalDouble.of(weightedSum / weightTotal) : OptionalDouble.empty();
    }

    /** Moyenne semestrielle pondérée par coefficient. */
    public OptionalDouble semestreAverage(Student student, Semestre semestre) {
        double weightedSum = 0;
        int totalCoef = 0;
        for (Subject sub : subjectsForStudent(student, semestre)) {
            OptionalDouble avg = subjectAverage(student.id(), sub);
            if (avg.isPresent()) {
                weightedSum += avg.getAsDouble() * sub.coefficient();
                totalCoef += sub.coefficient();
            }
        }
        return totalCoef > 0 ? OptionalDouble.of(weightedSum / totalCoef) : OptionalDouble.empty();
    }

    /** Moyenne annuelle d'un étudiant à son niveau courant. */
    public OptionalDouble annualAverage(Student student) {
        Semestre[] sems = Semestre.of(student.niveau());
        OptionalDouble s1 = semestreAverage(student, sems[0]);
        OptionalDouble s2 = semestreAverage(student, sems[1]);
        if (s1.isEmpty() && s2.isEmpty()) return OptionalDouble.empty();
        double sum = s1.orElse(0) + s2.orElse(0);
        int n = (s1.isPresent() ? 1 : 0) + (s2.isPresent() ? 1 : 0);
        return OptionalDouble.of(sum / n);
    }

    /** Moyenne globale toutes notes confondues (vue admin). */
    public OptionalDouble globalAverage() {
        return grades.findAll().stream().mapToDouble(Grade::value).average();
    }

    /**
     * Rang d'un étudiant dans sa cohorte (même niveau, même formation)
     * pour un semestre donné. 1 = major. -1 si étudiant introuvable.
     */
    public int rank(Student student, Semestre semestre) {
        List<Map.Entry<String, Double>> ranking = students.findAll().stream()
                .filter(s -> s.niveau() == student.niveau()
                        && s.formation() == student.formation())
                .map(s -> Map.entry(s.id(), semestreAverage(s, semestre).orElse(-1.0)))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList();
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getKey().equals(student.id())) return i + 1;
        }
        return -1;
    }

    public Mention mention(double moyenne) {
        return Mention.from(moyenne, thresholds);
    }

    /**
     * Moyenne par type d'évaluation (DS, TP, EXAM) pour un étudiant sur une matière.
     * Les types absents ne figurent pas dans la map retournée.
     */
    public Map<GradeType, Double> averagesByType(String studentId, String subjectId) {
        Map<GradeType, List<Double>> byType = new EnumMap<>(GradeType.class);
        for (Grade g : grades.findByStudentId(studentId)) {
            if (!g.subjectId().equals(subjectId)) continue;
            byType.computeIfAbsent(g.type(), k -> new java.util.ArrayList<>()).add(g.value());
        }
        Map<GradeType, Double> result = new EnumMap<>(GradeType.class);
        byType.forEach((type, values) ->
                result.put(type, values.stream().mapToDouble(Double::doubleValue).average().orElse(0)));
        return result;
    }

    private List<Subject> subjectsForStudent(Student student, Semestre semestre) {
        return subjects.findAll().stream()
                .filter(s -> s.niveau() == student.niveau())
                .filter(s -> s.semestre() == semestre)
                .filter(s -> s.formation().matches(student.formation()))
                .toList();
    }
}

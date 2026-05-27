package com.naoty.gradebook.service;

import com.naoty.gradebook.model.Formation;
import com.naoty.gradebook.model.Gender;
import com.naoty.gradebook.model.Grade;
import com.naoty.gradebook.model.GradeType;
import com.naoty.gradebook.model.Mention;
import com.naoty.gradebook.model.Niveau;
import com.naoty.gradebook.model.Semestre;
import com.naoty.gradebook.model.Status;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.model.Subject;
import com.naoty.gradebook.repository.GradeRepository;
import com.naoty.gradebook.repository.StudentRepository;
import com.naoty.gradebook.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatsServiceTest {

    private StudentRepository students;
    private SubjectRepository subjects;
    private GradeRepository grades;
    private StatsService stats;

    private final Mention.MentionThresholds defaults =
            new Mention.MentionThresholds(10, 12, 14, 16, 18);

    @BeforeEach
    void setup() {
        students = mock(StudentRepository.class);
        subjects = mock(SubjectRepository.class);
        grades   = mock(GradeRepository.class);
        stats = new StatsService(students, subjects, grades, defaults);
    }

    private Subject subject(String id, int coef, double ds, double tp, double exam) {
        return new Subject(id, id, coef, Niveau.L1, Semestre.S1, Formation.BOTH,
                "PROF-001", ds, tp, exam);
    }

    private Grade grade(String stu, String sub, GradeType type, double value) {
        return new Grade("g-" + stu + "-" + sub + "-" + type,
                stu, sub, "PROF-001", type, value, LocalDate.now());
    }

    private Student student(String id, Niveau n, Formation f) {
        return new Student(id, id, id, Gender.F, n, f, Status.PASSANT);
    }

    @Test
    void subjectAverage_pondereParPoidsDsTpExam() {
        Subject math = subject("MATH", 4, 0.30, 0.20, 0.50);
        when(grades.findByStudentId("S1")).thenReturn(List.of(
                grade("S1", "MATH", GradeType.DS,   12),
                grade("S1", "MATH", GradeType.TP,   18),
                grade("S1", "MATH", GradeType.EXAM, 14)
        ));
        // 12*0.30 + 18*0.20 + 14*0.50 = 3.6 + 3.6 + 7.0 = 14.2
        OptionalDouble avg = stats.subjectAverage("S1", math);
        assertThat(avg).isPresent();
        assertThat(avg.getAsDouble()).isCloseTo(14.2, within(0.001));
    }

    @Test
    void subjectAverage_renormaliseSiTypeManquant() {
        Subject math = subject("MATH", 4, 0.30, 0.20, 0.50);
        when(grades.findByStudentId("S1")).thenReturn(List.of(
                grade("S1", "MATH", GradeType.DS,   10),
                grade("S1", "MATH", GradeType.EXAM, 20)
        ));
        // (10*0.30 + 20*0.50) / (0.30 + 0.50) = 13 / 0.8 = 16.25
        OptionalDouble avg = stats.subjectAverage("S1", math);
        assertThat(avg.getAsDouble()).isCloseTo(16.25, within(0.001));
    }

    @Test
    void semestreAverage_pondereParCoefficients() {
        Subject math = subject("MATH", 4, 0.30, 0.20, 0.50);
        Subject ang  = subject("ANG",  2, 0.50, 0.00, 0.50);
        when(subjects.findAll()).thenReturn(List.of(math, ang));

        Student s = student("S1", Niveau.L1, Formation.ACADEMIQUE);
        when(grades.findByStudentId("S1")).thenReturn(List.of(
                grade("S1", "MATH", GradeType.DS,   16),
                grade("S1", "MATH", GradeType.EXAM, 16),
                grade("S1", "ANG",  GradeType.DS,   10),
                grade("S1", "ANG",  GradeType.EXAM, 10)
        ));
        // math = 16 (uniforme), ang = 10
        // (16*4 + 10*2) / 6 = 84/6 = 14.0
        OptionalDouble avg = stats.semestreAverage(s, Semestre.S1);
        assertThat(avg.getAsDouble()).isCloseTo(14.0, within(0.001));
    }

    @Test
    void mention_seuilsCorrects() {
        assertThat(stats.mention(9.99)).isEqualTo(Mention.INSUFFISANT);
        assertThat(stats.mention(10.0)).isEqualTo(Mention.PASSABLE);
        assertThat(stats.mention(13.5)).isEqualTo(Mention.ASSEZ_BIEN);
        assertThat(stats.mention(15.0)).isEqualTo(Mention.BIEN);
        assertThat(stats.mention(17.5)).isEqualTo(Mention.TRES_BIEN);
        assertThat(stats.mention(18.0)).isEqualTo(Mention.EXCELLENT);
    }

    @Test
    void rank_classeParCohorteNiveauPlusFormation() {
        Student alice = student("S1", Niveau.L1, Formation.ACADEMIQUE);
        Student bob   = student("S2", Niveau.L1, Formation.ACADEMIQUE);
        Student carol = student("S3", Niveau.L1, Formation.PROFESSIONNEL); // autre cohorte
        when(students.findAll()).thenReturn(List.of(alice, bob, carol));

        Subject math = subject("MATH", 1, 1.0, 0.0, 0.0);
        when(subjects.findAll()).thenReturn(List.of(math));

        when(grades.findByStudentId("S1")).thenReturn(List.of(grade("S1","MATH",GradeType.DS,18)));
        when(grades.findByStudentId("S2")).thenReturn(List.of(grade("S2","MATH",GradeType.DS,12)));
        when(grades.findByStudentId("S3")).thenReturn(List.of(grade("S3","MATH",GradeType.DS,20)));

        // Alice (18) > Bob (12) dans la cohorte L1-ACADEMIQUE
        assertThat(stats.rank(alice, Semestre.S1)).isEqualTo(1);
        assertThat(stats.rank(bob,   Semestre.S1)).isEqualTo(2);
        // Carol seule dans sa cohorte L1-PROFESSIONNEL
        assertThat(stats.rank(carol, Semestre.S1)).isEqualTo(1);
    }

    @Test
    void formation_BOTH_matchAcademiqueEtProfessionnel() {
        Subject sharedSubject = new Subject("X", "X", 1, Niveau.L1, Semestre.S1,
                Formation.BOTH, "PROF-001", 1.0, 0.0, 0.0);
        Student acad = student("Sa", Niveau.L1, Formation.ACADEMIQUE);
        Student pro  = student("Sp", Niveau.L1, Formation.PROFESSIONNEL);

        when(subjects.findAll()).thenReturn(List.of(sharedSubject));
        when(grades.findByStudentId("Sa")).thenReturn(List.of(grade("Sa","X",GradeType.DS,15)));
        when(grades.findByStudentId("Sp")).thenReturn(List.of(grade("Sp","X",GradeType.DS,10)));

        assertThat(stats.semestreAverage(acad, Semestre.S1).getAsDouble()).isEqualTo(15.0);
        assertThat(stats.semestreAverage(pro,  Semestre.S1).getAsDouble()).isEqualTo(10.0);
    }
}

package com.naoty.gradebook;

import com.naoty.gradebook.auth.AuthService;
import com.naoty.gradebook.auth.Session;
import com.naoty.gradebook.config.AppConfig;
import com.naoty.gradebook.event.EventBus;
import javafx.stage.Stage;
import com.naoty.gradebook.repository.GradeRepository;
import com.naoty.gradebook.repository.StudentRepository;
import com.naoty.gradebook.repository.SubjectRepository;
import com.naoty.gradebook.repository.UserRepository;
import com.naoty.gradebook.repository.csv.CsvGradeRepository;
import com.naoty.gradebook.repository.csv.CsvStudentRepository;
import com.naoty.gradebook.repository.csv.CsvSubjectRepository;
import com.naoty.gradebook.repository.csv.CsvUserRepository;
import com.naoty.gradebook.service.BulletinService;
import com.naoty.gradebook.service.CsvImportService;
import com.naoty.gradebook.service.GradeService;
import com.naoty.gradebook.service.SeedService;
import com.naoty.gradebook.service.StatsService;
import com.naoty.gradebook.service.StudentService;
import com.naoty.gradebook.service.SubjectService;
import com.naoty.gradebook.service.UserService;
import com.naoty.gradebook.validation.GradeValidator;
import com.naoty.gradebook.validation.StudentValidator;
import com.naoty.gradebook.validation.SubjectValidator;
import com.naoty.gradebook.validation.UserValidator;

/**
 * Container DI manuel. Une seule instance partagée pour toute l'app.
 */
public class AppContext {

    private final AppConfig config;
    private final EventBus eventBus;
    private final Session session;
    private final AuthService authService;
    private Stage primaryStage;
    private String currentTheme;

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;

    private final UserService userService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final GradeService gradeService;
    private final StatsService statsService;
    private final BulletinService bulletinService;
    private final SeedService seedService;
    private final CsvImportService csvImportService;

    public AppContext() {
        this.config = new AppConfig();
        this.eventBus = new EventBus();
        this.session = new Session();
        this.currentTheme = config.theme();

        this.userRepository    = new CsvUserRepository(config.usersFile(),    config.backupDir());
        this.studentRepository = new CsvStudentRepository(config.studentsFile(), config.backupDir());
        this.subjectRepository = new CsvSubjectRepository(config.subjectsFile(), config.backupDir());
        this.gradeRepository   = new CsvGradeRepository(config.gradesFile(),   config.backupDir());

        this.authService = new AuthService(config, userRepository, session);

        this.userService    = new UserService(userRepository, new UserValidator(), eventBus);
        this.studentService = new StudentService(studentRepository, new StudentValidator(), eventBus);
        this.subjectService = new SubjectService(subjectRepository, new SubjectValidator(), eventBus);
        this.gradeService   = new GradeService(gradeRepository, new GradeValidator(), eventBus);
        this.statsService   = new StatsService(studentRepository, subjectRepository, gradeRepository,
                                               config.mentionThresholds());
        this.bulletinService = new BulletinService(subjectService, statsService);
        this.seedService      = new SeedService(config);
        this.csvImportService = new CsvImportService(this);
    }

    public AppConfig config()                  { return config; }
    public EventBus eventBus()                 { return eventBus; }
    public Session session()                   { return session; }
    public AuthService authService()           { return authService; }
    public Stage primaryStage()                { return primaryStage; }
    public void setPrimaryStage(Stage stage)   { this.primaryStage = stage; }
    public String currentTheme()               { return currentTheme; }
    public void setCurrentTheme(String theme)  { this.currentTheme = theme; }

    public UserService userService()           { return userService; }
    public StudentService studentService()     { return studentService; }
    public SubjectService subjectService()     { return subjectService; }
    public GradeService gradeService()         { return gradeService; }
    public StatsService statsService()         { return statsService; }
    public BulletinService bulletinService()   { return bulletinService; }
    public SeedService seedService()           { return seedService; }
    public CsvImportService csvImportService() { return csvImportService; }
}

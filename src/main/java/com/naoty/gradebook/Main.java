package com.naoty.gradebook;

import com.naoty.gradebook.ui.SceneRouter;
import com.naoty.gradebook.ui.i18n.I18n;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private AppContext context;

    @Override
    public void init() {
        this.context = new AppContext();
        context.seedService().seedIfEmpty();
        I18n.setLocale(context.config().locale());
        log.info("Bootstrap : {} profs, {} étudiants, {} matières, {} notes",
                context.userService().findProfs().size(),
                context.studentService().findAll().size(),
                context.subjectService().findAll().size(),
                context.gradeService().findAll().size());
    }

    @Override
    public void start(Stage stage) {
        context.setPrimaryStage(stage);
        stage.setTitle(I18n.get("app.title"));
        stage.setMinWidth(420);
        stage.setMinHeight(520);
        SceneRouter.toLogin(context);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

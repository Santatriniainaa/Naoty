package com.naoty.gradebook.config;

import com.naoty.gradebook.model.Mention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private final Properties props = new Properties();

    public AppConfig() {
        try (InputStream in = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (in == null) {
                log.warn("application.properties not found on classpath, using defaults");
            } else {
                props.load(in);
            }
        } catch (IOException e) {
            log.error("Failed to load application.properties", e);
        }
    }

    public Path dataDir()      { return Paths.get(get("data.dir", "data")); }
    public Path backupDir()    { return Paths.get(get("data.backup.dir", "data/backup")); }
    public Path usersFile()    { return dataDir().resolve(get("data.file.users",    "users.csv")); }
    public Path studentsFile() { return dataDir().resolve(get("data.file.students", "students.csv")); }
    public Path subjectsFile() { return dataDir().resolve(get("data.file.subjects", "subjects.csv")); }
    public Path gradesFile()   { return dataDir().resolve(get("data.file.grades",   "grades.csv")); }

    public String adminCode() { return get("auth.admin.code", "ADMIN"); }

    public Locale locale() {
        return Locale.forLanguageTag(get("app.locale", "fr-FR"));
    }

    public String theme() {
        return get("app.theme", "light");
    }

    public Mention.MentionThresholds mentionThresholds() {
        return new Mention.MentionThresholds(
                getDouble("mention.passable",   10.0),
                getDouble("mention.assezBien",  12.0),
                getDouble("mention.bien",       14.0),
                getDouble("mention.tresBien",   16.0),
                getDouble("mention.excellent",  18.0)
        );
    }

    private String get(String key, String def)        { return props.getProperty(key, def); }
    private double getDouble(String key, double def)  { return Double.parseDouble(get(key, String.valueOf(def))); }
}

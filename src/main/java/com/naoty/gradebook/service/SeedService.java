package com.naoty.gradebook.service;

import com.naoty.gradebook.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Copie des CSV d'exemple dans le dossier data/ si celui-ci est vide.
 *
 * <p>Cherche les fichiers à deux endroits :
 * <ul>
 *   <li>{@code data-sample/} à la racine (mode dev)</li>
 *   <li>resources classpath {@code /data-sample/} (mode jar)</li>
 * </ul>
 */
public class SeedService {

    private static final Logger log = LoggerFactory.getLogger(SeedService.class);
    private static final List<String> SEED_FILES = List.of(
            "users.csv", "students.csv", "subjects.csv", "grades.csv"
    );

    private final AppConfig config;

    public SeedService(AppConfig config) {
        this.config = config;
    }

    /**
     * Copie chaque fichier d'exemple manquant individuellement.
     * Permet de récupérer un fichier oublié (ex. users.csv) sans toucher
     * aux autres CSV déjà présents.
     */
    public void seedIfEmpty() {
        Path dataDir = config.dataDir();
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            log.warn("Cannot create data directory {}", dataDir, e);
            return;
        }

        Path projectSample = Path.of("data-sample");
        boolean fromFs = Files.isDirectory(projectSample);
        int copied = 0;

        for (String name : SEED_FILES) {
            Path dst = dataDir.resolve(name);
            if (Files.exists(dst)) continue;   // ne pas écraser les CSV existants

            if (fromFs) {
                Path src = projectSample.resolve(name);
                if (!Files.exists(src)) continue;
                try {
                    Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                } catch (IOException e) {
                    log.warn("Failed to copy {} → {}", src, dst, e);
                }
            } else {
                try (InputStream in = getClass().getResourceAsStream("/data-sample/" + name)) {
                    if (in == null) continue;
                    Files.copy(in, dst, StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                } catch (IOException e) {
                    log.warn("Failed to copy classpath:/data-sample/{}", name, e);
                }
            }
        }

        if (copied > 0) {
            log.info("Seeded {} missing CSV file(s) into {}", copied, dataDir);
        }
    }
}

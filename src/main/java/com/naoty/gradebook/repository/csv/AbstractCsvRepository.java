package com.naoty.gradebook.repository.csv;

import com.naoty.gradebook.exception.PersistenceException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Base commune à toutes les impls CSV. Charge en mémoire au démarrage,
 * réécrit le fichier en entier à chaque save/delete (taille modeste OK).
 *
 * <p>Stratégie de robustesse :
 * <ul>
 *   <li>backup .bak horodaté avant chaque écriture</li>
 *   <li>ligne corrompue : log WARN + skip, pas de crash</li>
 *   <li>fichier inexistant : créé vide à la première écriture</li>
 * </ul>
 */
public abstract class AbstractCsvRepository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(AbstractCsvRepository.class);
    private static final DateTimeFormatter BACKUP_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path file;
    private final Path backupDir;
    private final CsvMapper<T> mapper;
    private final Function<T, ID> idExtractor;
    private final List<T> cache = new ArrayList<>();
    private boolean loaded = false;

    protected AbstractCsvRepository(Path file, Path backupDir, CsvMapper<T> mapper, Function<T, ID> idExtractor) {
        this.file = file;
        this.backupDir = backupDir;
        this.mapper = mapper;
        this.idExtractor = idExtractor;
    }

    public synchronized List<T> findAll() {
        ensureLoaded();
        return List.copyOf(cache);
    }

    public synchronized Optional<T> findById(ID id) {
        ensureLoaded();
        return cache.stream().filter(e -> idExtractor.apply(e).equals(id)).findFirst();
    }

    public synchronized T save(T entity) {
        ensureLoaded();
        ID id = idExtractor.apply(entity);
        cache.removeIf(e -> idExtractor.apply(e).equals(id));
        cache.add(entity);
        persist();
        return entity;
    }

    public synchronized void deleteById(ID id) {
        ensureLoaded();
        boolean removed = cache.removeIf(e -> idExtractor.apply(e).equals(id));
        if (removed) persist();
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(file)) {
            log.info("CSV file {} not found, starting empty", file);
            return;
        }
        try (Reader reader = Files.newBufferedReader(file);
             CSVReader csv = new CSVReader(reader)) {
            String[] header = csv.readNext();
            if (header == null) {
                log.warn("CSV file {} is empty", file);
                return;
            }
            String[] row;
            int lineNum = 1;
            while ((row = csv.readNext()) != null) {
                lineNum++;
                try {
                    cache.add(mapper.fromRow(row));
                } catch (Exception ex) {
                    log.warn("Skipping corrupted row {} in {}: {}", lineNum, file, ex.getMessage());
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new PersistenceException("Failed to read " + file, e);
        }
    }

    private void persist() {
        try {
            Files.createDirectories(file.getParent());
            backup();
            try (Writer writer = Files.newBufferedWriter(file);
                 CSVWriter csv = new CSVWriter(writer)) {
                csv.writeNext(mapper.headers());
                for (T entity : cache) {
                    csv.writeNext(mapper.toRow(entity));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to write " + file, e);
        }
    }

    private void backup() throws IOException {
        if (!Files.exists(file)) return;
        Files.createDirectories(backupDir);
        String stamp = LocalDateTime.now().format(BACKUP_FMT);
        Path bak = backupDir.resolve(file.getFileName() + "." + stamp + ".bak");
        Files.copy(file, bak, StandardCopyOption.REPLACE_EXISTING);
    }
}

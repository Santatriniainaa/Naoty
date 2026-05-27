package com.naoty.gradebook.repository.csv;

import java.util.List;

/**
 * Conversion entre une ligne CSV (List&lt;String&gt;) et une entité.
 * Une impl par type d'entité. Centralise les colonnes et l'ordre.
 */
public interface CsvMapper<T> {
    String[] headers();
    String[] toRow(T entity);
    T fromRow(String[] row);
}

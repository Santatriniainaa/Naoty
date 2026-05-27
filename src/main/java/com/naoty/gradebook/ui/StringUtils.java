package com.naoty.gradebook.ui;

public final class StringUtils {

    private StringUtils() {}

    /** Retourne la chaîne trimée, ou "" si null. Utilisé pour normaliser les entrées de formulaire. */
    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

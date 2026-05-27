package com.naoty.gradebook.ui.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {

    private static Locale currentLocale = Locale.FRANCE;
    private static ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public static Locale currentLocale() {
        return currentLocale;
    }

    public static String get(String key) {
        if (!bundle.containsKey(key)) return key;
        return bundle.getString(key);
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}

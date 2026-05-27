package com.naoty.gradebook.controller;

import com.naoty.gradebook.AppContext;
import javafx.util.Callback;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Fournit les controllers à FXMLLoader. Chaque controller doit accepter
 * un AppContext dans son constructeur, ou avoir un constructeur sans argument.
 */
public class ControllerFactory implements Callback<Class<?>, Object> {

    private final AppContext context;

    public ControllerFactory(AppContext context) {
        this.context = context;
    }

    @Override
    public Object call(Class<?> type) {
        try {
            for (Constructor<?> c : type.getDeclaredConstructors()) {
                if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == AppContext.class) {
                    return c.newInstance(context);
                }
            }
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Cannot instantiate controller " + type, e);
        }
    }
}

package com.naoty.gradebook;

/**
 * Point d'entrée pour le fat jar.
 * Une classe distincte qui n'étend PAS Application est nécessaire,
 * sinon le module JavaFX refuse de se charger depuis un jar non modulaire.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}

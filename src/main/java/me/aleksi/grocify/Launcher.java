package me.aleksi.grocify;

/**
 * Grocify launcher.
 *
 * <p>Uses a separate class so that the mainClass doesn't need JavaFX Application.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class Launcher {
    /**
     * Print author as per assignment and start Grocify.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("Author: Aleksi Kervinen");
        GrocifyFx.main(args);
    }
}

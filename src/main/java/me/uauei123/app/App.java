package me.uauei123.app;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Esegui nell'EDT
        SwingUtilities.invokeLater(() -> {
            GUI app = new GUI();
            app.inizializzaInterfaccia();
        });
    }
}

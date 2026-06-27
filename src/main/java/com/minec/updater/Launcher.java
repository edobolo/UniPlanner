package com.minec.updater;

import org.update4j.Configuration;
import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Launcher {

    private JFrame splashFrame;
    private JLabel testoStato;
    private JProgressBar barraProgresso;
    private String jarDaAvviare;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Launcher launcher = new Launcher();
            launcher.mostraSchermata();
            launcher.iniziaAggiornamento();
        });
    }

    // --- IL NOSTRO RADAR PER INTERNET ---
    private static boolean isInternetAvailable() {
        try {
            java.net.URLConnection connection = new java.net.URI("http://www.google.com").toURL().openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void mostraSchermata() {
        // Creiamo la finestra
        splashFrame = new JFrame();
        splashFrame.setUndecorated(true);
        splashFrame.setSize(400, 150);
        splashFrame.setLocationRelativeTo(null); // Centra sullo schermo
        splashFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Pannello principale con un po' di margine
        JPanel pannello = new JPanel(new BorderLayout(10, 10));
        pannello.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pannello.setBackground(new Color(240, 248, 255));

        // Titolo dell'App
        JLabel titolo = new JLabel("UniPlanner", SwingConstants.CENTER);
        titolo.setFont(new Font("Arial", Font.BOLD, 28));
        titolo.setForeground(new Color(33, 150, 243));
        pannello.add(titolo, BorderLayout.NORTH);

        // Testo che cambierà per dire all'utente cosa sta succedendo
        testoStato = new JLabel("Inizializzazione in corso...", SwingConstants.CENTER);
        testoStato.setFont(new Font("Arial", Font.PLAIN, 14));
        pannello.add(testoStato, BorderLayout.CENTER);

        // Barra di caricamento infinita
        barraProgresso = new JProgressBar();
        barraProgresso.setIndeterminate(true);
        pannello.add(barraProgresso, BorderLayout.SOUTH);

        splashFrame.add(pannello);
        splashFrame.setVisible(true);
    }

    private void iniziaAggiornamento() {
        new Thread(() -> {
            try {
                // 1. CONTROLLO CONNESSIONE
                if (isInternetAvailable()) {
                    aggiornaTesto("Ricerca aggiornamenti su GitHub...");
                    URL configUrl = new java.net.URI(
                            "https://raw.githubusercontent.com/edobolo/UniPlanner/main/aggiornamenti/config.xml").toURL();

                    java.net.URLConnection connessione = configUrl.openConnection();
                    connessione.setUseCaches(false); // Anti-cache

                    Configuration config;
                    try (Reader lettoreInternet = new InputStreamReader(connessione.getInputStream(),
                            StandardCharsets.UTF_8)) {
                        config = Configuration.read(lettoreInternet);
                    }

                    aggiornaTesto("Download degli aggiornamenti in corso...");
                    config.launch();

                } else {
                    // --- SIAMO OFFLINE ---
                    aggiornaTesto("Avvio in modalità offline...");

                    // Usiamo invokeAndWait per fermare temporaneamente il thread e aspettare
                    // che l'utente clicchi "OK" sul nostro messaggio moderno
                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            com.minec.schermate.DialoghiModerni.mostraMessaggio(
                                    splashFrame,
                                    "Modalità Offline",
                                    "Nessuna connessione rilevata. UniPlanner verrà avviato in modalità offline (aggiornamenti disabilitati).",
                                    false);
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                // 2. AVVIO DELL'APP PRINCIPALE
                lanciaAppPrincipale();

            } catch (Exception e) {
                // SE QUALCOSA VA STORTO (es. GitHub non risponde nonostante internet ci sia)
                e.printStackTrace();
                aggiornaTesto("Errore durante l'aggiornamento. Avvio in corso...");

                try {
                    SwingUtilities.invokeAndWait(() -> {
                        com.minec.schermate.DialoghiModerni.mostraMessaggio(
                                splashFrame,
                                "Errore di Connessione",
                                "Impossibile scaricare gli aggiornamenti in questo momento. L'app verrà avviata normalmente.",
                                true);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // Anche se va in errore, non blocchiamo l'utente ma lanciamo l'app!
                lanciaAppPrincipale();
            }
        }).start();
    }

    // --- METODO SEPARATO PER AVVIARE L'APP ---
    private void lanciaAppPrincipale() {
        aggiornaTesto("Preparazione avvio...");

        // 1. Determiniamo quale file avviare
        jarDaAvviare = "UniPlanner_Aggiornato.jar";
        java.io.File fileAggiornato = new java.io.File(jarDaAvviare);

        if (!fileAggiornato.exists()) {
            jarDaAvviare = "UniPlanner.jar";
        }

        // --- CONTROLLO CRITICO: Il file esiste? ---
        if (!new java.io.File(jarDaAvviare).exists()) {
            SwingUtilities.invokeLater(() -> {
                com.minec.schermate.DialoghiModerni.mostraMessaggio(
                        null,
                        "Errore di Avvio",
                        "Il file '" + jarDaAvviare + "' non è stato trovato.\n",
                        true);
                System.exit(0);
            });
            return;
        }

        aggiornaTesto("Avvio di UniPlanner...");

        // Fermiamo l'animazione della barra e la riempiamo al 100%
        SwingUtilities.invokeLater(() -> {
            barraProgresso.setIndeterminate(false);
            barraProgresso.setValue(100);
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        // Chiudiamo la schermata di caricamento
        SwingUtilities.invokeLater(() -> splashFrame.dispose());

        try {
            // Troviamo il percorso di Java
            String javaBin = System.getProperty("java.home") + java.io.File.separator + "bin"
                    + java.io.File.separator + "java";

            // Creiamo il comando per far partire l'app vera e propria
            ProcessBuilder pb = new ProcessBuilder(
                    javaBin,
                    "-cp", jarDaAvviare,
                    "com.minec.MainApp");

            pb.start();
            System.exit(0); 

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Errore fatale durante l'esecuzione del processo: " + e.getMessage());
            });
        }
    }

    // Metodo per aggiornare il testo sulla grafica
    private void aggiornaTesto(String testo) {
        SwingUtilities.invokeLater(() -> testoStato.setText(testo));
    }
}
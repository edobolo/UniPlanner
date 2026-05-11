package com.minec.updater;

import org.update4j.Configuration;
import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Launcher {

    // Elementi grafici della nostra schermata di caricamento
    private JFrame splashFrame;
    private JLabel testoStato;
    private JProgressBar barraProgresso;

    public static void main(String[] args) {
        // Avviamo la grafica sul thread apposito di Java (Swing)
        SwingUtilities.invokeLater(() -> {
            Launcher launcher = new Launcher();
            launcher.mostraSchermata();
            launcher.iniziaAggiornamento();
        });
    }

    private void mostraSchermata() {
        // Creiamo la finestra (senza i classici bordi con la X per chiudere)
        splashFrame = new JFrame();
        splashFrame.setUndecorated(true);
        splashFrame.setSize(400, 150);
        splashFrame.setLocationRelativeTo(null); // Centra sullo schermo
        splashFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Pannello principale con un po' di margine
        JPanel pannello = new JPanel(new BorderLayout(10, 10));
        pannello.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pannello.setBackground(new Color(240, 248, 255)); // Un celestino molto chiaro

        // Titolo dell'App
        JLabel titolo = new JLabel("UniPlanner", SwingConstants.CENTER);
        titolo.setFont(new Font("Arial", Font.BOLD, 28));
        titolo.setForeground(new Color(33, 150, 243)); // Blu stile materiale
        pannello.add(titolo, BorderLayout.NORTH);

        // Testo che cambierà per dire all'utente cosa sta succedendo
        testoStato = new JLabel("Inizializzazione in corso...", SwingConstants.CENTER);
        testoStato.setFont(new Font("Arial", Font.PLAIN, 14));
        pannello.add(testoStato, BorderLayout.CENTER);

        // Barra di caricamento infinita (animata da destra a sinistra)
        barraProgresso = new JProgressBar();
        barraProgresso.setIndeterminate(true);
        pannello.add(barraProgresso, BorderLayout.SOUTH);

        splashFrame.add(pannello);
        splashFrame.setVisible(true);
    }

    private void iniziaAggiornamento() {
        // Creiamo un "Lavoratore" (Thread) che fa il lavoro sporco in background
        // così la grafica della finestra non si blocca
        new Thread(() -> {
            try {
                // 1. Lettura della mappa online
                aggiornaTesto("Ricerca aggiornamenti su GitHub...");
                URL configUrl = new URL(
                        "https://raw.githubusercontent.com/edobolo/UniPlanner/main/aggiornamenti/config.xml");

                Configuration config;
                try (Reader lettoreInternet = new InputStreamReader(configUrl.openStream(), StandardCharsets.UTF_8)) {
                    config = Configuration.read(lettoreInternet);
                }

                // 2. Download (Se ci sono file nuovi, li scarica ora)
                aggiornaTesto("Download degli aggiornamenti in corso...");
                config.update();

                // 3. Avvio Completato
                aggiornaTesto("Avvio di UniPlanner...");

                // Fermiamo l'animazione della barra e la riempiamo al 100%
                SwingUtilities.invokeLater(() -> {
                    barraProgresso.setIndeterminate(false);
                    barraProgresso.setValue(100);
                });

                // Piccolo ritardo opzionale giusto per far leggere "Avvio" all'utente
                Thread.sleep(500);

                // Chiudiamo la schermata di caricamento
                splashFrame.dispose();

                // 4. Lanciamo il vero UniPlanner.jar!
                config.launch();

            } catch (Exception e) {
                // Se non c'è internet o il link è sbagliato, lo diciamo e proviamo ad avviare
                // offline
                e.printStackTrace();
                aggiornaTesto("Nessuna connessione. Avvio offline...");

                try {
                    Thread.sleep(1500); // Lasciamo il tempo di leggere l'errore
                    splashFrame.dispose();
                    // (Qui potresti inserire il codice per leggere il config.xml dal disco fisso
                    // locale)
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    // Un piccolo metodo di comodità per aggiornare il testo sulla grafica in modo
    // sicuro
    private void aggiornaTesto(String testo) {
        SwingUtilities.invokeLater(() -> testoStato.setText(testo));
    }
}
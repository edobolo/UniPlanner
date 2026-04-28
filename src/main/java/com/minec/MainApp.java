package com.minec;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import com.minec.dati.GestoreDatabase;
import com.minec.schermate.PannelloAggiungi;
import com.minec.schermate.PannelloPomodoro;
import com.minec.schermate.PannelloScadenze;
import com.minec.schermate.PannelloVoti;


public class MainApp {
    private static final String ICONA_APP = "icone/u.svg";

    public static void main(String[] args) {
        // Leggiamo la memoria
        if (com.minec.dati.GestoreDati.isTemaScuro()) {
            com.formdev.flatlaf.FlatDarkLaf.setup(); // Tema Scuro
        } else {
            com.formdev.flatlaf.FlatLightLaf.setup(); // Tema Chiaro
        }
        GestoreNotifiche.avviaNotifiche(ICONA_APP);
        com.minec.dati.GestoreDatabase.inizializzaDatabase();
        SwingUtilities.invokeLater(() -> creaEmostraGUI());
    }

    private static void creaEmostraGUI() {
        // 1. CREIAMO LA FINESTRA BASE
        JFrame finestra = new JFrame("UniPlanner");
        finestra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        finestra.setMinimumSize(new Dimension(970, 730));
        finestra.setSize(970, 730);
        finestra.setLocationRelativeTo(null);
        try {
            finestra.setIconImages(FlatSVGUtils.createWindowIconImages("/icone/u.svg"));
        } catch (Exception e) {
        }

        // 2. CREIAMO LO SPLASH SCREEN
        JPanel splashPanel = new JPanel(new GridBagLayout());
        splashPanel.setBackground(Color.WHITE);
        splashPanel.setOpaque(true);

        JPanel contenitoreSplash = new JPanel();
        contenitoreSplash.setLayout(new javax.swing.BoxLayout(contenitoreSplash, javax.swing.BoxLayout.Y_AXIS));
        contenitoreSplash.setOpaque(false);

        try {
            javax.swing.JLabel lblIcona = new javax.swing.JLabel(new FlatSVGIcon("icone/u.svg", 120, 120));
            lblIcona.setAlignmentX(javax.swing.JLabel.CENTER_ALIGNMENT);
            contenitoreSplash.add(lblIcona);
        } catch (Exception e) {
            javax.swing.JLabel lblFallback = new javax.swing.JLabel("U", javax.swing.SwingConstants.CENTER);
            lblFallback.setFont(new Font("Arial", Font.BOLD, 80));
            lblFallback.setAlignmentX(javax.swing.JLabel.CENTER_ALIGNMENT);
            contenitoreSplash.add(lblFallback);
        }

        contenitoreSplash.add(javax.swing.Box.createRigidArea(new Dimension(0, 30)));

        // --- LA NUOVA BARRA A PERCENTUALE ---
        javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar(0, 100);
        progressBar.setIndeterminate(false); // Ora è una barra misurabile
        progressBar.setValue(0); // Parte da 0
        progressBar.setPreferredSize(new Dimension(250, 6));
        progressBar.setMaximumSize(new Dimension(250, 6));
        progressBar.setAlignmentX(javax.swing.JProgressBar.CENTER_ALIGNMENT);
        progressBar.setForeground(new Color(41, 128, 185));
        contenitoreSplash.add(progressBar);

        splashPanel.add(contenitoreSplash, new java.awt.GridBagConstraints());

        finestra.setGlassPane(splashPanel);
        splashPanel.setVisible(true);

        // 3. MOSTRIAMO LA FINESTRA CON LO SPLASH
        finestra.setVisible(true);

        // 4. CARICAMENTO A GRADINI (Spezzettiamo il lavoro)
        javax.swing.Timer timerAvvio = new javax.swing.Timer(100, e -> {

            // STEP 1: Creiamo le fondamenta e la prima schermata
            progressBar.setValue(15); // Avanzamento barra
            CardLayout cardLayout = new CardLayout();
            JPanel pannelloSchermate = new JPanel(cardLayout);
            PannelloVoti schermataVoti = new PannelloVoti();

            SwingUtilities.invokeLater(() -> {
                // STEP 2: Schermata Aggiungi (Diamo tempo allo schermo di aggiornarsi)
                progressBar.setValue(40);
                PannelloAggiungi schermataAggiungi = new PannelloAggiungi(schermataVoti);

                SwingUtilities.invokeLater(() -> {
                    // STEP 3: Schermata Scadenze
                    progressBar.setValue(65);
                    PannelloScadenze schermataScadenze = new PannelloScadenze();

                    SwingUtilities.invokeLater(() -> {
                        // STEP 4: Il temuto Pannello Pomodoro
                        progressBar.setValue(85);
                        PannelloPomodoro schermataPomodoro = new PannelloPomodoro(finestra);

                        SwingUtilities.invokeLater(() -> {
                            // STEP 5: Uniamo tutto e completiamo al 100%
                            progressBar.setValue(100);

                            pannelloSchermate.add(schermataVoti, "Voti");
                            pannelloSchermate.add(schermataAggiungi, "Aggiungi");
                            pannelloSchermate.add(schermataScadenze, "Scadenze");
                            pannelloSchermate.add(schermataPomodoro, "Pomodoro");

                            // Setup Menu
                            JPanel pannelloMenu = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
                            Color coloreSfondoMenu = new Color(44, 62, 80);
                            pannelloMenu.setBackground(coloreSfondoMenu);
                            pannelloMenu
                                    .setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(41, 128, 185)));

                            // Bottoni
                            JButton btnVoti = creaBottoneMenu(" Voti e Media", coloreSfondoMenu);
                            btnVoti.addActionListener(evt -> {
                                cardLayout.show(pannelloSchermate, "Voti");
                                schermataVoti.refresh();
                            });
                            btnVoti.setIcon(new FlatSVGIcon("icone/bar.svg", 24, 24));

                            JButton btnAggiungi = creaBottoneMenu(" Aggiungi Esame", coloreSfondoMenu);
                            btnAggiungi.addActionListener(evt -> {
                                cardLayout.show(pannelloSchermate, "Aggiungi");
                                schermataAggiungi.aggiornaTutto();
                            });
                            btnAggiungi.setIcon(new FlatSVGIcon("icone/plus.svg", 24, 24));

                            JButton btnScadenze = creaBottoneMenu(" Timer Scadenze", coloreSfondoMenu);
                            btnScadenze.addActionListener(evt -> {
                                schermataScadenze.refreshOrdineScadenze();
                                cardLayout.show(pannelloSchermate, "Scadenze");
                            });
                            btnScadenze.setIcon(new FlatSVGIcon("icone/hourglass.svg", 24, 24));

                            JButton btnPomodoro = creaBottoneMenu(" Timer Pomodoro", coloreSfondoMenu);
                            btnPomodoro.addActionListener(evt -> cardLayout.show(pannelloSchermate, "Pomodoro"));
                            btnPomodoro.setIcon(new FlatSVGIcon("icone/tomato.svg", 24, 24));

                            pannelloMenu.add(btnVoti);
                            pannelloMenu.add(btnAggiungi);
                            pannelloMenu.add(btnScadenze);
                            pannelloMenu.add(btnPomodoro);

                            finestra.add(pannelloMenu, BorderLayout.SOUTH);
                            finestra.add(pannelloSchermate, BorderLayout.CENTER);
                            finestra.revalidate();
                            finestra.repaint();

                            // Trucco anti-lag visivo
                            cardLayout.show(pannelloSchermate, "Pomodoro");

                            // Aspettiamo una frazione di secondo per l'ultima elaborazione e poi apriamo il
                            // sipario
                            javax.swing.Timer timerFine = new javax.swing.Timer(400, evt -> {
                                cardLayout.show(pannelloSchermate, "Voti");
                                splashPanel.setVisible(false);
                                finestra.setGlassPane(new JPanel() {
                                    {
                                        setOpaque(false);
                                    }
                                });
                            });
                            timerFine.setRepeats(false);
                            timerFine.start();
                        });
                    });
                });
            });
        });

        timerAvvio.setRepeats(false);
        timerAvvio.start();
    }

    private static JButton creaBottoneMenu(String testo, Color coloreSfondo) {
        JButton btn = new JButton(testo);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(coloreSfondo);
        
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        
        // Diamo un po' di spazio "interno" al bottone
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color coloreHover = new Color(48, 68, 88);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(coloreHover);
                btn.setBorderPainted(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(coloreSfondo);
                btn.setBorderPainted(false);
            }
        });

        return btn;
    }
}

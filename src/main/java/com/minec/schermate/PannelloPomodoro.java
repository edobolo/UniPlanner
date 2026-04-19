package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class PannelloPomodoro extends JPanel{
    private JLabel lblTimer;
    private JLabel lblStato;
    private JButton btnStartPause;
    private JButton btnReset;

    private Timer timer;
    private int secondiRimanenti;
    private boolean inEsecuzione = false;
    private boolean isSessioneStudio = true;
    
    private static final int MINUTI_STUDIO = 1;
    private static final int MINUTI_PAUSA = 5;

    public PannelloPomodoro() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // ---Titolo e Stato---
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("Timer Pomodoro", CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        lblStato = new JLabel("Pronto per studiare?", SwingConstants.CENTER);
        lblStato.setFont(new Font("Arial", Font.ITALIC, 18));
        lblStato.setForeground(Color.GRAY);
        topPanel.add(title);
        topPanel.add(lblStato);
        this.add(topPanel, BorderLayout.NORTH);

        // --- Timer Display ---
        lblTimer = new JLabel("25:00", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Monospaced", Font.BOLD, 100));
        this.add(lblTimer, BorderLayout.CENTER);

        // --- Controlli ---
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnStartPause = new JButton("Avvia");
        btnReset = new JButton("Reset");
        btnStartPause.setPreferredSize(new Dimension(120, 40));
        btnReset.setPreferredSize(new Dimension(120, 40));
        btnStartPause.addActionListener(e -> toggleTimer());
        btnReset.addActionListener(e -> resetTimer());
        botPanel.add(btnStartPause);
        botPanel.add(btnReset);
        this.add(botPanel, BorderLayout.SOUTH);
        resetTimer(); // Inizializza lo stato
    }

    private void toggleTimer() {
        if (inEsecuzione) {
            pausaTimer();
        } else {
            avviaTimer();
        }
    }

    private void avviaTimer() {
        riproduciSuono();
        inEsecuzione = true;
        btnStartPause.setText("Pausa");
        if(isSessioneStudio) {
            lblStato.setIcon(new FlatSVGIcon("icone/books.svg" , 20, 20));
            lblStato.setText(" Sessione di Studio...");
        } else {
            lblStato.setIcon(new FlatSVGIcon("icone/coffee.svg" , 20, 20));
            lblStato.setText(" Pausa...");
        }
        lblStato.setForeground(isSessioneStudio ? new Color(231, 76, 60) : new Color(46, 204, 113));

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (secondiRimanenti > 0) {
                    secondiRimanenti--;
                    aggiornaLabelTimer();
                } else {
                    timerFinito();
                }
            }
        }, 1000, 1000);
    }

    private void pausaTimer() {
        inEsecuzione = false;
        btnStartPause.setText("Riprendi");
        if (timer != null) timer.cancel();
    }

    private void resetTimer() {
        pausaTimer();
        isSessioneStudio = true;
        secondiRimanenti = MINUTI_STUDIO * 60;
        btnStartPause.setText("Avvia");
        lblStato.setText("Pronto per studiare?");
        lblStato.setForeground(Color.GRAY);
        aggiornaLabelTimer();
    }

    private void aggiornaLabelTimer() {
        int min = secondiRimanenti / 60;
        int sec = secondiRimanenti % 60;
        // SwingUtilities.invokeLater assicura che il cambio grafico avvenga nel thread giusto
        SwingUtilities.invokeLater(() -> lblTimer.setText(String.format("%02d:%02d", min, sec)));
    }

    private void timerFinito() {
        pausaTimer();
        riproduciSuono();
        // Notifica di sistema usando il tuo GestoreNotifiche!
        String titolo = isSessioneStudio ? "Studio Completato!" : "Pausa Finita!";
        String msg = isSessioneStudio ? "Ottimo lavoro! Ora prenditi 5 minuti di pausa." : "La pausa è finita, torna sui libri!";
        // Se hai aggiunto il metodo mostraMessaggio nel GestoreNotifiche, usalo qui
        // Altrimenti possiamo richiamare una notifica standard qui per ora
        JOptionPane.showMessageDialog(this, msg, titolo, JOptionPane.INFORMATION_MESSAGE);
        // Cambia modalità (se era studio passa a pausa, e viceversa)
        isSessioneStudio = !isSessioneStudio;
        secondiRimanenti = (isSessioneStudio ? MINUTI_STUDIO : MINUTI_PAUSA) * 60;
        aggiornaLabelTimer();
        btnStartPause.setText("Avvia " + (isSessioneStudio ? "Studio" : "Pausa"));
    }

    private void riproduciSuono() {
    try {
        File fileSuono = new File("suoni/ding.wav"); 
        if (fileSuono.exists()) {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileSuono);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } else {
            System.out.println("File audio non trovato al percorso: " + fileSuono.getAbsolutePath());
        }
    } catch (Exception e) {
        System.out.println("Errore durante la riproduzione del suono - " + e.getMessage());
    }
}
}

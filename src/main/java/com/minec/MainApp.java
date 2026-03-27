package com.minec;

import javax.swing.*;

import com.minec.schermate.PannelloAggiungi;
import com.minec.schermate.PannelloScadenze;
import com.minec.schermate.PannelloVoti;

import java.awt.*;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> creaEmostraGUI());
    }

    private static void creaEmostraGUI() {
        JFrame finestra = new JFrame("Gestore Esami Universitari");
        finestra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        finestra.setSize(800, 600);
        finestra.setResizable(false);
        finestra.setLocationRelativeTo(null);

        CardLayout cardLayout = new CardLayout();
        JPanel pannelloSchermate = new JPanel(cardLayout);

        // Ora creiamo le schermate usando le nostre nuove CLASSI personalizzate!
        PannelloVoti schermataVoti = new PannelloVoti();
        PannelloAggiungi schermataAggiungi = new PannelloAggiungi(schermataVoti);
        PannelloScadenze schermataScadenze = new PannelloScadenze();

        // Le aggiungiamo al pannello principale
        pannelloSchermate.add(schermataVoti, "Voti");
        pannelloSchermate.add(schermataAggiungi, "Aggiungi");
        pannelloSchermate.add(schermataScadenze, "Scadenze");

        // Menu di navigazione
        JPanel pannelloMenu = new JPanel();
        pannelloMenu.setBackground(Color.LIGHT_GRAY);

        JButton btnVoti = new JButton("Voti e Media");
        btnVoti.addActionListener(e -> cardLayout.show(pannelloSchermate, "Voti"));

        JButton btnAggiungi = new JButton("Aggiungi Esame");
        btnAggiungi.addActionListener(e -> cardLayout.show(pannelloSchermate, "Aggiungi"));

        JButton btnScadenze = new JButton("Timer Scadenze");
        btnScadenze.addActionListener(e -> cardLayout.show(pannelloSchermate, "Scadenze"));

        pannelloMenu.add(btnVoti);
        pannelloMenu.add(btnAggiungi);
        pannelloMenu.add(btnScadenze);

        //assemblo la finestra
        finestra.add(pannelloMenu, BorderLayout.SOUTH);
        finestra.add(pannelloSchermate, BorderLayout.CENTER);

        finestra.setVisible(true);
    }
}
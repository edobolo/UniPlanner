package com.minec;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;
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
        SwingUtilities.invokeLater(() -> creaEmostraGUI());
    }

    private static void creaEmostraGUI() {
        JFrame finestra = new JFrame("UniPlanner");
        finestra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        finestra.setResizable(true);
        finestra.setIconImages(FlatSVGUtils.createWindowIconImages("/icone/u.svg"));

        CardLayout cardLayout = new CardLayout();
        JPanel pannelloSchermate = new JPanel(cardLayout);

        PannelloVoti schermataVoti = new PannelloVoti();
        PannelloAggiungi schermataAggiungi = new PannelloAggiungi(schermataVoti);
        PannelloScadenze schermataScadenze = new PannelloScadenze();
        PannelloPomodoro schermataPomodoro = new PannelloPomodoro();

        pannelloSchermate.add(schermataVoti, "Voti");
        pannelloSchermate.add(schermataAggiungi, "Aggiungi");
        pannelloSchermate.add(schermataScadenze, "Scadenze");
        pannelloSchermate.add(schermataPomodoro, "Pomodoro");

        // Usiamo un FlowLayout con margini più ampi (30px tra un bottone e l'altro)
        JPanel pannelloMenu = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        Color coloreSfondoMenu = new Color(44, 62, 80); 
        pannelloMenu.setBackground(coloreSfondoMenu);
        // Aggiungiamo un bordo colorato in alto alla barra per staccarla dal contenuto
        pannelloMenu.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(41, 128, 185)));

        // Creiamo i bottoni
        JButton btnVoti = creaBottoneMenu(" Voti e Media", coloreSfondoMenu);
        btnVoti.addActionListener(e -> {
            cardLayout.show(pannelloSchermate, "Voti");
            schermataVoti.refresh();
        });
        btnVoti.setIcon(new FlatSVGIcon("icone/bar.svg", 24, 24));

        JButton btnAggiungi = creaBottoneMenu(" Aggiungi Esame", coloreSfondoMenu);
        btnAggiungi.addActionListener(e -> {
            cardLayout.show(pannelloSchermate, "Aggiungi");
            schermataAggiungi.aggiornaTutto();
        });
        btnAggiungi.setIcon(new FlatSVGIcon("icone/plus.svg", 24, 24));

        JButton btnScadenze = creaBottoneMenu(" Timer Scadenze", coloreSfondoMenu);
        btnScadenze.addActionListener(e -> {
            schermataScadenze.refreshOrdineScadenze();
            cardLayout.show(pannelloSchermate, "Scadenze");
        });
        btnScadenze.setIcon(new FlatSVGIcon("icone/hourglass.svg", 24, 24));

        JButton btnPomodoro = creaBottoneMenu(" Timer Pomodoro", coloreSfondoMenu);
        btnPomodoro.addActionListener(e -> {
            cardLayout.show(pannelloSchermate, "Pomodoro");
        });
        btnPomodoro.setIcon(new FlatSVGIcon("icone/tomato.svg", 24, 24));

        pannelloMenu.add(btnVoti);
        pannelloMenu.add(btnAggiungi);
        pannelloMenu.add(btnScadenze);
        pannelloMenu.add(btnPomodoro);

        finestra.add(pannelloMenu, BorderLayout.SOUTH);
        finestra.add(pannelloSchermate, BorderLayout.CENTER);

        finestra.setMinimumSize(new Dimension(970, 730));
        finestra.setSize(970, 730);
        finestra.setLocationRelativeTo(null);
        finestra.setVisible(true);
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

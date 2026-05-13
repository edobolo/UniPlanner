package com.minec.schermate;

import javax.swing.*;
import java.awt.*;

public class DialoghiModerni {

    // --- IL SEGRETO: UN BOTTONE A PILLOLA DISEGNATO DA ZERO ---
    // (Ignora i limiti di FlatLaf e si disegna perfettamente curvo)
    static class BottonePillola extends JButton {
        private Color coloreSfondo;

        public BottonePillola(String testo, Color colore) {
            super(testo);
            this.coloreSfondo = colore;
            setContentAreaFilled(false); // Blocca il disegno standard
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25)); // Spazio ai lati
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Bordi fluidi

            // Colori quando clicchi o ci passi sopra col mouse
            if (getModel().isPressed()) {
                g2.setColor(coloreSfondo.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(coloreSfondo.brighter());
            } else {
                g2.setColor(coloreSfondo);
            }

            // Disegna la forma a pillola (l'arco è uguale all'altezza)
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();

            super.paintComponent(g); // Disegna il testo sopra
        }
    }

    // --- 1. MESSAGGIO NORMALE (Info o Errori) ---
    public static void mostraMessaggio(Component parent, String titolo, String testo, boolean isErrore) {
        BottonePillola btnOk = new BottonePillola("OK", isErrore ? new Color(230, 57, 70) : new Color(58, 134, 255));

        JLabel lblTesto = new JLabel("<html><div style='text-align: center; width: 250px;'>" + testo + "</div></html>");
        lblTesto.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JOptionPane pane = new JOptionPane(lblTesto, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                new Object[] { btnOk }, btnOk);
        JDialog dialog = pane.createDialog(parent, titolo);

        btnOk.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // --- 2. MESSAGGI DI CONFERMA (Sì / No) ---
    public static boolean chiediConferma(Component parent, String titolo, String testo, String testoConferma,
            boolean isPericoloso) {
        BottonePillola btnConferma = new BottonePillola(testoConferma,
                isPericoloso ? new Color(230, 57, 70) : new Color(60, 179, 113));
        BottonePillola btnAnnulla = new BottonePillola("Annulla", new Color(100, 116, 139));

        JLabel lblTesto = new JLabel("<html><div style='text-align: center; width: 260px;'>" + testo + "</div></html>");
        lblTesto.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JOptionPane pane = new JOptionPane(lblTesto, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null,
                new Object[] { btnConferma, btnAnnulla }, btnAnnulla);
        JDialog dialog = pane.createDialog(parent, titolo);

        btnConferma.addActionListener(e -> {
            pane.setValue(JOptionPane.YES_OPTION);
            dialog.dispose();
        });
        btnAnnulla.addActionListener(e -> {
            pane.setValue(JOptionPane.NO_OPTION);
            dialog.dispose();
        });

        dialog.setVisible(true);
        Object value = pane.getValue();
        return value != null && value.equals(JOptionPane.YES_OPTION);
    }

    // --- 3. MESSAGGI DI INPUT ---
    public static String chiediInput(Component parent, String titolo, String testo, String testoConferma,
            String valoreIniziale) {

        // 1. Il Campo di Testo bloccato a una dimensione corta
        JTextField inputField = new JTextField();
        if (valoreIniziale != null)
            inputField.setText(valoreIniziale);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.putClientProperty("JTextField.placeholderText", "Inserisci valore...");
        inputField.putClientProperty("JComponent.roundRect", true);
        inputField.setHorizontalAlignment(JTextField.LEFT); // Centra il testo che scrivi!

        // La magia che gli impedisce di allargarsi:
        inputField.setMaximumSize(new Dimension(180, 35));
        inputField.setPreferredSize(new Dimension(180, 35));

        // 2. Il Testo in alto
        JLabel lblTesto = new JLabel("<html><div style='text-align: center; width: 260px;'>" + testo + "</div></html>");
        lblTesto.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // 3. Pannello che costringe gli elementi ad allinearsi al centro
        JPanel pannelloInput = new JPanel();
        pannelloInput.setLayout(new BoxLayout(pannelloInput, BoxLayout.Y_AXIS));
        pannelloInput.setOpaque(false);

        lblTesto.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputField.setAlignmentX(Component.CENTER_ALIGNMENT);

        pannelloInput.add(lblTesto);
        pannelloInput.add(Box.createVerticalStrut(15)); // Spazio tra la scritta e la casella
        pannelloInput.add(inputField);

        // 4. I Bottoni personalizzati
        BottonePillola btnConferma = new BottonePillola(testoConferma, new Color(58, 134, 255));
        BottonePillola btnAnnulla = new BottonePillola("Annulla", new Color(100, 116, 139));

        JOptionPane pane = new JOptionPane(pannelloInput, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
                new Object[] { btnConferma, btnAnnulla }, null);
        JDialog dialog = pane.createDialog(parent, titolo);

        btnConferma.addActionListener(e -> {
            pane.setValue(JOptionPane.OK_OPTION);
            dialog.dispose();
        });
        btnAnnulla.addActionListener(e -> {
            pane.setValue(JOptionPane.CANCEL_OPTION);
            dialog.dispose();
        });
        inputField.addActionListener(e -> {
            pane.setValue(JOptionPane.OK_OPTION);
            dialog.dispose();
        }); // Funziona l'invio!

        // Autofocus
        dialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                inputField.requestFocusInWindow();
                inputField.selectAll();
            }
        });

        dialog.setVisible(true);

        Object value = pane.getValue();
        if (value != null && value.equals(JOptionPane.OK_OPTION)) {
            return inputField.getText();
        }
        return null;
    }
}
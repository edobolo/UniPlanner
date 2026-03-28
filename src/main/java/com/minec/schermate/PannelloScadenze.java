package com.minec.schermate;

import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

import com.github.lgooddatepicker.components.DatePicker;
import com.minec.dati.GestoreDati;

public class PannelloScadenze extends JPanel {

    private JPanel scadenzeListPanel;
    private JComboBox<String> comboEsami;
    private boolean ordinaPerData = false;

    public PannelloScadenze() {
        this.setLayout(new BorderLayout());

        JPanel moduloPanel = new JPanel();
        moduloPanel.setPreferredSize(new Dimension(800, 150));
        moduloPanel.setLayout(null);

        JLabel title = new JLabel("Imposta le date degli esami");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBounds(230, 20, 400, 40);
        moduloPanel.add(title);

        comboEsami = new JComboBox<>();
        comboEsami.setBounds(100, 80, 250, 35);
        caricaEsamiNelMenu();
        moduloPanel.add(comboEsami);

        DatePicker datePicker = new DatePicker();
        datePicker.setBounds(370, 80, 200, 35);

        JButton btnCalendario = datePicker.getComponentToggleCalendarButton();
        btnCalendario.setText("▼");
        btnCalendario.setFont(new Font("Arial", Font.BOLD, 12));
        btnCalendario.setIcon(null);

        moduloPanel.add(datePicker);

        JButton btnSalva = new JButton("Salva Data");
        btnSalva.setBounds(590, 80, 150, 35);
        moduloPanel.add(btnSalva);

        // --- PARTE CENTRO: Lista delle scadenze ---
        JPanel contenitoreLista = new JPanel(new BorderLayout());
        contenitoreLista.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2), "Prossimi Esami",
                0, 0, new Font("Arial", Font.BOLD, 16)));

        // --- NOVITÀ 2: Creiamo un pannellino in alto a destra per il bottone di
        // ordinamento ---
        JPanel barraStrumentiLista = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOrdina = new JButton("Ordina: Cronologico");
        btnOrdina.setFont(new Font("Arial", Font.ITALIC, 12));
        barraStrumentiLista.add(btnOrdina);

        contenitoreLista.add(barraStrumentiLista, BorderLayout.NORTH);

        scadenzeListPanel = new JPanel();
        scadenzeListPanel.setLayout(new BoxLayout(scadenzeListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(scadenzeListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contenitoreLista.add(scrollPane, BorderLayout.CENTER);

        this.add(moduloPanel, BorderLayout.NORTH);
        this.add(contenitoreLista, BorderLayout.CENTER);

        aggiornaListaScadenze();

        btnSalva.addActionListener(e -> {
            String esameSelezionato = (String) comboEsami.getSelectedItem();
            LocalDate dataSelezionata = datePicker.getDate();
            if (esameSelezionato == null || dataSelezionata == null) {
                JOptionPane.showMessageDialog(this, "Seleziona sia un esame che una data valida!");
                return;
            }
            GestoreDati.salvaScadenza(esameSelezionato, dataSelezionata.toString());
            datePicker.clear();
            aggiornaListaScadenze();
        });

        btnOrdina.addActionListener(e -> {
            ordinaPerData = !ordinaPerData; // Inverte il valore (da falso a vero e viceversa)

            if (ordinaPerData) {
                btnOrdina.setText("Ordina: Aggiunta"); 
            } else {
                btnOrdina.setText("Ordina: Cronologico");
            }

            aggiornaListaScadenze();
        });
    }

    private void caricaEsamiNelMenu() {
        comboEsami.removeAllItems();
        String[] esamiRaw = GestoreDati.getEsamiSalvatiRaw();
        for (String riga : esamiRaw) {
            String[] parti = riga.split(";");
            if (parti.length >= 2 && parti[1].equals("false")) {
                comboEsami.addItem(parti[0]);
            }
        }
        comboEsami.setSelectedIndex(-1);
    }

    public void aggiornaListaScadenze() {
        scadenzeListPanel.removeAll();
        String[] scadenzeRaw = GestoreDati.getScadenzeRaw();
        LocalDate oggi = LocalDate.now();
        List<String> listaScadenze = new ArrayList<>(Arrays.asList(scadenzeRaw));
        if (ordinaPerData) {
            listaScadenze.sort((riga1, riga2) -> {
                try {
                    // Estraiamo le date (il secondo elemento della stringa)
                    LocalDate data1 = LocalDate.parse(riga1.split(";")[1]);
                    LocalDate data2 = LocalDate.parse(riga2.split(";")[1]);
                    // Confrontiamo le due date (la più vicina andrà in alto)
                    return data1.compareTo(data2);
                } catch (Exception e) {
                    return 0; // Se c'è un errore nella lettura, lasciali dove sono
                }
            });
        }

        for (String riga : listaScadenze) {
            String[] parti = riga.split(";");
            if (parti.length >= 2) {
                String nomeEsame = parti[0];
                LocalDate dataEsame = LocalDate.parse(parti[1]);
                long giorniMancanti = ChronoUnit.DAYS.between(oggi, dataEsame);

                // --- CREAZIONE DEL RIQUADRO GRAFICO ---
                JPanel panel = new JPanel(new BorderLayout());
                Dimension dim = new Dimension(720, 50);
                panel.setPreferredSize(dim);
                panel.setMaximumSize(dim);
                panel.setMinimumSize(dim);
                panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

                JLabel lblNome = new JLabel("  " + nomeEsame + " (" + parti[1] + ")");
                lblNome.setFont(new Font("Arial", Font.BOLD, 16));

                JLabel lblGiorni = new JLabel();
                lblGiorni.setFont(new Font("Arial", Font.BOLD, 16));

                if (giorniMancanti < 0) {
                    lblGiorni.setText("Scaduto da " + Math.abs(giorniMancanti) + " gg");
                    lblGiorni.setForeground(Color.GRAY);
                } else if (giorniMancanti == 0) {
                    lblGiorni.setText("È OGGI!");
                    lblGiorni.setForeground(Color.RED);
                } else if (giorniMancanti <= 7) {
                    lblGiorni.setText("-" + giorniMancanti + " gg!");
                    lblGiorni.setForeground(Color.RED);
                } else {
                    lblGiorni.setText("-" + giorniMancanti + " gg");
                    lblGiorni.setForeground(new Color(0, 150, 0));
                }

                // --- BOTTONE RIMOZIONE ---
                JButton btnRimuovi = new JButton("X");
                btnRimuovi.setForeground(Color.RED);
                btnRimuovi.setFont(new Font("Arial", Font.BOLD, 14));
                btnRimuovi.setMargin(new Insets(2, 5, 2, 5));

                btnRimuovi.addActionListener(e -> {
                    int conferma = JOptionPane.showConfirmDialog(this,
                            "Vuoi davvero rimuovere la data per " + nomeEsame + "?",
                            "Conferma rimozione", JOptionPane.YES_NO_OPTION);

                    if (conferma == JOptionPane.YES_OPTION) {
                        GestoreDati.removeScadenza(nomeEsame);
                        aggiornaListaScadenze();
                    }
                });

                JPanel pannelloDestra = new JPanel();
                pannelloDestra.setLayout(new BoxLayout(pannelloDestra, BoxLayout.X_AXIS));
                pannelloDestra.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

                pannelloDestra.add(lblGiorni);
                pannelloDestra.add(Box.createRigidArea(new Dimension(15, 0)));
                pannelloDestra.add(btnRimuovi);

                panel.add(lblNome, BorderLayout.WEST);
                panel.add(pannelloDestra, BorderLayout.EAST);

                scadenzeListPanel.add(panel);
                scadenzeListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        caricaEsamiNelMenu();
        scadenzeListPanel.revalidate();
        scadenzeListPanel.repaint();
    }
}
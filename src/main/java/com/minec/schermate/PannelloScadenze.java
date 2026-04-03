package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.lgooddatepicker.components.DatePicker;
import com.minec.dati.GestoreDati;

public class PannelloScadenze extends JPanel {

    private JPanel scadenzeListPanel;
    private JComboBox<String> comboEsami;
    private JButton btnOrdina;
    private DatePicker datePicker;
    private boolean ordinaPerData = GestoreDati.getOrdineScadenza();

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

        datePicker = new DatePicker();
        datePicker.setBounds(370, 80, 200, 35);
        datePicker.setBackground(getBackground());
        applicaTemaCampoData();

        JButton btnCalendario = datePicker.getComponentToggleCalendarButton();
        btnCalendario.setText("");
        btnCalendario.setIcon(new FlatSVGIcon("icone/calendar.svg", 24, 24));
        btnCalendario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        moduloPanel.add(datePicker);

        JButton btnSalva = new JButton("Salva Data");
        btnSalva.setBounds(590, 80, 150, 35);
        moduloPanel.add(btnSalva);

        // --- PARTE CENTRO: Lista delle scadenze ---
        javax.swing.border.Border bordoSoloSopra = BorderFactory.createMatteBorder(2, 0, 0, 0, Color.GRAY);
        JPanel contenitoreLista = new JPanel(new BorderLayout());
        contenitoreLista.setBorder(BorderFactory.createTitledBorder(
                bordoSoloSopra, "Prossimi Esami",0, 
                0, new Font("Arial", Font.BOLD, 16)));

        JPanel barraStrumentiLista = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnOrdina = new JButton(ordinaPerData ? "Ordina: Aggiunta" : "Ordina: Cronologico");
        btnOrdina.setFont(new Font("Arial", Font.ITALIC, 12));
        barraStrumentiLista.add(btnOrdina);

        contenitoreLista.add(barraStrumentiLista, BorderLayout.NORTH);

        scadenzeListPanel = new JPanel();
        scadenzeListPanel.setLayout(new BoxLayout(scadenzeListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(scadenzeListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
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
            GestoreDati.salvaOrdineScadenze(ordinaPerData);
            btnOrdina.setText(ordinaPerData ? "Ordina: Aggiunta" : "Ordina: Cronologico");
            aggiornaListaScadenze();
        });
    }

    private void applicaTemaCampoData() {
        if (datePicker == null) {
            return;
        }
        JTextField campoData = datePicker.getComponentDateTextField();
        if (GestoreDati.isTemaScuro()) {
            campoData.setBackground(new Color(60, 63, 65));
            campoData.setForeground(new Color(230, 230, 230));
            campoData.setCaretColor(new Color(230, 230, 230));
        } else {
            campoData.setBackground(Color.WHITE);
            campoData.setForeground(Color.BLACK);
            campoData.setCaretColor(Color.BLACK);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        applicaTemaCampoData();
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

                JLabel lblNome = new JLabel(" " + nomeEsame + " (" + parti[1] + ")");
                lblNome.setBorder(new EmptyBorder(0, 15, 0, 0));
                lblNome.setIcon(new FlatSVGIcon("icone/books.svg", 20, 20));
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
                JButton btnRimuovi = new JButton("");
                btnRimuovi.setIcon(new FlatSVGIcon("icone/x.svg", 24, 24));
                btnRimuovi.setContentAreaFilled(false);
                btnRimuovi.setBorderPainted(false);
                btnRimuovi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

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

    public void refreshOrdineScadenze() {
        ordinaPerData = GestoreDati.getOrdineScadenza();
        if (btnOrdina != null) {
            btnOrdina.setText(ordinaPerData ? "Ordina: Aggiunta" : "Ordina: Cronologico");
        }
        aggiornaListaScadenze();
    }
}
package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.lgooddatepicker.components.DatePicker;
import com.minec.GestoreNotifiche;
import com.minec.dati.GestoreDati;

public class PannelloScadenze extends JPanel {

    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 1.8f;

    private JPanel moduloPanel;
    private JPanel contenitoreLista;
    private TitledBorder listaTitledBorder;
    private JLabel title;
    private JPanel scadenzeListPanel;
    private JComboBox<String> comboEsami;
    private JButton btnOrdina;
    private DatePicker datePicker;
    private JButton btnSalva;
    private JButton btnCalendario;
    private boolean ordinaPerData = GestoreDati.getOrdineScadenza();
    private float currentScale = 1.0f;

    public PannelloScadenze() {
        this.setLayout(new BorderLayout());

        moduloPanel = new JPanel();
        moduloPanel.setPreferredSize(new Dimension(BASE_WIDTH, 150));
        moduloPanel.setLayout(null);

        title = new JLabel("Imposta le date degli esami");
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        moduloPanel.add(title);

        comboEsami = new JComboBox<>();
        caricaEsamiNelMenu();
        moduloPanel.add(comboEsami);

        datePicker = new DatePicker();
        datePicker.setBackground(getBackground());
        applicaTemaCampoData();

        btnCalendario = datePicker.getComponentToggleCalendarButton();
        btnCalendario.setText("");
        btnCalendario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        moduloPanel.add(datePicker);

        btnSalva = new JButton("Salva Data");
        moduloPanel.add(btnSalva);

        // --- PARTE CENTRO: Lista delle scadenze ---
        javax.swing.border.Border bordoSoloSopra = BorderFactory.createMatteBorder(2, 0, 0, 0, Color.GRAY);
        contenitoreLista = new JPanel(new BorderLayout());
        listaTitledBorder = BorderFactory.createTitledBorder(
            bordoSoloSopra, "Prossimi Esami", TitledBorder.CENTER,
            TitledBorder.TOP, new Font("Arial", Font.BOLD, 16));
        contenitoreLista.setBorder(listaTitledBorder);

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

        setupResponsiveLayout();
        initListaScadenze();
        SwingUtilities.invokeLater(this::applyResponsiveLayout);

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
            GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
        });

        btnOrdina.addActionListener(e -> {
            ordinaPerData = !ordinaPerData; // Inverte il valore (da falso a vero e viceversa)
            GestoreDati.salvaOrdineScadenze(ordinaPerData);
            btnOrdina.setText(ordinaPerData ? "Ordina: Aggiunta" : "Ordina: Cronologico");
            aggiornaListaScadenze();
        });
    }

    private void setupResponsiveLayout() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveLayout();
            }
        });
    }

    private void initListaScadenze() {
        aggiornaListaScadenze();
    }

    private void applyResponsiveLayout() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        float scaleX = (float) getWidth() / BASE_WIDTH;
        float scaleY = (float) getHeight() / BASE_HEIGHT;
        currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.min(scaleX, scaleY)));

        int topHeight = Math.max(145, Math.round(150 * currentScale));
        moduloPanel.setPreferredSize(new Dimension(getWidth(), topHeight));
        moduloPanel.setMinimumSize(new Dimension(0, topHeight));
        moduloPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, topHeight));

        title.setBounds(0, Math.round(20 * currentScale), getWidth(), Math.round(40 * currentScale));

        int comboWidth = Math.round(250 * currentScale);
        int dateWidth = Math.round(200 * currentScale);
        int saveWidth = Math.round(150 * currentScale);
        int rowGap = Math.max(12, Math.round(18 * currentScale));
        int rowY = Math.round(80 * currentScale);
        int rowWidth = comboWidth + rowGap + dateWidth + rowGap + saveWidth;
        int rowX = Math.max(Math.round(20 * currentScale), (getWidth() - rowWidth) / 2);

        comboEsami.setBounds(rowX, rowY, comboWidth, Math.round(35 * currentScale));
        datePicker.setBounds(rowX + comboWidth + rowGap, rowY, dateWidth, Math.round(35 * currentScale));
        btnSalva.setBounds(rowX + comboWidth + rowGap + dateWidth + rowGap, rowY, saveWidth, Math.round(35 * currentScale));

        if (btnCalendario != null) {
            btnCalendario.setIcon(new FlatSVGIcon("icone/calendar.svg", Math.max(20, Math.round(24 * currentScale)), Math.max(20, Math.round(24 * currentScale))));
        }

        if (btnOrdina != null) {
            btnOrdina.setFont(btnOrdina.getFont().deriveFont(Math.max(12f, 12f * currentScale)));
        }

        if (listaTitledBorder != null) {
            listaTitledBorder.setTitleFont(new Font("Arial", Font.BOLD, Math.max(16, Math.round(16 * currentScale))));
        }

        scaleFontsRecursively(this, currentScale);
        aggiornaListaScadenze();
        revalidate();
        repaint();
    }

    private void scaleFontsRecursively(Container container, float scale) {
        for (Component child : container.getComponents()) {
            if (child instanceof JComponent component) {
                Font baseFont = (Font) component.getClientProperty("baseFont");
                if (baseFont == null && component.getFont() != null) {
                    baseFont = component.getFont();
                    component.putClientProperty("baseFont", baseFont);
                }
                if (baseFont != null) {
                    component.setFont(baseFont.deriveFont(Math.max(12f, baseFont.getSize2D() * scale)));
                }
            }
            if (child instanceof Container childContainer) {
                scaleFontsRecursively(childContainer, scale);
            }
        }
    }

    private int getScaledRowWidth() {
        int availableWidth = getWidth();
        if (availableWidth <= 0 && scadenzeListPanel != null && scadenzeListPanel.getParent() != null) {
            availableWidth = scadenzeListPanel.getParent().getWidth();
        }
        if (availableWidth <= 0) {
            availableWidth = 720;
        }
        return Math.max(520, availableWidth - Math.round(40 * currentScale));
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
                Dimension dim = new Dimension(getScaledRowWidth(), Math.max(50, Math.round(50 * currentScale)));
                panel.setPreferredSize(dim);
                panel.setMaximumSize(dim);
                panel.setMinimumSize(dim);
                panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

                JLabel lblNome = new JLabel(" " + nomeEsame + " (" + parti[1] + ")");
                lblNome.setBorder(new EmptyBorder(0, 15, 0, 0));
                lblNome.setIcon(new FlatSVGIcon("icone/books.svg", Math.max(16, Math.round(20 * currentScale)), Math.max(16, Math.round(20 * currentScale))));
                lblNome.setFont(new Font("Arial", Font.BOLD, Math.max(14, Math.round(16 * currentScale))));

                JLabel lblGiorni = new JLabel();
                lblGiorni.setFont(new Font("Arial", Font.BOLD, Math.max(14, Math.round(16 * currentScale))));

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
                btnRimuovi.setIcon(new FlatSVGIcon("icone/x.svg", Math.max(18, Math.round(24 * currentScale)), Math.max(18, Math.round(24 * currentScale))));
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
                        GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
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
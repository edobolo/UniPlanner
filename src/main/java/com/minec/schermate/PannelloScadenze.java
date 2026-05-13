package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.lgooddatepicker.components.DatePicker;
import com.minec.GestoreNotifiche;
import com.minec.dati.GestoreDatabase;

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
    private JPanel[] pnlGiorni;
    private JButton btnOrdina;
    private DatePicker datePicker;
    private JButton btnSalva;
    private JButton btnCalendario;
    private int currentMonth;
    private int currentYear;
    private boolean ordinaPerData = GestoreDatabase.getOrdineScadenza();
    private float currentScale = 1.0f;
    private JPanel calendar;
    private int numGiorni;
    private String[] mesi = {"Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno" ,"Luglio", "Agosto", 
                            "Settembre", "Ottobre", "Novembre", "Dicembre"};
    private boolean showLista = true;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public PannelloScadenze() {
        this.setLayout(new BorderLayout());
        cardLayout = new CardLayout();

        // --- PANNELLO SUPERIORE (Input) ---
        moduloPanel = new JPanel(new BorderLayout());
        moduloPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        title = new JLabel("Imposta le date degli esami", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        moduloPanel.add(title, BorderLayout.NORTH);

        // Riga di input centrale
        JPanel rigaInput = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        comboEsami = new JComboBox<>();
        comboEsami.setPreferredSize(new Dimension(250, 36));
        comboEsami.setFont(new Font("Arial", Font.PLAIN, 14));
        caricaEsamiNelMenu();
        
        datePicker = new DatePicker();
        applicaTemaCampoData();
        btnCalendario = datePicker.getComponentToggleCalendarButton();
        btnCalendario.setText("");
        btnCalendario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCalendario.setIcon(new FlatSVGIcon("icone/calendar.svg", 20, 20));

        btnSalva = new JButton("Salva Data");
        btnSalva.putClientProperty("JButton.buttonType", "roundRect");
        btnSalva.setBackground(new Color(33, 150, 243));
        btnSalva.setForeground(Color.WHITE);
        btnSalva.setFont(new Font("Arial", Font.BOLD, 14));
        btnSalva.setPreferredSize(new Dimension(130, 36));
        btnSalva.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rigaInput.add(comboEsami);
        rigaInput.add(datePicker);
        rigaInput.add(btnSalva);
        moduloPanel.add(rigaInput, BorderLayout.CENTER);

        // --- VISTA: LISTA SCADENZE ---
        contenitoreLista = new JPanel(new BorderLayout());
        contenitoreLista.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JPanel headerLista = new JPanel(new BorderLayout());
        headerLista.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));
        JLabel lblTitoloLista = new JLabel("Prossimi Esami");
        lblTitoloLista.setFont(new Font("Arial", Font.BOLD, 20));
        headerLista.add(lblTitoloLista, BorderLayout.WEST);

        JPanel barraStrumentiLista = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        barraStrumentiLista.setOpaque(false);
        
        btnOrdina = new JButton(ordinaPerData ? "Ordina: Aggiunta" : "Ordina: Cronologico");
        btnOrdina.putClientProperty("JButton.buttonType", "roundRect");
        btnOrdina.setFont(new Font("Arial", Font.ITALIC, 12));
        
        JButton btnScambia = new JButton("Vedi Calendario");
        btnScambia.putClientProperty("JButton.buttonType", "roundRect");
        btnScambia.setIcon(new FlatSVGIcon("icone/calendar.svg", 18, 18));
        btnScambia.addActionListener(e -> scambiaViste(btnScambia));
        
        barraStrumentiLista.add(btnOrdina);
        barraStrumentiLista.add(btnScambia);
        headerLista.add(barraStrumentiLista, BorderLayout.EAST);
        contenitoreLista.add(headerLista, BorderLayout.NORTH);

        scadenzeListPanel = new JPanel();
        scadenzeListPanel.setLayout(new BoxLayout(scadenzeListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(scadenzeListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contenitoreLista.add(scrollPane, BorderLayout.CENTER);

        // --- VISTA: CALENDARIO ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JPanel headerCalendario = new JPanel(new BorderLayout());
        headerCalendario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));
        
        LocalDate oggi = LocalDate.now();
        currentMonth = oggi.getMonthValue();
        currentYear = oggi.getYear();
        setCalendar(currentMonth, currentYear);

        JLabel month = new JLabel(mesi[currentMonth - 1] + " " + currentYear);
        month.setFont(new Font("Arial", Font.BOLD, 20));
        headerCalendario.add(month, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);

        JButton btnLeftArrow = new JButton("<");
        btnLeftArrow.putClientProperty("JButton.buttonType", "roundRect");
        btnLeftArrow.addActionListener(e -> {
            currentMonth--;
            if (currentMonth < 1) { currentMonth = 12; currentYear--; }
            setCalendar(currentMonth, currentYear);
            month.setText(mesi[currentMonth - 1] + " " + currentYear);
            aggiornaCalendario();
        });
        
        JButton btnRightArrow = new JButton(">");
        btnRightArrow.putClientProperty("JButton.buttonType", "roundRect");
        btnRightArrow.addActionListener(e -> {
            currentMonth++;
            if (currentMonth > 12) { currentMonth = 1; currentYear++; }
            setCalendar(currentMonth, currentYear);
            month.setText(mesi[currentMonth - 1] + " " + currentYear);
            aggiornaCalendario();
        });
        
        JButton btnScambiaCalendario = new JButton("Vedi Lista");
        btnScambiaCalendario.putClientProperty("JButton.buttonType", "roundRect");
        btnScambiaCalendario.setIcon(new FlatSVGIcon("icone/list.svg", 18, 18));
        btnScambiaCalendario.addActionListener(e -> scambiaViste(btnScambiaCalendario));
        
        btnPanel.add(btnLeftArrow);
        btnPanel.add(btnRightArrow);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(btnScambiaCalendario);
        headerCalendario.add(btnPanel, BorderLayout.EAST);

        bottomPanel.add(headerCalendario, BorderLayout.NORTH);
        
        JPanel wrapperCal = new JPanel(new BorderLayout());
        wrapperCal.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapperCal.add(calendar, BorderLayout.CENTER);
        bottomPanel.add(wrapperCal, BorderLayout.CENTER);

        // --- Card Panel ---
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(contenitoreLista, "LISTA");
        cardPanel.add(bottomPanel, "CALENDARIO");
        cardLayout.show(cardPanel, "LISTA");

        this.add(moduloPanel, BorderLayout.NORTH);
        this.add(cardPanel, BorderLayout.CENTER);

        // --- LOGICA FINALE (Quella che mancava!) ---
        setupResponsiveLayout();
        initListaScadenze();
        SwingUtilities.invokeLater(this::applyResponsiveLayout);

        btnSalva.addActionListener(e -> {
            String esameSelezionato = (String) comboEsami.getSelectedItem();
            LocalDate dataSelezionata = datePicker.getDate();
            if (esameSelezionato == null || dataSelezionata == null) {
                DialoghiModerni.mostraMessaggio(this, "Attenzione!", "Selezione sia un esame che una data valida!", true);
                return;
            }
            GestoreDatabase.salvaScadenza(esameSelezionato, dataSelezionata.toString());
            datePicker.clear();
            aggiornaListaScadenze();
            GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
        });

        btnOrdina.addActionListener(e -> {
            ordinaPerData = !ordinaPerData; 
            GestoreDatabase.salvaOrdineScadenze(ordinaPerData);
            btnOrdina.setText(ordinaPerData ? "Ordina: Aggiunta" : "Ordina: Cronologico");
            aggiornaListaScadenze();
        });
    }

    public void setCalendar(int numMese, int anno) {
        boolean isBisestile = java.time.Year.of(anno).isLeap();
        numGiorni = 0;
        if (numMese == 4 || numMese == 6 || numMese == 9 || numMese == 11)
            numGiorni = 30;
        else if (isBisestile && numMese == 2)
            numGiorni = 29;
        else if (!isBisestile && numMese == 2)
            numGiorni = 28;
        else
            numGiorni = 31;

        // Creiamo la griglia del calendario (7 colonne per i giorni della settimana)
        JPanel newCalendar = new JPanel(new java.awt.GridLayout(0, 7, 5, 5));
        newCalendar.setOpaque(false);

        JPanel[] newPnlGiorni = new JPanel[numGiorni];
        java.time.LocalDate oggi = java.time.LocalDate.now();

        boolean temaScuro = com.minec.dati.GestoreDatabase.isTemaScuro();

        for (int i = 0; i < numGiorni; i++) {
            JPanel p = new JPanel(new java.awt.BorderLayout());
            p.setPreferredSize(new java.awt.Dimension(40, 40));

            // Colori moderni per i quadratini dei giorni
            p.setBackground(temaScuro ? new java.awt.Color(60, 63, 65) : java.awt.Color.WHITE);

            // Evidenziamo il giorno corrente in rosso, gli altri con un bordo grigio
            if (i + 1 == oggi.getDayOfMonth() && oggi.getMonthValue() == currentMonth
                    && oggi.getYear() == currentYear) {
                p.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(231, 76, 60), 2, true));
            } else {
                p.setBorder(javax.swing.BorderFactory.createLineBorder(
                        temaScuro ? new java.awt.Color(80, 80, 80) : new java.awt.Color(230, 230, 230), 1, true));
            }

            JLabel date = new JLabel(String.valueOf(i + 1), javax.swing.SwingConstants.LEFT);
            date.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 0, 0));
            date.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            p.add(date, java.awt.BorderLayout.NORTH);

            newPnlGiorni[i] = p;
            newCalendar.add(p);
        }

        // Sostituiamo il vecchio calendario con quello nuovo
        JPanel oldCalendar = this.calendar;
        this.calendar = newCalendar;
        this.pnlGiorni = newPnlGiorni;

        if (oldCalendar != null && oldCalendar.getParent() != null) {
            java.awt.Container parent = oldCalendar.getParent();
            parent.remove(oldCalendar);
            parent.add(this.calendar, java.awt.BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        }
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

        if (btnCalendario != null) {
            btnCalendario.setIcon(new FlatSVGIcon("icone/calendar.svg", Math.max(20, Math.round(24 * currentScale)), Math.max(20, Math.round(24 * currentScale))));
        }

        if (btnOrdina != null) {
            btnOrdina.setFont(btnOrdina.getFont().deriveFont(Math.max(12f, 12f * currentScale)));
        }

        if (listaTitledBorder != null) {
            listaTitledBorder.setTitleFont(new Font("Arial", Font.BOLD, Math.max(16, Math.round(16 * currentScale))));
        }
        if (calendar != null) {
            int calWidth = Math.max(200, getWidth() - 10);
            int calHeight = Math.max(120, Math.round(280 * currentScale));
            calendar.setPreferredSize(new Dimension(calWidth, calHeight));
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
        if (GestoreDatabase.isTemaScuro()) {
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
        String[] esamiRaw = GestoreDatabase.getEsamiSalvatiRaw();
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
        String[] scadenzeRaw = GestoreDatabase.getScadenzeRaw();
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
                // Colore di sfondo della card e bordo arrotondato
                boolean temaScuro = GestoreDatabase.isTemaScuro();
                panel.setBackground(temaScuro ? new Color(48, 50, 54) : Color.WHITE);
                panel.setBorder(BorderFactory.createLineBorder(temaScuro ? new Color(70, 70, 75) : new Color(220, 220, 220), 1, true));

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
                JButton btnRimuovi = new JButton();
                try {
                    btnRimuovi.setIcon(new FlatSVGIcon("icone/bin1.svg", 22, 22));
                } catch (Exception e) {
                    btnRimuovi.setText("X");
                }
                btnRimuovi.putClientProperty("JButton.buttonType", "toolBarButton"); // Piatto
                btnRimuovi.setForeground(new Color(211, 47, 47));
                btnRimuovi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

                btnRimuovi.addActionListener(e -> {
                    boolean conferma = DialoghiModerni.chiediConferma(this, "Conferma rimozione",
                            "Vuoi davvero rimuovere la data per " + nomeEsame + "?", "Si, elimina", true);

                    if (conferma) {
                        GestoreDatabase.removeScadenza(nomeEsame);
                        aggiornaListaScadenze();
                        GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
                    }
                });

                JPanel pannelloDestra = new JPanel();
                pannelloDestra.setLayout(new BoxLayout(pannelloDestra, BoxLayout.X_AXIS));
                pannelloDestra.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
                pannelloDestra.setBackground(panel.getBackground());

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
        aggiornaCalendario();
    }

    private void aggiornaCalendario() {
        if (pnlGiorni == null || calendar == null) return;

        // Raccogli tutti gli esami per ogni giorno
        Map<Integer, List<String>> esamiPerGiorno = new HashMap<>();
        for (int i = 1; i <= pnlGiorni.length; i++) {
            esamiPerGiorno.put(i, new ArrayList<>());
        }

        // Leggi gli esami dal database
        String[] scadenzeRaw = GestoreDatabase.getScadenzeRaw();
        for (String riga : scadenzeRaw) {
            String[] parti = riga.split(";");
            if (parti.length >= 2) {
                try {
                    LocalDate data = LocalDate.parse(parti[1]);
                    if (data.getYear() == currentYear && data.getMonthValue() == currentMonth) {
                        int giorno = data.getDayOfMonth();
                        if (giorno >= 1 && giorno <= pnlGiorni.length) {
                            esamiPerGiorno.get(giorno).add(parti[0]);
                        }
                    }
                } catch (Exception ex) {
                    // ignoriamo righe non parseable
                }
            }
        }

        // Popola i pannelli dei giorni
        LocalDate oggi = LocalDate.now();
        for (int i = 0; i < pnlGiorni.length; i++) {
            JPanel p = pnlGiorni[i];
            p.removeAll();
            p.setLayout(new BorderLayout());

            // Aggiungi il numero del giorno in NORTH (a sinistra)
            JLabel lblDay = new JLabel("" + (i + 1));
            lblDay.setBorder(new EmptyBorder(4, 6, 0, 0));
            lblDay.setHorizontalAlignment(JLabel.LEFT);
            if(i+1 == oggi.getDayOfMonth() && currentMonth == oggi.getMonthValue()) {
                lblDay.setFont(new Font("Arial", Font.BOLD, 14));
                lblDay.setForeground(Color.RED);
            }
            p.add(lblDay, BorderLayout.NORTH);

            // Aggiungi gli esami per questo giorno
            List<String> esami = esamiPerGiorno.get(i + 1);
            if (esami.size() == 1) {
                // Un singolo esame: mostra direttamente senza scrollpane
                String nomeEsame = esami.get(0);
                JLabel lblExam = new JLabel("<html><div style='width: 70px; text-align: center;'>- " + nomeEsame + "</div></html>");
                lblExam.setBorder(new EmptyBorder(2, 2, 2, 2));
                lblExam.setOpaque(false);
                lblExam.setFont(new Font("Arial", Font.PLAIN, 12));
                lblExam.setVerticalAlignment(JLabel.CENTER);
                p.add(lblExam, BorderLayout.CENTER);
            } else if (esami.size() >= 2) {
                // Due o più esami: usa uno scrollpane
                JPanel esamiPanel = new JPanel();
                esamiPanel.setLayout(new BoxLayout(esamiPanel, BoxLayout.Y_AXIS));
                for (String nomeEsame : esami) {
                    JLabel lblExam = new JLabel("<html><div style='width: 70px; text-align: center;'>- " + nomeEsame + "</div></html>");
                    lblExam.setBorder(new EmptyBorder(2, 2, 2, 2));
                    lblExam.setOpaque(false);
                    lblExam.setFont(new Font("Arial", Font.PLAIN, 12));
                    lblExam.setVerticalAlignment(JLabel.CENTER);
                    esamiPanel.add(lblExam);
                }
                JScrollPane scrollPane = new JScrollPane(esamiPanel);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                p.add(scrollPane, BorderLayout.CENTER);
            }
        }

        calendar.revalidate();
        calendar.repaint();
    }

    public void refreshOrdineScadenze() {
        ordinaPerData = GestoreDatabase.getOrdineScadenza();
        if (btnOrdina != null) {
            btnOrdina.setText(ordinaPerData ? "Ordina: Aggiunta" : "Ordina: Cronologico");
        }
        aggiornaListaScadenze();
    }

    private void scambiaViste(JButton button) {
        showLista = !showLista;
        if (showLista) {
            cardLayout.show(cardPanel, "LISTA");
            aggiornaListaScadenze();
        } else {
            cardLayout.show(cardPanel, "CALENDARIO");
            aggiornaCalendario();
        }
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        applicaTemaCampoData();
        if (currentMonth > 0 && currentYear > 0) {
            setCalendar(currentMonth, currentYear);
            aggiornaCalendario();
        }
    }
}
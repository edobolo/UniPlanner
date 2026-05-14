package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.minec.GestoreNotifiche;
import com.minec.dati.GestoreDatabase;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PannelloAggiungi extends JPanel {

    private static final int BASE_WIDTH = 1280;
    private static final int BASE_HEIGHT = 720;
    private static final int BASE_EXAM_CARD_WIDTH = 750;
    private static final int BASE_EXAM_CARD_HEIGHT = 50;
    private static final int BASE_EXAM_GAP = 5;
    private static final int BASE_EXAM_SIDE_MARGIN = 16;
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 1.8f;

    private int index = 0;
    private float currentScale = 1.0f;
    private final PannelloVoti pv;
    private JPanel esamiPanel;
    private String criterioOrdinamento = "RECENTI";
    JButton btnOrdina;
    JPopupMenu menuOrdina;

    public PannelloAggiungi(PannelloVoti pv) {
        this.pv = pv;

        JPanel esamiAggiuntiPanel = new JPanel();
        JPanel aggiungiEsamePanel = new JPanel();
        this.setLayout(new BorderLayout());
        // Inizializziamo prima la struttura
        initAddedExamsLayout(esamiAggiuntiPanel);
        initAddExamLayout(aggiungiEsamePanel);


        this.add(aggiungiEsamePanel, BorderLayout.NORTH);
        this.add(esamiAggiuntiPanel, BorderLayout.CENTER);

        setupResponsiveScaling();
        // Carichiamo i dati iniziali
        refreshDataUI();

        // Prima scalatura appena il pannello viene mostrato.
        SwingUtilities.invokeLater(this::applyResponsiveScaling);
    }

    public void aggiornaTutto() {
        refreshDataUI();
    }

    private void refreshDataUI() {
        // 1. Aggiorna la lista grafica (il centro)
        esamiPanel.removeAll();
        // 2. Recupero dati
        String[] esamiRaw = GestoreDatabase.getEsamiSalvatiRaw();
        String[] votiRaw = GestoreDatabase.getVotiEsamiRaw();
        // 3. Sistemo i voti per la logica ordinamento esami
        HashMap<String, Integer> mappaVoti = new java.util.HashMap<>();
        if (votiRaw != null) {
            for (String riga : votiRaw) {
                if (riga != null) {
                    String[] p = riga.split(";");
                    if (p.length >= 2) {
                        String votoStr = p[0];
                        String nomeEsame = p[1];
                        int votoNum = -1; // -1 = senza voto / da dare
                        if (votoStr.equals("30L"))
                            votoNum = 31; // Diamo 31 per farlo stare sopra il 30
                        else if (votoStr.equals("IDONEO"))
                            votoNum = 0; // Le idoneità valgono 0 ai fini della classifica
                        else {
                            try {
                                votoNum = Integer.parseInt(votoStr);
                            } catch (Exception e) {
                            }
                        }
                        mappaVoti.put(nomeEsame, votoNum);
                    }
                }
            }
        }
        // 4. Creo lista dinamica per ordinarla
        List<String> listaEsami = new java.util.ArrayList<>();
        if (esamiRaw != null) {
            for (String s : esamiRaw) {
                if (s != null && !s.trim().isEmpty()) {
                    listaEsami.add(s);
                }
            }
        }
        // 5. Logica di ordinamento
        if (!criterioOrdinamento.equals("RECENTI")) {
            listaEsami.sort((raw1, raw2) -> {
                String nome1 = raw1.split(";")[0];
                String nome2 = raw2.split(";")[0];

                if (criterioOrdinamento.equals("NOME")) {
                    return nome1.compareToIgnoreCase(nome2); // Ordine Alfabetico A-Z
                } 
                if (criterioOrdinamento.startsWith("ANNO")) {
                    String anno1 = (raw1.split(";").length > 3) ? raw1.split(";")[3] : "N/D";
                    String anno2 = (raw2.split(";").length > 3) ? raw2.split(";")[3] : "N/D";

                    // Identifichiamo i tag che non sono anni numerici
                    boolean senzaAnno1 = anno1.equals("N/D") || anno1.equals("Opzionale");
                    boolean senzaAnno2 = anno2.equals("N/D") || anno2.equals("Opzionale");

                    // Se uno dei due non ha un anno numerico, lo forziamo SEMPRE in fondo
                    if (senzaAnno1 && !senzaAnno2)
                        return 1;
                    if (senzaAnno2 && !senzaAnno1)
                        return -1;

                    int confrontoAnno;
                    if (criterioOrdinamento.equals("ANNO_ASC")) {
                        confrontoAnno = anno1.compareTo(anno2);
                    } else {
                        confrontoAnno = anno2.compareTo(anno1);
                    }

                    // Se l'anno è lo stesso (o se sono entrambi N/D), ordiniamo alfabeticamente per
                    // nome
                    if (confrontoAnno == 0) {
                        return nome1.compareToIgnoreCase(nome2);
                    }
                    return confrontoAnno;
                }else {
                    int v1 = mappaVoti.getOrDefault(nome1, -1);
                    int v2 = mappaVoti.getOrDefault(nome2, -1);

                    // Trucco: Mettiamo gli esami "Da fare" (voto -1) SEMPRE in fondo
                    if (v1 == -1 && v2 != -1)
                        return 1;
                    if (v2 == -1 && v1 != -1)
                        return -1;
                    // Se entrambi non hanno voto, li mettiamo in ordine alfabetico
                    if (v1 == -1 && v2 == -1)
                        return nome1.compareToIgnoreCase(nome2);

                    if (criterioOrdinamento.equals("VOTO_DESC")) {
                        return Integer.compare(v2, v1); // Dal più alto al più basso
                    } else if (criterioOrdinamento.equals("VOTO_ASC")) {
                        return Integer.compare(v1, v2); // Dal più basso al più alto
                    }
                }
                return 0;
            });
        }
        // 6. Disegniamo gli esami (ora ordinati) su schermo
        index = 0;
        for (String s : listaEsami) {
            disegnaEsameSuSchermo(s);
            index++;
        }
        applyExamRowScaling();
        // 7. Forza il ridisegno della UI
        esamiPanel.revalidate();
        esamiPanel.repaint();
        GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
    }

    private void disegnaEsameSuSchermo(String raw) {
        String[] parti = raw.split(";");
        String nome = parti[0];
        boolean isCompletato = estraiCompletato(parti);
        boolean isIdoneita = estraiIdoneita(parti);
        String annoEsame = (parti.length > 3) ? parti[3] : "N/D";

        // --- Recupero CFU e Voto salvati per la visualizzazione ---
        int cfuSalvati = 0;
        String votoSalvato = "";
        String[] votiRaw = GestoreDatabase.getVotiEsamiRaw();
        for (String rigaVoto : votiRaw) {
            String[] pVoto = rigaVoto.split(";");
            if (pVoto.length >= 2 && pVoto[1].equals(nome)) {
                votoSalvato = pVoto[0];
                if (pVoto.length > 2)
                    try {
                        cfuSalvati = Integer.parseInt(pVoto[2]);
                    } catch (Exception e) {
                    }
                break;
            }
        }

        // --- STILE CARD MODERNA ---
        JPanel panelSingoloEsame = new JPanel(new BorderLayout());
        Dimension dimensioneCard = getScaledExamCardDimension();
        panelSingoloEsame.setPreferredSize(dimensioneCard);
        panelSingoloEsame.setMaximumSize(dimensioneCard);

        boolean temaScuro = GestoreDatabase.isTemaScuro();
        Color cardBg = temaScuro ? new Color(48, 50, 54) : Color.WHITE;
        panelSingoloEsame.setBackground(cardBg);
        panelSingoloEsame
                .setBorder(BorderFactory.createLineBorder(temaScuro ? new Color(80, 80, 80) : Color.LIGHT_GRAY, 1));

        // --- SINISTRA: Nome Esame ---
        JPanel pSinistra = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        pSinistra.setOpaque(false);

        JLabel badgeAnno = new JLabel(" " + annoEsame + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Bordi fluidi
                g2.setColor(getBackground());
                // Disegna la forma a pillola
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g); // Disegna il testo sopra
            }
        };
        badgeAnno.setOpaque(true);
        if(annoEsame.startsWith("1")) {
            badgeAnno.setBackground(Color.GREEN);
            badgeAnno.setForeground(Color.BLACK);
        } 
        else if(annoEsame.startsWith("2")) {
            badgeAnno.setBackground(Color.YELLOW);
            badgeAnno.setForeground(Color.BLACK);
        }
        else if(annoEsame.startsWith("3")) {
            badgeAnno.setBackground(Color.RED);
            badgeAnno.setForeground(Color.WHITE);
        } 
        else if(annoEsame.startsWith("4")) {
            badgeAnno.setBackground(new Color(101, 0, 3));
            badgeAnno.setForeground(Color.WHITE);
        } 
        else if(annoEsame.startsWith("5")) {
            badgeAnno.setBackground(new Color(120, 0, 139));
            badgeAnno.setForeground(Color.WHITE);
        } else {
            badgeAnno.setBackground(new Color(100, 116, 139));
            badgeAnno.setForeground(Color.WHITE);
        }
        //TODO: rendere modificabili i tag premendoci sopra
        badgeAnno.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeAnno.setCursor(new Cursor(Cursor.HAND_CURSOR));
        badgeAnno.setToolTipText("Clicca per cambiare l'anno");

        JPopupMenu menuCambioAnno = new JPopupMenu();
        String[] anniScelta = { "1° Anno", "2° Anno", "3° Anno", "4° Anno", "5° Anno", "Opzionale", "N/D" };

        for (String annoSelezionato : anniScelta) {
            JMenuItem itemAnno = new JMenuItem(annoSelezionato);

            // Se la voce del menu è uguale all'anno attuale dell'esame, la mettiamo in
            // grassetto
            if (annoSelezionato.equals(annoEsame)) {
                itemAnno.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }

            itemAnno.addActionListener(e -> {
                // Quando clicchi una voce, aggiorna il DB e ricarica le schermate
                GestoreDatabase.aggiornaAnnoEsame(nome, annoSelezionato);
                aggiornaTutto();
                pv.refresh();
            });
            menuCambioAnno.add(itemAnno);
        }

        // Mostriamo il menu quando si clicca sul badge
        badgeAnno.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                menuCambioAnno.show(badgeAnno, 0, badgeAnno.getHeight());
            }
        });
        
        pSinistra.add(badgeAnno);

        JLabel nomeEsameLabel = new JLabel(nome);
        nomeEsameLabel.setFont(new Font("Arial", isCompletato ? Font.ITALIC : Font.BOLD, 18));
        nomeEsameLabel.setForeground(temaScuro ? new Color(230, 230, 230) : Color.DARK_GRAY);
        if (isCompletato) {
            Map<TextAttribute, Object> attributes = new HashMap<>();
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            nomeEsameLabel.setFont(nomeEsameLabel.getFont().deriveFont(attributes));
        }
        pSinistra.add(nomeEsameLabel);

        // --- DESTRA: Area Interattiva / Bottone e Cestino ---
        JPanel pDestra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pDestra.setOpaque(false);

        if (isCompletato) {
            // 1. SE L'ESAME È FATTO: Mostriamo la scritta interattiva verde per la modifica
            JPanel pnlInterattivo = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
            pnlInterattivo.setOpaque(false);

            String testo = (isIdoneita ? "IDONEO" : "Voto: " + votoSalvato) + " (" + cfuSalvati + " CFU)";
            JLabel lblScritta = new JLabel(testo);
            lblScritta.setFont(new Font("Arial", Font.BOLD, 14));
            lblScritta.setForeground(new Color(76, 175, 80));
            pnlInterattivo.add(lblScritta);

            pnlInterattivo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pnlInterattivo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    registraEsame(nome, isIdoneita);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    pnlInterattivo.setOpaque(true);
                    pnlInterattivo.setBackground(temaScuro ? new Color(60, 63, 67) : new Color(242, 242, 242));
                    pnlInterattivo.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    pnlInterattivo.setOpaque(false);
                    pnlInterattivo.repaint();
                }
            });
            pDestra.add(pnlInterattivo);

        } else {
            // 2. SE L'ESAME È DA FARE: Bottone pulito originale di FlatLaf
            JButton btnRegistra = new JButton("Registra Voto");
            btnRegistra.putClientProperty("JButton.buttonType", "roundRect");

            btnRegistra.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRegistra.addActionListener(e -> registraEsame(nome, isIdoneita));
            pDestra.add(btnRegistra);
        }

        // --- BOTTONE CESTINO (ELIMINA) ---
        JButton btnElimina = new JButton();
        try {
            btnElimina.setIcon(new FlatSVGIcon("icone/bin1.svg", 18, 18));
        } catch (Exception e) {
            btnElimina.setText("🗑");
        }
        btnElimina.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnElimina.putClientProperty("JButton.buttonType", "toolBarButton");
        btnElimina.setForeground(new Color(211, 47, 47));
        btnElimina.addActionListener(e -> {
            if (DialoghiModerni.chiediConferma(this, "Conferma rimozione", 
                "Eliminare definitivamente ", 
                "Si, elimina", true)) {
                GestoreDatabase.removeNomeEsame(nome);
                GestoreDatabase.removeVotiEsame(nome);
                aggiornaTutto();
                pv.refresh();
            }
        });
        pDestra.add(btnElimina);

        panelSingoloEsame.add(pSinistra, BorderLayout.WEST);
        panelSingoloEsame.add(pDestra, BorderLayout.EAST);
        esamiPanel.add(panelSingoloEsame);
        esamiPanel.add(Box.createRigidArea(new Dimension(0, getScaledExamGap())));
    }

    private void registraEsame(String nome, boolean isIdoneita) {
        // --- CASO 1: IDONEITÀ ---
        if (isIdoneita) {
            String cfu = DialoghiModerni.chiediInput(this, "Registra", "Modifica CFU per Idoneità (" + nome + "):", "Conferma",
                    "");
            if (cfu != null) {
                GestoreDatabase.removeVotiEsame(nome);
                GestoreDatabase.setVotiEsami("IDONEO", nome, 0);
                GestoreDatabase.addCfuEsame(nome, Integer.parseInt(cfu));
                GestoreDatabase.aggiornaStatoEsame(nome, true);
            } else {
                aggiornaTutto(); // Se annullo, resetto la grafica della checkbox
                return;
            }
        }
        // --- CASO 2: ESAME NORMALE (CON VOTO) ---
        else {
            String votoPulito = "";
            boolean votoValido = false;

            // Ciclo che richiede il voto finché non è valido o finché non si annulla
            while (!votoValido) {
                String voto = DialoghiModerni.chiediInput(this, "Modifica Voto",
                    "Inserisci/Modifica il voto per " + nome + "(18-30L):",
                    "Conferma", "");

                // Se l'utente preme "Annulla" o chiude la finestra
                if (voto == null) {
                    aggiornaTutto(); // Resetta la checkbox
                    return;
                }

                votoPulito = voto.trim().toUpperCase();
                if (votoPulito.equals("30 E LODE"))
                    votoPulito = "30L";

                // VALIDAZIONE
                if (votoPulito.equals("30L")) {
                    votoValido = true;
                } else {
                    try {
                        int v = Integer.parseInt(votoPulito);
                        if (v >= 18 && v <= 30) {
                            votoValido = true;
                        } else {
                            DialoghiModerni.mostraMessaggio(this, "Attenzione!", 
                                    "Errore: Il voto deve essere compreso tra 18 e 30", true);
                        }
                    } catch (NumberFormatException ex) {
                        DialoghiModerni.mostraMessaggio(this, "Attenzione!", 
                                "Errore: Inserisci un numero tra 18 e 30, oppure '30L'", true);
                    }
                }
            }

            String cfu = DialoghiModerni.chiediInput(this, "Selezione CFU", "Seleziona i CFU per " + nome + "(Max 18):",
                    "Conferma", "");

            if (cfu != null) {
                GestoreDatabase.removeVotiEsame(nome);
                GestoreDatabase.setVotiEsami(votoPulito, nome, 0);
                GestoreDatabase.addCfuEsame(nome, Integer.parseInt(cfu));
                GestoreDatabase.aggiornaStatoEsame(nome, true);
            } else {
                aggiornaTutto(); // Se annullo sui CFU, resetto la checkbox
                return;
            }
        }

        // Aggiornamento finale di tutti i dati grafici se il salvataggio è andato a
        // buon fine
        aggiornaTutto();
        pv.refresh();
        GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
    }

    private void setupResponsiveScaling() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveScaling();
            }
        });
    }
    //per scalare la dimensione dei componenti in base alla dimensione della finestra
    private void applyResponsiveScaling() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        float scaleX = (float) getWidth() / BASE_WIDTH;
        float scaleY = (float) getHeight() / BASE_HEIGHT;
        currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.min(scaleX, scaleY)));
        scaleFontsRecursively(this, currentScale);
        applyExamRowScaling();
        revalidate();
        repaint();
    }

    private Dimension getScaledExamCardDimension() {
        int targetWidth = Math.round(BASE_EXAM_CARD_WIDTH * currentScale);
        int targetHeight = Math.max(42, Math.round(BASE_EXAM_CARD_HEIGHT * currentScale));

        if (esamiPanel != null && esamiPanel.getParent() != null) {
            int sideMargin = getScaledExamSideMargin();
            int availableWidth = esamiPanel.getParent().getWidth() - (sideMargin * 2) - 8;
            if (availableWidth > 0) {
                targetWidth = Math.max(300, availableWidth);
            }
        }

        return new Dimension(targetWidth, targetHeight);
    }

    private int getScaledExamGap() {
        return Math.max(4, Math.round(BASE_EXAM_GAP * currentScale));
    }

    private int getScaledExamSideMargin() {
        return Math.max(10, Math.round(BASE_EXAM_SIDE_MARGIN * currentScale));
    }

    private boolean estraiCompletato(String[] parti) {
        if (parti.length > 1) {
            if ("true".equalsIgnoreCase(parti[1]) || "false".equalsIgnoreCase(parti[1])) {
                return Boolean.parseBoolean(parti[1]);
            }
            if (parti[1].startsWith("true")) {
                return true;
            }
        }
        return false;
    }

    private boolean estraiIdoneita(String[] parti) {
        if (parti.length > 2) {
            return Boolean.parseBoolean(parti[2]);
        }
        if (parti.length > 1) {
            // Compatibilita con vecchio formato errato: "falsetrue" o "truetrue"
            return parti[1].endsWith("true");
        }
        return false;
    }

    private void applyExamRowScaling() {
        if (esamiPanel == null) {
            return;
        }

        Dimension rowSize = getScaledExamCardDimension();
        Dimension gapSize = new Dimension(0, getScaledExamGap());
        int sideMargin = getScaledExamSideMargin();

        esamiPanel.setBorder(BorderFactory.createEmptyBorder(0, sideMargin, 0, sideMargin));

        for (Component child : esamiPanel.getComponents()) {
            if (JPanel.class.isInstance(child)) {
                JPanel rowPanel = JPanel.class.cast(child);
                rowPanel.setPreferredSize(rowSize);
                rowPanel.setMaximumSize(rowSize);
                rowPanel.setMinimumSize(rowSize);
                rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            } else if (Box.Filler.class.isInstance(child)) {
                Box.Filler spacer = Box.Filler.class.cast(child);
                spacer.changeShape(gapSize, gapSize, gapSize);
            }
        }
    }

    private void scaleFontsRecursively(Component component, float scale) {
        if (component instanceof JComponent jc) {
            Font baseFont = (Font) jc.getClientProperty("baseFont");
            if (baseFont == null && jc.getFont() != null) {
                baseFont = jc.getFont();
                jc.putClientProperty("baseFont", baseFont);
            }

            if (baseFont != null) {
                float scaledSize = Math.max(11f, baseFont.getSize2D() * scale);
                jc.setFont(baseFont.deriveFont(scaledSize));
            }
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                scaleFontsRecursively(child, scale);
            }
        }
    }

    private void initAddExamLayout(JPanel aggiungiEsame) {
        aggiungiEsame.setLayout(new BorderLayout());
        aggiungiEsame.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Titolo
        JLabel title = new JLabel("I Miei Esami", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        aggiungiEsame.add(title, BorderLayout.NORTH);

        // Riga di inserimento
        JPanel rigaInput = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JTextField campoNome = new JTextField(20);
        campoNome.setPreferredSize(new Dimension(250, 36));
        campoNome.putClientProperty("JTextField.placeholderText", "Nome nuovo esame...");
        campoNome.putClientProperty("JTextField.showClearButton", true);

        JCheckBox checkIdoneita = new JCheckBox("Idoneità");
        checkIdoneita.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- NUOVO: Selettore dell'Anno ---
        String[] anniDisponibili = {"1° Anno", "2° Anno", "3° Anno", "4° Anno", "5° Anno", "Opzionale"};
        JComboBox<String> comboAnno = new JComboBox<>(anniDisponibili);
        comboAnno.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comboAnno.setPreferredSize(new Dimension(90, 32));
        comboAnno.setMaximumSize(new Dimension(90, 32));
        comboAnno.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboAnno.putClientProperty("JComponent.roundRect", true);
        comboAnno.putClientProperty("FlatLaf.style", "arc: 999; focusWidth: 0;");

        JButton btnSalva = new JButton("Aggiungi Esame");
        btnSalva.putClientProperty("JButton.buttonType", "roundRect");
        btnSalva.setBackground(new Color(33, 150, 243));
        btnSalva.setForeground(Color.WHITE);
        btnSalva.setFont(new Font("Arial", Font.BOLD, 14));
        btnSalva.setPreferredSize(new Dimension(150, 36));
        btnSalva.setCursor(new Cursor(Cursor.HAND_CURSOR));

        rigaInput.add(campoNome);
        rigaInput.add(comboAnno);
        rigaInput.add(checkIdoneita);
        rigaInput.add(btnSalva);
        aggiungiEsame.add(rigaInput, BorderLayout.CENTER);

        // Azione Salva
        btnSalva.addActionListener(e -> {
            String nome = campoNome.getText().trim();
            String annoScelto = comboAnno.getSelectedItem().toString();
            if (nome.isEmpty()) {
                DialoghiModerni.mostraMessaggio(this, "Attenzione!", "Inserisci un nome!", true);
                return;
            }
            if (index < 40) {
                GestoreDatabase.salvaEsame(nome, checkIdoneita.isSelected(), annoScelto);
                checkIdoneita.setSelected(false);
                campoNome.setText("");
                aggiornaTutto();
                pv.refresh();
            }
        });
    }

    private void initAddedExamsLayout(JPanel esamiAggiunti) {
        esamiAggiunti.setLayout(new BorderLayout());
        esamiAggiunti.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // --- Intestazione Moderna (Header) ---
        JPanel headerLista = new JPanel(new BorderLayout());
        headerLista.setOpaque(false);
        headerLista.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));

        JLabel lblTitolo = new JLabel("Esami nel Libretto");
        lblTitolo.setFont(new Font("Arial", Font.BOLD, 20));
        headerLista.add(lblTitolo, BorderLayout.WEST);

        // --- IL NUOVO BOTTONE E MENU DI ORDINAMENTO ---
        btnOrdina = new JButton("Ordina ▼");
        btnOrdina.putClientProperty("FlatLaf.style", "arc: 999; focusWidth: 0;");
        btnOrdina.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOrdina.setCursor(new Cursor(Cursor.HAND_CURSOR));

        menuOrdina = new JPopupMenu();
        JMenuItem mnuNome = new JMenuItem("Alfabetico (A-Z)");
        JMenuItem mnuVotoAlto = new JMenuItem("Voto (Dal più alto)");
        JMenuItem mnuVotoBasso = new JMenuItem("Voto (Dal più basso)");
        JMenuItem mnuAnnoAsc = new JMenuItem("Anno (1° → 5°)");
        JMenuItem mnuAnnoDesc = new JMenuItem("Anno (5° → 1°)");
        JMenuItem mnuRecenti = new JMenuItem("Aggiunti di recente");

        menuOrdina.add(mnuNome);
        menuOrdina.add(mnuVotoAlto);
        menuOrdina.add(mnuVotoBasso);
        menuOrdina.add(mnuAnnoAsc);
        menuOrdina.add(mnuAnnoDesc);
        menuOrdina.addSeparator();
        menuOrdina.add(mnuRecenti);

        // Mostra il menu al click
        btnOrdina.addActionListener(e -> {menuOrdina.show(btnOrdina, 0, btnOrdina.getHeight());});

        // Azioni delle voci
        mnuNome.addActionListener(e -> { criterioOrdinamento = "NOME"; refreshDataUI(); });
        mnuVotoAlto.addActionListener(e -> { criterioOrdinamento = "VOTO_DESC"; refreshDataUI(); });
        mnuVotoBasso.addActionListener(e -> { criterioOrdinamento = "VOTO_ASC"; refreshDataUI(); });
        mnuAnnoAsc.addActionListener(e -> { criterioOrdinamento = "ANNO_ASC"; refreshDataUI(); });
        mnuAnnoDesc.addActionListener(e -> { criterioOrdinamento = "ANNO_DESC"; refreshDataUI(); });
        mnuRecenti.addActionListener(e -> { criterioOrdinamento = "RECENTI"; refreshDataUI(); });

        // --- ANIMAZIONE FRECCIA DEL MENU ---
        menuOrdina.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                // Quando il menu si apre, la freccia punta in alto
                btnOrdina.setText("Ordina ▲");
            }
            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                // Quando il menu si chiude, la freccia torna in basso
                btnOrdina.setText("Ordina ▼");
            }
            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                // Se annulli cliccando fuori dal menu, la freccia torna in basso
                btnOrdina.setText("Ordina ▼");
            }
        });

        headerLista.add(btnOrdina, BorderLayout.EAST); // Aggiunto a DESTRA dell'header

        esamiAggiunti.add(headerLista, BorderLayout.NORTH);
        

        // --- Area della Lista (Scroll) ---
        esamiPanel = new JPanel();
        esamiPanel.setLayout(new BoxLayout(esamiPanel, BoxLayout.Y_AXIS));
        esamiPanel.setOpaque(false);
        // Togliamo il margine superiore da qui...
        esamiPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(esamiPanel);
        // ...E LO SPOSTIAMO QUI! (15 pixel di distanza dalla linea superiore)
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        boolean temaScuro = GestoreDatabase.isTemaScuro();
        Color bg = temaScuro ? new Color(34, 37, 43) : Color.WHITE;
        scrollPane.setBackground(bg);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        esamiAggiunti.add(scrollPane, BorderLayout.CENTER);
    }
}
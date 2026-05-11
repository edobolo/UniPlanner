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
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.minec.GestoreNotifiche;
import com.minec.dati.GestoreDatabase;
import com.formdev.flatlaf.extras.FlatSVGIcon;
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
    private static final int BASE_EXAM_HORIZONTAL_PADDING = 10;
    private static final int BASE_EXAM_VERTICAL_PADDING = 10;
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 1.8f;

    private int index = 0;
    private int cfuSalvati;
    private String votoSalvato;
    private float currentScale = 1.0f;
    private final PannelloVoti pv;
    private JPanel esamiPanel;
    private JComboBox<String> tendina;

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
        String[] esami = GestoreDatabase.getEsamiSalvatiRaw();
        index = 0;
        for (String s : esami) {
            if (s != null) {
                disegnaEsameSuSchermo(s);
                index++;
            }
        }
        applyExamRowScaling();

        // 2. Forza il ridisegno della UI
        esamiPanel.revalidate();
        esamiPanel.repaint();
        GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
    }

    private void disegnaEsameSuSchermo(String raw) {
        String[] parti = raw.split(";");
        String nome = parti[0];
        boolean isCompletato = estraiCompletato(parti);
        boolean isIdoneita = estraiIdoneita(parti);

        // Recupero CFU/Voto
        cfuSalvati = 0;
        votoSalvato = "";
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

        JPanel panelSingoloEsame = new JPanel(new BorderLayout());
        Dimension dimensioneCard = getScaledExamCardDimension();
        panelSingoloEsame.setPreferredSize(dimensioneCard);
        panelSingoloEsame.setMaximumSize(dimensioneCard);

        boolean temaScuro = GestoreDatabase.isTemaScuro();
        Color cardBg = temaScuro ? new Color(48, 50, 54) : Color.WHITE;
        panelSingoloEsame.setBackground(cardBg);
        panelSingoloEsame.setBorder(
                BorderFactory.createLineBorder(temaScuro ? new Color(70, 70, 75) : new Color(220, 220, 220), 1, true));

        // --- SINISTRA: Nome Esame ---
        JLabel nomeEsameLabel = new JLabel(nome);
        nomeEsameLabel.setFont(new Font("Arial", isCompletato ? Font.ITALIC : Font.BOLD, 18));
        nomeEsameLabel.setForeground(temaScuro ? new Color(230, 230, 230) : Color.DARK_GRAY);
        if (isCompletato) {
            // Effetto sbarrato se completato
            Map<TextAttribute, Object> attributes = new HashMap<>();
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            nomeEsameLabel.setFont(nomeEsameLabel.getFont().deriveFont(attributes));
        }

        // --- DESTRA: Info e Azioni ---
        JPanel pannelloAzioni = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 13));
        pannelloAzioni.setOpaque(false);
        pannelloAzioni.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Testo Stato (Voto e CFU)
        if (isCompletato) {
            String info = (isIdoneita ? "IDONEO" : "Voto: " + votoSalvato) + " (" + cfuSalvati + " CFU)";
            JLabel labelInfo = new JLabel(info);
            labelInfo.setFont(new Font("Arial", Font.BOLD, 14));
            labelInfo.setForeground(new Color(76, 175, 80)); // Verde
            labelInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            labelInfo.setToolTipText("Clicca per modificare voto/CFU");
            labelInfo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent me) {
                    if (!SwingUtilities.isLeftMouseButton(me)) return;
                    if (isIdoneita) {
                        String[] opzioni = { "1", "2", "3", "4", "5", "6", "8", "10", "12", "13", "14", "15" };
                        String scelta = (String) JOptionPane.showInputDialog(PannelloAggiungi.this,
                                "CFU per Idoneità:",
                                "Modifica CFU",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                opzioni,
                                cfuSalvati > 0 ? String.valueOf(cfuSalvati) : "6");
                        if (scelta != null) {
                            try {
                                int cfu = Integer.parseInt(scelta);
                                GestoreDatabase.setVotiEsami("IDONEO", nome, cfu);
                                GestoreDatabase.aggiornaStatoEsame(nome, true);
                                aggiornaTutto();
                                pv.refresh();
                                GestoreNotifiche.aggiornaTrofeiEAvvisa(PannelloAggiungi.this);
                            } catch (NumberFormatException ex) {
                                // ignoriamo input non validi
                            }
                        }
                    } else {
                        String nuovoVoto = JOptionPane.showInputDialog(PannelloAggiungi.this,
                                "Modifica voto (18-30 o 30L):", votoSalvato);
                        if (nuovoVoto == null) return; // annullato
                        nuovoVoto = nuovoVoto.trim().toUpperCase();
                        boolean valido = false;
                        if (nuovoVoto.equals("30L") || nuovoVoto.equals("30 E LODE")) valido = true;
                        else {
                            try {
                                int v = Integer.parseInt(nuovoVoto);
                                valido = (v >= 18 && v <= 30);
                            } catch (NumberFormatException ex) {
                                valido = false;
                            }
                        }
                        if (!valido) {
                            JOptionPane.showMessageDialog(PannelloAggiungi.this,
                                    "Voto non valido! Inserisci un numero tra 18 e 30, oppure '30L'.");
                            return;
                        }
                        String nuovaCfu = JOptionPane.showInputDialog(PannelloAggiungi.this,
                                "CFU:", cfuSalvati > 0 ? String.valueOf(cfuSalvati) : "6");
                        if (nuovaCfu == null) return;
                        try {
                            int cfu = Integer.parseInt(nuovaCfu.trim());
                            GestoreDatabase.setVotiEsami(nuovoVoto, nome, cfu);
                            GestoreDatabase.aggiornaStatoEsame(nome, true);
                            aggiornaTutto();
                            pv.refresh();
                            GestoreNotifiche.aggiornaTrofeiEAvvisa(PannelloAggiungi.this);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(PannelloAggiungi.this, "CFU non valido!");
                        }
                    }
                }
            });
            pannelloAzioni.add(labelInfo);
        } else {
            JButton btnRegistra = new JButton("Registra Voto");
            btnRegistra.putClientProperty("JButton.buttonType", "roundRect");
            btnRegistra.setFont(new Font("Arial", Font.PLAIN, 12));
            btnRegistra.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRegistra.setAlignmentY(Component.CENTER_ALIGNMENT);
            btnRegistra.addActionListener(e -> registraEsame(nome, isIdoneita));
            pannelloAzioni.add(btnRegistra);
        }

        // PULSANTE ELIMINA (Cestino)
        JButton btnElimina = new JButton();
        btnElimina.setBorder(BorderFactory.createEmptyBorder(1,0,0,0));
        // Se hai icone SVG usa FlatSVGIcon, altrimenti un semplice testo "X" o icona
        // standard
        try {
            btnElimina.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icone/bin1.svg", 22, 22));
        } catch (Exception e) {
            btnElimina.setText("🗑");
        }
        btnElimina.setToolTipText("Elimina esame");
        btnElimina.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnElimina.putClientProperty("JButton.buttonType", "toolBarButton"); // Rende il bottone piatto
        btnElimina.setForeground(new Color(211, 47, 47));
        btnElimina.setAlignmentY(Component.CENTER_ALIGNMENT);

        btnElimina.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Eliminare definitivamente " + nome + "?", "Conferma",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                GestoreDatabase.removeNomeEsame(nome);
                GestoreDatabase.removeVotiEsame(nome);
                aggiornaTutto();
                pv.refresh();
            }
        });

        pannelloAzioni.add(btnElimina);

        JPanel pSinistra = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        pSinistra.setOpaque(false);
        pSinistra.add(nomeEsameLabel);

        panelSingoloEsame.add(pSinistra, BorderLayout.WEST);
        panelSingoloEsame.add(pannelloAzioni, BorderLayout.EAST);

        esamiPanel.add(panelSingoloEsame);
        esamiPanel.add(Box.createRigidArea(new Dimension(0, getScaledExamGap())));
    }

    // Metodo di supporto per la registrazione veloce
    private void registraEsame(String nome, boolean isIdoneita) {
        if (isIdoneita) {
            String[] opzioni = { "1", "2", "3", "4", "5", "6", "8", "10", "12", "13", "14", "15" };
            String cfu = (String) JOptionPane.showInputDialog(this, "CFU per Idoneità:", "Registra",
                    JOptionPane.QUESTION_MESSAGE, null, opzioni, "6");
            if (cfu != null) {
                GestoreDatabase.setVotiEsami("IDONEO", nome, Integer.parseInt(cfu));
                GestoreDatabase.aggiornaStatoEsame(nome, true);
            }
        } else {
            String voto = JOptionPane.showInputDialog(this, "Inserisci il voto (18-30L):");
            if (voto != null && !voto.isEmpty()) {
                String cfu = JOptionPane.showInputDialog(this, "Inserisci i CFU:");
                if (cfu != null && !cfu.isEmpty()) {
                    GestoreDatabase.setVotiEsami(voto.toUpperCase(), nome, Integer.parseInt(cfu));
                    GestoreDatabase.aggiornaStatoEsame(nome, true);
                }
            }
        }
        aggiornaTutto();
        pv.refresh();
    }

    // Metodo di supporto per applicare lo sbarramento
    private void autoAggiornaSbarramento(JLabel label, JCheckBox check, Font originale) {
        Map<TextAttribute, Object> attributes = new HashMap<>(originale.getAttributes());
        if (check.isSelected()) {
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        } else {
            attributes.put(TextAttribute.STRIKETHROUGH, false);
        }
        Font fontConSbarramento = originale.deriveFont(attributes)
                .deriveFont(Math.max(11f, originale.getSize2D() * currentScale));
        label.putClientProperty("baseFont", originale.deriveFont(attributes));
        label.setFont(fontConSbarramento);
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

    private int getScaledExamHorizontalPadding() {
        return Math.max(6, Math.round(BASE_EXAM_HORIZONTAL_PADDING * currentScale));
    }

    private int getScaledExamVerticalPadding() {
        return Math.max(6, Math.round(BASE_EXAM_VERTICAL_PADDING * currentScale));
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

        JButton btnSalva = new JButton("Aggiungi Esame");
        btnSalva.putClientProperty("JButton.buttonType", "roundRect");
        btnSalva.setBackground(new Color(33, 150, 243));
        btnSalva.setForeground(Color.WHITE);
        btnSalva.setFont(new Font("Arial", Font.BOLD, 14));
        btnSalva.setPreferredSize(new Dimension(150, 36));
        btnSalva.setCursor(new Cursor(Cursor.HAND_CURSOR));

        rigaInput.add(campoNome);
        rigaInput.add(checkIdoneita);
        rigaInput.add(btnSalva);
        aggiungiEsame.add(rigaInput, BorderLayout.CENTER);

        // Azione Salva
        btnSalva.addActionListener(e -> {
            String nome = campoNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci un nome!");
                return;
            }
            if (index < 40) {
                GestoreDatabase.salvaEsame(nome, checkIdoneita.isSelected());
                checkIdoneita.setSelected(false);
                campoNome.setText("");
                aggiornaTutto();
                pv.refresh();
            }
        });
    }

    private void initAddedExamsLayout(JPanel esamiAggiunti) {
        esamiAggiunti.setLayout(new BorderLayout());
        esamiAggiunti.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        esamiPanel = new JPanel();
        esamiPanel.setLayout(new BoxLayout(esamiPanel, BoxLayout.Y_AXIS));
        esamiPanel.setBorder(BorderFactory.createEmptyBorder(0, getScaledExamSideMargin(), 0, getScaledExamSideMargin()));
        JScrollPane scrollPane = new JScrollPane(esamiPanel);
        javax.swing.border.Border bordoSoloSopra = BorderFactory.createMatteBorder(2, 0, 0, 0, Color.GRAY);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                bordoSoloSopra,
                "Aggiunti di recente",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16)));
        scrollPane.setBackground(GestoreDatabase.isTemaScuro() ? new Color(34,37,43) : Color.WHITE);
        scrollPane.getViewport().setBackground(GestoreDatabase.isTemaScuro() ? new Color(34,37,43) : Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        esamiAggiunti.add(scrollPane, BorderLayout.CENTER);
    }
}
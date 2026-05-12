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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private int cfuSalvati;
    private String votoSalvato;
    private float currentScale = 1.0f;
    private final PannelloVoti pv;
    private JPanel esamiPanel;

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
        JPanel pSinistra = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        pSinistra.setOpaque(false);

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
            if (JOptionPane.showConfirmDialog(this, "Eliminare definitivamente " + nome + "?", "Conferma",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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
            String[] opzioni = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
                    "17", "18" };
            String cfu = (String) JOptionPane.showInputDialog(this, "Modifica CFU per Idoneità (" + nome + "):",
                    "Registra", JOptionPane.QUESTION_MESSAGE, null, opzioni, "6");
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
                String voto = JOptionPane.showInputDialog(this,
                        "Inserisci/Modifica il voto per " + nome + " (18-30L):");

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
                            JOptionPane.showMessageDialog(this, "Errore: Il voto deve essere compreso tra 18 e 30.",
                                    "Voto non valido", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Errore: Inserisci un numero tra 18 e 30, oppure '30L'.",
                                "Formato non valido", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            // Se il voto è valido, chiediamo i CFU (Max 18)
            String[] opzioni = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
                    "17", "18" };
            String cfu = (String) JOptionPane.showInputDialog(this, "Seleziona i CFU per " + nome + " (Max 18):",
                    "Selezione CFU", JOptionPane.QUESTION_MESSAGE, null, opzioni, "6");

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
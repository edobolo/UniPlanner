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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.minec.GestoreNotifiche;
import com.minec.dati.GestoreDatabase;

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
        // 2. Aggiorna il JComboBox
        tendina.removeAllItems();
        for (String s : esami) {
            if (s != null) {
            String[] parti = s.split(";");
            String nome = parti[0];
            tendina.addItem(nome);
            }
        }
        tendina.setSelectedIndex(-1); // Deseleziona tutto

        // 3. Forza il ridisegno della UI
        esamiPanel.revalidate();
        esamiPanel.repaint();
        GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
    }

    private void disegnaEsameSuSchermo(String raw) {
        // la stringa 'raw' viene da esami.txt (es. "Analisi 1;true")
        String[] parti = raw.split(";");
        String nome = parti[0];
        boolean isCompletato = estraiCompletato(parti);
        boolean isIdoneita = estraiIdoneita(parti);

        // --- Cerca i CFU nel file voti.txt ---
        int cfuSalvati = 0;
        String[] votiRaw = GestoreDatabase.getVotiEsamiRaw();
        for (String rigaVoto : votiRaw) {
            String[] pVoto = rigaVoto.split(";");
            // Se la riga corrisponde al nostro esame e ha 3 elementi (Voto;Nome;CFU)
            if (pVoto.length >= 2 && pVoto[1].equals(nome)) {
                if (pVoto.length > 2) {
                    try {
                        cfuSalvati = Integer.parseInt(pVoto[2]);
                    } catch (NumberFormatException e) {
                    }
                }
                break;
            }
        }

        JPanel panelSingoloEsame = new JPanel(new BorderLayout());
        Dimension dimensioneCard = getScaledExamCardDimension();
        panelSingoloEsame.setPreferredSize(dimensioneCard);
        panelSingoloEsame.setMaximumSize(dimensioneCard);
        panelSingoloEsame.setMinimumSize(dimensioneCard);
        panelSingoloEsame.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSingoloEsame.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JLabel nomeEsameLabel = new JLabel(nome);
        JCheckBox markAsDone = new JCheckBox();
        markAsDone.setSelected(isCompletato);

        Font fontOriginale = new Font("Arial", Font.PLAIN, 18);
        nomeEsameLabel.setFont(fontOriginale);
        autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);

        // --- GESTIONE GRAFICA DEI CFU ---
        JButton buttonCFU = new JButton("Agg. CFU");
        JPanel pannelloCfuLocale = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        if (cfuSalvati > 0) {
            // Se ho già i CFU, mostro il testo e nascondo il bottone
            JLabel textCfu = new JLabel(cfuSalvati + " CFU");
            textCfu.setFont(new Font("Arial", Font.BOLD, 14));
            pannelloCfuLocale.add(textCfu);
            buttonCFU.setVisible(false);
        } else {
            // Il bottone CFU appare solo quando l'esame e' marcato come completato.
            buttonCFU.setVisible(isCompletato);
        }

        // Azione Bottone CFU
        buttonCFU.addActionListener(e -> {
            String[] opzioni = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" };
            String scelta = (String) JOptionPane.showInputDialog(null,
                    "Seleziona il numero di CFU per " + nome + ":",
                    "Selezione CFU", JOptionPane.QUESTION_MESSAGE, null, opzioni, opzioni[0]);
            if (scelta != null) {
                int cfu = Integer.parseInt(scelta);
                GestoreDatabase.addCfuEsame(nome, cfu); // Scrive nel file

                // Aggiorna l'interfaccia
                JLabel textCfu = new JLabel(cfu + " CFU");
                textCfu.setFont(new Font("Arial", Font.BOLD, 14));
                pannelloCfuLocale.add(textCfu);
                buttonCFU.setVisible(false); // Nasconde il bottone

                if (isIdoneita) {
                    GestoreDatabase.aggiornaStatoEsame(nome, true);
                    autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);
                }

                pannelloCfuLocale.revalidate();
                pannelloCfuLocale.repaint();
                pv.refresh();
                GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
            }
        });

        // Azione Checkbox Voti
        if (!isIdoneita) {
            markAsDone.addActionListener(e -> {
                if (markAsDone.isSelected()) {
                    String input = JOptionPane.showInputDialog(this, "Inserire il voto per " + nome);
                    if (input != null && !input.trim().isEmpty()) {
                        String votoPulito = input.trim().toUpperCase(); // Rimuove spazi e fa tutto maiuscolo (30L)
                        boolean valido = false;
                        String votoDaSalvare = "";
                        // 1. Controlliamo se è un 30L
                        if (votoPulito.equals("30L") || votoPulito.equals("30 E LODE")) {
                            valido = true;
                            votoDaSalvare = "30L"; // Standardizziamo il formato per il salvataggio
                        } else {
                            // 2. Altrimenti, controlliamo se è un numero valido
                            try {
                                int votoNumerico = Integer.parseInt(votoPulito);
                                if (votoNumerico >= 18 && votoNumerico <= 30) {
                                    valido = true;
                                    votoDaSalvare = String.valueOf(votoNumerico);
                                }
                            } catch (NumberFormatException ex) {
                                // Non è né "30L" né un numero
                            }
                        }
                        if (valido) {
                            // Se ha superato i test, salviamo usando il GestoreDatabase
                            GestoreDatabase.setVotiEsami(votoDaSalvare, nome, 0);
                            GestoreDatabase.aggiornaStatoEsame(nome, true);
                            autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);
                            buttonCFU.setVisible(true);
                            pv.refresh();
                            GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Voto non valido! Inserisci un numero tra 18 e 30, oppure '30L'.");
                            markAsDone.setSelected(false);
                        }
                    } else {
                        markAsDone.setSelected(false); // Utente ha annullato l'inserimento
                    }
                } else {
                    // SE TOLGO LA SPUNTA: Rimuovo tutto
                    GestoreDatabase.aggiornaStatoEsame(nome, false);
                    GestoreDatabase.removeVotiEsame(nome);
                    autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);
                    pannelloCfuLocale.removeAll();
                    buttonCFU.setVisible(false);
                    pannelloCfuLocale.revalidate();
                    pannelloCfuLocale.repaint();
                    pv.refresh();
                    GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
                }
            });
        } else {
            markAsDone.addActionListener(e -> {
                if (markAsDone.isSelected()) {
                    GestoreDatabase.aggiornaStatoEsame(nome, true);
                    autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);
                    if (pannelloCfuLocale.getComponentCount() == 0) {
                        buttonCFU.setVisible(true);
                    }
                } else {
                    GestoreDatabase.aggiornaStatoEsame(nome, false);
                    GestoreDatabase.removeVotiEsame(nome);
                    autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);
                    pannelloCfuLocale.removeAll();
                    buttonCFU.setVisible(false);
                    pannelloCfuLocale.revalidate();
                    pannelloCfuLocale.repaint();
                }
                pv.refresh();
                GestoreNotifiche.aggiornaTrofeiEAvvisa(this);
            });
        }
        // Assemblaggio finale delle "scatole"
        int horizontalPadding = getScaledExamHorizontalPadding();
        int verticalPadding = getScaledExamVerticalPadding();

        JPanel pannelloSinistra = new JPanel(new FlowLayout(FlowLayout.LEFT, horizontalPadding, verticalPadding));
        pannelloSinistra.add(markAsDone);
        pannelloSinistra.add(nomeEsameLabel);
        JPanel pannelloDestra = new JPanel(new FlowLayout(FlowLayout.RIGHT, horizontalPadding, verticalPadding));
        pannelloDestra.add(pannelloCfuLocale);
        pannelloDestra.add(buttonCFU);
        panelSingoloEsame.add(pannelloSinistra, BorderLayout.WEST);
        panelSingoloEsame.add(pannelloDestra, BorderLayout.EAST);
        esamiPanel.add(panelSingoloEsame);
        esamiPanel.add(Box.createRigidArea(new Dimension(0, getScaledExamGap())));
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

        // --- Pannello titolo ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        JLabel title = new JLabel("Aggiungi gli esami del tuo corso");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        topPanel.add(title);
        aggiungiEsame.add(topPanel, BorderLayout.NORTH);

        // ---Pannello aggiunta esami---
        JPanel midPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        midPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel text1 = new JLabel("Nome Esame:");
        text1.setFont(new Font("Arial", Font.BOLD, 20));
        JTextField campoNome = new JTextField();
        campoNome.setColumns(18);
        campoNome.setFont(new Font("Arial", Font.PLAIN, 15));
        campoNome.setHorizontalAlignment(JTextField.CENTER);
        JLabel textIdoneita = new JLabel("Idoneità:");
        textIdoneita.setFont(new Font("Arial", Font.BOLD, 13));
        JCheckBox checkIdoneita = new JCheckBox();
        checkIdoneita.setSelected(false);
        JButton btnSalva = new JButton("Salva Esame");
        midPanel.add(text1);
        midPanel.add(campoNome);
        midPanel.add(textIdoneita);
        midPanel.add(checkIdoneita);
        midPanel.add(btnSalva);
        aggiungiEsame.add(midPanel, BorderLayout.CENTER);

        // ---Pannello rimozione
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel textRem = new JLabel("Rimuovi esame:");
        textRem.setFont(new Font("Arial", Font.BOLD, 20));
        tendina = new JComboBox<>();
        JButton btnRemove = new JButton("Rimuovi");
        bottomPanel.add(textRem);
        bottomPanel.add(tendina);
        bottomPanel.add(btnRemove);
        aggiungiEsame.add(bottomPanel, BorderLayout.SOUTH);

        // LOGICA PULSANTI
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
                aggiornaTutto(); // Ricarica tutto dal file
                pv.refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Limite raggiunto!");
            }
        });

        btnRemove.addActionListener(e -> {
            String selezionato = (String) tendina.getSelectedItem();
            if (selezionato != null) {
                GestoreDatabase.removeNomeEsame(selezionato);
                GestoreDatabase.removeVotiEsame(selezionato);
                aggiornaTutto(); // Ricarica tutto dal file
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
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        esamiAggiunti.add(scrollPane, BorderLayout.CENTER);
    }
}
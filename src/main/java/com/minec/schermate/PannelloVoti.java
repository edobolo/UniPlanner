package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.minec.EsportatorePDF;
import com.minec.GestoreNotifiche;
import com.minec.dati.GestoreDatabase;

// Questa riga significa: "SchermataVoti è un tipo personalizzato di JPanel"
public class PannelloVoti extends JPanel {

    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 1.8f;

    private ArrayList<JPanel> examsList = new ArrayList<>();
    private JPanel mediaPanel = new JPanel();
    private JPanel examLeftPanel = new JPanel();
    private JPanel votiEsamiPanel = new JPanel();
    private JPanel panelInfo = new JPanel();
    private JPanel panelGraph = new JPanel();
    private JPanel optionButtonPanel;
    private float currentScale = 1.0f;

    public PannelloVoti() {
        this.setLayout(null);
        setPanelMedia(mediaPanel);
        setExamLeft(examLeftPanel);
        setVotiEsami(votiEsamiPanel);
        setPanelInfo(panelInfo);
        setGraphPanel(panelGraph);
        setOptionButton();

        this.add(mediaPanel);
        this.add(examLeftPanel);
        this.add(votiEsamiPanel);
        this.add(panelGraph);
        this.add(panelInfo);

        setupResponsiveLayout();
        SwingUtilities.invokeLater(this::applyResponsiveLayout);
    }

    public void setPanelMedia(JPanel mediaPanel) {
        mediaPanel.setLayout(new BorderLayout());

        // --- STILE CARD MODERNA ---
        boolean temaScuro = GestoreDatabase.isTemaScuro();
        Color cardBg = temaScuro ? new Color(48, 50, 54) : Color.WHITE;
        mediaPanel.setBackground(cardBg);
        mediaPanel.setBorder(
                BorderFactory.createLineBorder(temaScuro ? new Color(80, 80, 80) : new Color(220, 220, 220), 1, true));

        // --- LOGICA CALCOLO MEDIA (Ripristinata) ---
        String[] voti = GestoreDatabase.getVotiEsamiRaw();
        int sommaVoti = 0;
        int sommaVotiSemplice = 0;
        int sommaCfu = 0;
        int esamiValidi = 0;
        int pesoLode = GestoreDatabase.getPesoLode();

        for (int i = 0; i < voti.length; i++) {
            String[] pair = voti[i].split(";");
            if (pair.length >= 3) {
                try {
                    int cfuSingolo = Integer.parseInt(pair[2]);
                    int votoSingolo = 0;

                    if (pair[0].equalsIgnoreCase("30L") || pair[0].equalsIgnoreCase("30 e lode")) {
                        votoSingolo = pesoLode;
                    } else {
                        votoSingolo = Integer.parseInt(pair[0]);
                    }

                    sommaVoti += votoSingolo * cfuSingolo;
                    sommaVotiSemplice += votoSingolo;
                    sommaCfu += cfuSingolo;
                    esamiValidi++;
                } catch (NumberFormatException e) {
                }
            }
        }

        double mediaVotiP = 0;
        if (sommaCfu != 0) {
            mediaVotiP = Math.round(((double) sommaVoti / sommaCfu) * 10.0) / 10.0;
        }
        double mediaVotiA = 0;
        if (esamiValidi != 0) {
            mediaVotiA = Math.round(((double) sommaVotiSemplice / esamiValidi) * 10.0) / 10.0;
        }

        final String textP = "" + mediaVotiP;
        final String textA = "" + mediaVotiA;

        // --- ASSEMBLAGGIO GRAFICO ---
        JLabel title = new JLabel("Media Ponderata");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(temaScuro ? new Color(200, 200, 200) : Color.GRAY);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        title.setHorizontalAlignment(JLabel.CENTER);

        JPanel mediaF = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 45));
        mediaF.setOpaque(false);

        JLabel mediaLabel = new JLabel(textP);
        mediaLabel.setFont(new Font("Arial", Font.BOLD, 52));
        mediaLabel.setForeground(new Color(33, 150, 243));

        JLabel outOfLabel = new JLabel("/30");
        outOfLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        outOfLabel.setForeground(Color.LIGHT_GRAY);

        mediaF.add(mediaLabel);
        mediaF.add(outOfLabel);

        mediaPanel.add(title, BorderLayout.NORTH);
        mediaPanel.add(mediaF, BorderLayout.CENTER);

        mediaPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (title.getText().equals("Media Ponderata")) {
                    title.setText("Media Aritmetica");
                    mediaLabel.setText(textA);
                } else {
                    title.setText("Media Ponderata");
                    mediaLabel.setText(textP);
                }
            }
        });
        mediaPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    public void setExamLeft(JPanel examLeftPanel) {
        examLeftPanel.setLayout(new BorderLayout());

        boolean temaScuro = GestoreDatabase.isTemaScuro();
        examLeftPanel.setBackground(temaScuro ? new Color(48, 50, 54) : Color.WHITE);
        examLeftPanel.setOpaque(true);
        examLeftPanel.setBorder(
                BorderFactory.createLineBorder(temaScuro ? new Color(70, 70, 70) : new Color(230, 230, 230), 1, true));

        JLabel title = new JLabel("Progresso", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.PLAIN, 12));
        title.setForeground(Color.GRAY);
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        examLeftPanel.add(title, BorderLayout.NORTH);

        JPanel palliniPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        palliniPanel.setOpaque(false);

        examsList.clear();
        int examAdded = GestoreDatabase.numeroEsami();
        int numVoti = GestoreDatabase.numeroVoti();
        for (int i = 0; i < examAdded; i++) {
            boolean esameCompletato = i < numVoti;
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color fillColor = esameCompletato ? new Color(46, 204, 113) : new Color(230, 233, 238);
                    Color borderColor = esameCompletato ? new Color(34, 139, 34) : new Color(150, 150, 150);
                    g2.setColor(fillColor);
                    g2.fillOval(1, 1, getWidth() - 3, getHeight() - 3);
                    g2.setColor(borderColor);
                    g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                    g2.dispose();
                }
            };
            panel.setPreferredSize(new Dimension(14, 14));
            panel.setMinimumSize(new Dimension(14, 14));
            panel.setMaximumSize(new Dimension(14, 14));
            panel.setOpaque(false);
            panel.setToolTipText(esameCompletato ? "Esame completato" : "Esame rimanente");
            palliniPanel.add(panel);
            examsList.add(panel);
        }
        examLeftPanel.add(palliniPanel, BorderLayout.CENTER);
    }

    public void setPanelInfo(JPanel panelInfo) {
        boolean temaScuro = GestoreDatabase.isTemaScuro();
        panelInfo.setLayout(new GridLayout(2, 2, 10, 10));
        panelInfo.setOpaque(false);

        // --- LOGICA CALCOLI ---
        String[] voti = GestoreDatabase.getVotiEsamiRaw();
        int sommaVoti = 0;
        int sommaCfu = 0;
        int numeroLodi = 0;
        int pesoLode = GestoreDatabase.getPesoLode();
        int bonusLode = GestoreDatabase.getBonusLode();
        for (int i = 0; i < voti.length; i++) {
            String[] pair = voti[i].split(";");
            if (pair.length >= 3) {
                try {
                    int cfuSingolo = Integer.parseInt(pair[2]);
                    int votoSingolo;
                    if (pair[0].equalsIgnoreCase("30L") || pair[0].equalsIgnoreCase("30 e lode")) {
                        votoSingolo = pesoLode;
                        numeroLodi++;
                    } else {
                        votoSingolo = Integer.parseInt(pair[0]);
                    }
                    sommaVoti += votoSingolo * cfuSingolo;
                    sommaCfu += cfuSingolo;
                } catch (NumberFormatException e) {
                }
            }
        }

        double mediaVoti = 0;
        double baseL = 0;
        if (sommaCfu != 0) {
            mediaVoti = (double) sommaVoti / sommaCfu;
            baseL = (mediaVoti * 110) / 30;
            baseL += (numeroLodi * bonusLode);
        }

        int obiettivoSalvato = GestoreDatabase.getObiettivoMedia();
        int maxCfu = GestoreDatabase.getObiettivoCFU();
        if (maxCfu <= 0) {
            maxCfu = 1;
        }

        // --- 1 & 2. CREAZIONE CARD NORMALI ---
        autoCreazioneInfo(panelInfo, "Esami Passati",
                GestoreDatabase.numeroVoti() + "/" + GestoreDatabase.numeroEsami(), temaScuro);
        autoCreazioneInfo(panelInfo, "Base Laurea", Math.round(baseL) + "/110", temaScuro);

        // --- 3. PANNELLO OBIETTIVO (con logica click e stacco dal fondo) ---
        JPanel pnlObiettivo = autoCreazioneInfo(panelInfo, "Obiettivo", obiettivoSalvato + "/30", temaScuro);
        double differenza = mediaVoti - obiettivoSalvato;
        differenza = Math.round(differenza * 10.0) / 10.0;
        JLabel diff = new JLabel((differenza > 0 ? "+" : "") + differenza, SwingConstants.CENTER);
        diff.setFont(new Font("Arial", Font.BOLD, 13));
        diff.setForeground(differenza >= 0 ? new Color(0, 150, 0) : Color.RED);
        pnlObiettivo.add(diff, BorderLayout.SOUTH);

        pnlObiettivo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlObiettivo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String newOb = DialoghiModerni.chiediInput(
                        PannelloVoti.this,
                        "Obiettivo Media",
                        "Inserisci il tuo nuovo obiettivo di media (18-30):",
                        "Salva", ""
                );
                if (newOb != null && !newOb.trim().isEmpty()) {
                    try {
                        int nuovoObiettivo = Integer.parseInt(newOb);
                        if (nuovoObiettivo < 18 || nuovoObiettivo > 30)
                            throw new NumberFormatException();
                        GestoreDatabase.salvaObiettivoMedia(nuovoObiettivo);
                        refresh();
                    } catch (NumberFormatException e1) {
                        DialoghiModerni.mostraMessaggio(PannelloVoti.this, "Attenzione!", "Inserisci un voto valido tra 18 e 30", true);
                    }
                }
            }
        });

        // --- 4. PANNELLO CREDITI (con barra sistemata) ---
        JPanel pnlCrediti = new JPanel(new BorderLayout());
        pnlCrediti.setBackground(temaScuro ? new Color(48, 50, 54) : Color.WHITE);

        // Aggiungiamo padding interno anche qui!
        javax.swing.border.Border linea = BorderFactory
                .createLineBorder(temaScuro ? new Color(70, 70, 70) : new Color(230, 230, 230), 1, true);
        javax.swing.border.Border margine = BorderFactory.createEmptyBorder(6, 6, 6, 6);
        pnlCrediti.setBorder(BorderFactory.createCompoundBorder(linea, margine));

        JLabel t = new JLabel("Crediti", SwingConstants.CENTER);
        t.setFont(new Font("Arial", Font.PLAIN, 12));
        t.setForeground(Color.GRAY);

        // Ora il font è 18, identico alle altre caselle
        JLabel lblCfu = new JLabel(sommaCfu + "/" + maxCfu, SwingConstants.CENTER);
        lblCfu.setFont(new Font("Arial", Font.BOLD, 18));

        JProgressBar jp = new JProgressBar(0, maxCfu);
        jp.setValue(Math.min(sommaCfu, maxCfu));
        jp.setPreferredSize(new Dimension(100, 6)); // Leggermente più fine
        jp.setForeground(new Color(76, 175, 80));
        jp.setBorderPainted(false);

        // Contenitore per la barra per staccarla dai lati e dal fondo
        JPanel contenitoreBarra = new JPanel(new BorderLayout());
        contenitoreBarra.setOpaque(false);
        contenitoreBarra.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5)); // top, left, bottom, right
        contenitoreBarra.add(jp, BorderLayout.CENTER);

        pnlCrediti.add(t, BorderLayout.NORTH);
        pnlCrediti.add(lblCfu, BorderLayout.CENTER);
        pnlCrediti.add(contenitoreBarra, BorderLayout.SOUTH);

        panelInfo.add(pnlCrediti);
    }

    // Metodo helper
    private JPanel autoCreazioneInfo(JPanel parent, String titolo, String valore, boolean dark) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(dark ? new Color(48, 50, 54) : Color.WHITE);

        javax.swing.border.Border linea = BorderFactory
                .createLineBorder(dark ? new Color(70, 70, 70) : new Color(230, 230, 230), 1, true);
        javax.swing.border.Border margine = BorderFactory.createEmptyBorder(6, 5, 6, 5);
        p.setBorder(BorderFactory.createCompoundBorder(linea, margine));

        JLabel t = new JLabel(titolo, SwingConstants.CENTER);
        t.setFont(new Font("Arial", Font.PLAIN, 12));
        t.setForeground(Color.GRAY);

        JLabel v = new JLabel(valore, SwingConstants.CENTER);
        v.setFont(new Font("Arial", Font.BOLD, 18));

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        parent.add(p);

        return p;
    }

    public void setVotiEsami(JPanel votiEsamePanel) {
        votiEsamePanel.setLayout(new BorderLayout());
        boolean temaScuro = GestoreDatabase.isTemaScuro();

        // --- HEADER MODERNO ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        JLabel lblT = new JLabel("Voti Salvati");
        lblT.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(lblT, BorderLayout.WEST);
        votiEsamePanel.add(header, BorderLayout.NORTH);

        String[] votiRaw = GestoreDatabase.getVotiEsamiRaw();
        int numVoti = GestoreDatabase.numeroVoti();

        JPanel votiOnly = new JPanel();
        votiOnly.setLayout(new BoxLayout(votiOnly, BoxLayout.Y_AXIS));
        votiOnly.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(votiOnly);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        votiEsamePanel.add(scrollPane, BorderLayout.CENTER);

        for (int i = 0; i < numVoti; i++) {
            String rigaVoto = votiRaw[i];
            String[] parti = rigaVoto.split(";");
            String voto = parti[0];
            String nomeEsame = parti[1];

            // Logica tempo di studio
            int minutiTotali = GestoreDatabase.getMinutiStudioEsame(nomeEsame);
            String tempoFormattato = "";
            if (minutiTotali > 0) {
                int ore = minutiTotali / 60;
                int minRestanti = minutiTotali % 60;
                tempoFormattato = (ore > 0) ? ore + "h " + minRestanti + "m" : minRestanti + "m";
            }

            // --- CREAZIONE CARD RIGA ---
            JPanel panel = new JPanel(new BorderLayout());
            // Altezza leggermente aumentata per dare più respiro
            int rowHeight = Math.max(34, Math.round(34 * currentScale));
            panel.setMaximumSize(new Dimension(2000, rowHeight));
            panel.setPreferredSize(new Dimension(280, rowHeight));

            // SFONDO BIANCO/DARK
            panel.setBackground(temaScuro ? new Color(48, 50, 54) : Color.WHITE);

            // BORDO FINO CON PADDING INTERNO
            Border linea = BorderFactory.createLineBorder(temaScuro ? new Color(80, 80, 80) : Color.LIGHT_GRAY, 1);
            Border padding = BorderFactory.createEmptyBorder(0, 12, 0, 12);
            panel.setBorder(BorderFactory.createCompoundBorder(linea, padding));

            JLabel etichettaVoto = new JLabel(nomeEsame + ": " + voto);
            etichettaVoto.setFont(new Font("Arial", Font.PLAIN, 15));

            boolean haTempo = !tempoFormattato.isEmpty();
            String testoVisualizzato = haTempo ? tempoFormattato : "0h 0m";

            JLabel etichettaTempo = new JLabel(testoVisualizzato);
            etichettaTempo.setIcon(new FlatSVGIcon("icone/clock.svg", 16, 16));
            etichettaTempo.setIconTextGap(8);
        
            if (!haTempo) {
                etichettaTempo.setForeground(Color.LIGHT_GRAY);
            } else {
                etichettaTempo.setForeground(Color.GRAY);
            }
            etichettaTempo.setFont(new Font("Arial", Font.ITALIC, 12));
            etichettaTempo.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Listener per il tempo (Invariato)
            etichettaTempo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String tempoRaw = DialoghiModerni.chiediInput(
                            PannelloVoti.this,
                            "Tempo di Studio",
                            "Quanto hai studiato per " + nomeEsame + "? (es. '120' per minuti, oppure '2:30')",
                            "Salva",
                            "" // Lasciamo vuoto all'inizio
                    );

                    // 2. Controlliamo che l'utente non abbia annullato
                    if (tempoRaw != null && !tempoRaw.trim().isEmpty()) {
                        tempoRaw = tempoRaw.trim();
                        int mTot = -1;

                        try {
                            if (tempoRaw.contains(":")) {
                                String[] p = tempoRaw.split(":");
                                if (p.length == 2) {
                                    int ore = Integer.parseInt(p[0]);
                                    int minuti = Integer.parseInt(p[1]);
                                    mTot = (ore * 60) + minuti;
                                } else {
                                    throw new NumberFormatException(); // Lancia errore se scrive roba strana tipo
                                                                       // "2:30:15"
                                }
                            } else {
                                mTot = Integer.parseInt(tempoRaw);
                            }
                            // 4. Salvataggio e aggiornamento
                            if (mTot >= 0) {
                                GestoreDatabase.setNuovoTempoStudio(nomeEsame, mTot);
                                refresh();
                            } else {
                                throw new NumberFormatException(); // Niente numeri negativi
                            }
                        } catch (NumberFormatException ex) {
                            // 5. Gestione dell'errore moderna
                            DialoghiModerni.mostraMessaggio(
                                    PannelloVoti.this,
                                    "Formato non valido",
                                    "Inserisci solo i minuti (es. 120) o il formato ore:minuti (es. 2:30).",
                                    true);
                        }
                    }
                }
            });

            panel.add(etichettaVoto, BorderLayout.WEST);
            panel.add(etichettaTempo, BorderLayout.EAST);

            votiOnly.add(panel);
            votiOnly.add(Box.createRigidArea(new Dimension(0, 6))); // Spazio tra le card
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

    private void applyResponsiveLayout() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        float scaleX = (float) getWidth() / BASE_WIDTH;
        float scaleY = (float) getHeight() / BASE_HEIGHT;
        currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.min(scaleX, scaleY)));

        int leftX = Math.round(50 * currentScale);
        int leftW = Math.round(200 * currentScale);
        int columnGap = Math.round(50 * currentScale);
        int rightX = leftX + leftW + columnGap;
        int rightMargin = Math.round(30 * currentScale);
        int rightW = Math.max(260, getWidth() - rightX - rightMargin);

        mediaPanel.setBounds(scaleRect(50, 40, 200, 200));
        examLeftPanel.setBounds(scaleRect(50, 255, 200, 80));
        
        // --- CALCOLO DINAMICO DELLO SPAZIO VERTICALE ---
        int margineFondo = Math.round(20 * currentScale);
        int infoX = Math.round(50 * currentScale);
        int infoY = Math.round(350 * currentScale);
        int infoW = Math.round(200 * currentScale);
        int infoH = getHeight() - infoY - margineFondo;
        panelInfo.setBounds(infoX, infoY, infoW, Math.max(infoH, Math.round(200 * currentScale)));
        int graphH = Math.round(150 * currentScale);
        int graphY = getHeight() - graphH - margineFondo;
        int graphY_minimo = Math.round(330 * currentScale);
        graphY = Math.max(graphY, graphY_minimo);
        panelGraph.setBounds(rightX, graphY, rightW, graphH);
        int votiY = Math.round(35 * currentScale);
        int gapTraListaEGrafico = Math.round(25 * currentScale);
        int votiH = graphY - votiY - gapTraListaEGrafico;
        votiEsamiPanel.setBounds(rightX, votiY, rightW, Math.max(votiH, Math.round(270 * currentScale)));

        if (optionButtonPanel != null) {
            int buttonSize = Math.round(30 * currentScale);
            optionButtonPanel.setBounds(
                    getWidth() - buttonSize - Math.round(15 * currentScale),
                    Math.round(10 * currentScale),
                    buttonSize, // Larghezza
                    buttonSize // Altezza
            );
        }
        applyVotiRowsScaling();
        scaleFontsRecursively(this, currentScale);
        revalidate();
        repaint();
    }

    private void applyVotiRowsScaling() {
        JScrollPane scrollPane = null;
        for (Component child : votiEsamiPanel.getComponents()) {
            if (JScrollPane.class.isInstance(child)) {
                scrollPane = JScrollPane.class.cast(child);
                break;
            }
        }
        if (scrollPane == null) {
            return;
        }

        Component view = scrollPane.getViewport().getView();
        if (!JPanel.class.isInstance(view)) {
            return;
        }

        JPanel votiOnly = JPanel.class.cast(view);
        int rowWidth = Math.max(220, votiEsamiPanel.getWidth() - Math.round(70 * currentScale));
        int rowHeight = Math.max(30, Math.round(34 * currentScale));
        int rowGap = Math.max(4, Math.round(5 * currentScale));
        votiOnly.setBorder(BorderFactory.createEmptyBorder(0, Math.max(8, Math.round(10 * currentScale)), 0,
                Math.max(8, Math.round(10 * currentScale))));

        for (Component row : votiOnly.getComponents()) {
            if (JPanel.class.isInstance(row)) {
                JPanel panel = JPanel.class.cast(row);
                Dimension dim = new Dimension(rowWidth, rowHeight);
                panel.setPreferredSize(dim);
                panel.setMaximumSize(dim);
                panel.setMinimumSize(dim);
            } else if (Box.Filler.class.isInstance(row)) {
                Box.Filler spacer = Box.Filler.class.cast(row);
                Dimension gapDim = new Dimension(0, rowGap);
                spacer.changeShape(gapDim, gapDim, gapDim);
            }
        }
    }

    private void scaleFontsRecursively(Component component, float scale) {
        if (JComponent.class.isInstance(component)) {
            JComponent jc = JComponent.class.cast(component);
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

        if (Container.class.isInstance(component)) {
            Container container = Container.class.cast(component);
            for (Component child : container.getComponents()) {
                scaleFontsRecursively(child, scale);
            }
        }
    }

    private java.awt.Rectangle scaleRect(int x, int y, int w, int h) {
        return new java.awt.Rectangle(
                Math.round(x * currentScale),
                Math.round(y * currentScale),
                Math.round(w * currentScale),
                Math.round(h * currentScale));
    }

    public void setGraphPanel(JPanel panelGraph) {
        panelGraph.setLayout(new BorderLayout());
        panelGraph.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Resettiamo i bordi

        // --- HEADER MODERNO PER IL GRAFICO ---
        JPanel headerG = new JPanel(new BorderLayout());
        headerG.setOpaque(false);
        headerG.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        JLabel lblG = new JLabel("Andamento Voti");
        lblG.setFont(new Font("Arial", Font.BOLD, 18));
        headerG.add(lblG, BorderLayout.WEST);

        panelGraph.add(headerG, BorderLayout.NORTH);

        GraphVotiMaker gp = new GraphVotiMaker(350, 150);
        panelGraph.add(gp, BorderLayout.CENTER);
    }
    class GraphVotiMaker extends JPanel {
        private static final int MIN_VOTO = 18;
        private String[] etichette;
        private int[] voti;

        public GraphVotiMaker(int x, int y) {
            this.setPreferredSize(new Dimension(x, y));
            caricaVoti();
        }
        private void caricaVoti() {
            String[] votiRaw = GestoreDatabase.getVotiEsamiRaw();
            int numVoti = GestoreDatabase.numeroVoti();
            ArrayList<Integer> valori = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            for (int i = 0; i < numVoti && i < votiRaw.length; i++) {
                String[] parti = votiRaw[i].split(";");
                if (parti.length < 2) {
                    continue;
                }
                try {
                    int votoNumerico = parseVoto(parti[0]);
                    valori.add(votoNumerico);
                    labels.add(creaSigla(parti[1]));
                } catch (NumberFormatException ex) {
                }
            }
            voti = new int[valori.size()];
            for (int i = 0; i < valori.size(); i++) {
                voti[i] = valori.get(i);
            }
            etichette = labels.toArray(new String[0]);
        }
        private int parseVoto(String votoRaw) {
            if (votoRaw.equalsIgnoreCase("30L") || votoRaw.equalsIgnoreCase("30 e lode")) {
                return GestoreDatabase.getPesoLode();
            }
            return Integer.parseInt(votoRaw.trim());
        }
        private String creaSigla(String nomeEsame) {
            String[] paroleDivise = nomeEsame.trim().split("\\s+");
            StringBuilder sigla = new StringBuilder();
            for (String parola : paroleDivise) {
                if (!parola.isEmpty()) {
                    sigla.append(Character.toUpperCase(parola.charAt(0)));
                }
            }
            return sigla.length() == 0 ? "?" : sigla.toString();
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // Abilita l'antialiasing per linee e testo
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Definisci i colori in base al tema
            boolean isDarkMode = GestoreDatabase.isTemaScuro();
            Color colorAssi = isDarkMode ? new Color(150, 150, 150) : Color.GRAY;
            Color colorGridLine = isDarkMode ? new Color(80, 80, 80) : new Color(210, 210, 210);
            Color colorTesto = isDarkMode ? new Color(200, 200, 200) : Color.DARK_GRAY;
            Color colorLinea = new Color(36, 166, 6); // Verde sempre uguale
            
            final int left = 30;
            final int right = 10;
            final int top = 12;
            final int bottom = 22;
            int graphW = getWidth() - left - right;
            int graphH = getHeight() - top - bottom;
            if (graphW <= 0 || graphH <= 0) {
                g2.dispose();
                return;
            }
            // Disegna assi
            g2.setColor(colorAssi);
            g2.drawLine(left, top + graphH, left + graphW, top + graphH);
            g2.drawLine(left, top, left, top + graphH);

            if (voti.length == 0) {
                g2.setColor(colorTesto);
                g2.drawString("Nessun voto disponibile", left + 35, top + (graphH / 2));
                g2.dispose();
                return;
            }
            int maxVoto = Math.max(30, GestoreDatabase.getPesoLode());
            int rangeVoti = Math.max(1, maxVoto - MIN_VOTO);
            
            // Disegna griglia di livelli
            g2.setColor(colorGridLine);
            for (int livello = MIN_VOTO; livello <= 30; livello += 3) {
                int yLinea = top + ((maxVoto - livello) * graphH) / rangeVoti;
                g2.drawLine(left, yLinea, left + graphW, yLinea);
            }
            int prevX = -1;
            int prevY = -1;
            g2.setColor(colorLinea);

            // Disegna la linea del grafico e i punti
            for (int i = 0; i < voti.length; i++) {
                int x = voti.length == 1
                        ? left + (graphW / 2)
                        : left + (i * graphW) / (voti.length - 1);
                int y = top + ((maxVoto - voti[i]) * graphH) / rangeVoti;
                if (i > 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                g2.fillOval(x - 3, y - 3, 6, 6);
                if (i < etichette.length) {
                    g2.setColor(colorTesto);
                    g2.drawString(etichette[i], x - 8, top + graphH + 15);
                    g2.setColor(colorLinea);
                }
                prevX = x;
                prevY = y;
            }
            g2.dispose();
        }
    }

    private JPanel creaCardImpostazione(String titolo, String descrizione, JComponent controllo, String iconPath) {
        boolean temaScuro = GestoreDatabase.isTemaScuro();

        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(temaScuro ? new Color(55, 58, 63) : new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(temaScuro ? new Color(80, 80, 80) : new Color(225, 225, 225), 1, true),
                BorderFactory.createEmptyBorder(12, 15, 12, 15
        )));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- PARTE SINISTRA: Icona + Testi ---
        JPanel leftPanel = new JPanel(new BorderLayout(15, 0));
        leftPanel.setOpaque(false);

        if (iconPath != null && !iconPath.isEmpty()) {
            JLabel lblIcona = new JLabel(new FlatSVGIcon(iconPath, 24, 24));
            // Applichiamo un colore grigio chiaro all'icona se siamo in tema scuro per
            // farla risaltare
            lblIcona.setForeground(temaScuro ? new Color(200, 200, 200) : Color.DARK_GRAY);
            leftPanel.add(lblIcona, BorderLayout.WEST);
        }

        // Pannello per Titolo e Descrizione
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel lblTitolo = new JLabel(titolo);
        lblTitolo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitolo.setForeground(temaScuro ? new Color(230, 230, 230) : Color.DARK_GRAY);

        JLabel lblDesc = new JLabel(descrizione);
        lblDesc.setFont(new Font("Arial", Font.PLAIN, 11));
        lblDesc.setForeground(Color.GRAY);

        textPanel.add(lblTitolo);
        textPanel.add(lblDesc);
        leftPanel.add(textPanel, BorderLayout.CENTER);

        card.add(leftPanel, BorderLayout.CENTER);

        // --- PARTE DESTRA: Il controllo ---
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setOpaque(false);
        controlPanel.add(controllo);
        card.add(controlPanel, BorderLayout.EAST);

        // Manteniamo la flessibilità orizzontale
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        card.setMinimumSize(new Dimension(300, 75));

        return card;
    }
    
    private JLabel creaHeaderSezione(String testo) {
        JLabel lbl = new JLabel(testo);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(140, 140, 140));
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 5, 8, 0));
        lbl.setHorizontalAlignment(SwingConstants.LEFT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return lbl;
    }

    public void setOptionButton() {
        JButton optionBut = new JButton("");
        if (GestoreDatabase.isTemaScuro()) {
            optionBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24)); 
        }else {
            optionBut.setIcon(new FlatSVGIcon("icone/opzioni.svg", 24, 24));
        }
        optionBut.setBorderPainted(false);
        optionBut.setFocusPainted(false);
        optionBut.setContentAreaFilled(false);
        optionBut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        optionBut.setPreferredSize(new Dimension(30, 30));

        optionBut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (GestoreDatabase.isTemaScuro()) {
                    optionBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24)); 
                }else {
                    optionBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24));
                }
                refresh();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (GestoreDatabase.isTemaScuro()) {
                    optionBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24)); 
                }else {
                    optionBut.setIcon(new FlatSVGIcon("icone/opzioni.svg", 24, 24));
                }
                refresh();
            }
        });
        optionBut.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            JPanel shadowOverlay = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                }
            };
            shadowOverlay.setOpaque(false);
            shadowOverlay.setLayout(new GridBagLayout());
            shadowOverlay.addMouseListener(new java.awt.event.MouseAdapter() {
            });

            boolean scuro = GestoreDatabase.isTemaScuro();

            JPanel pannelloImpostazioni = new JPanel();
            pannelloImpostazioni.setPreferredSize(new Dimension(560, 600)); // Finestra leggermente più grande per far
                                                                            // respirare le card
            pannelloImpostazioni.setBorder(
                    BorderFactory.createLineBorder(scuro ? new Color(80, 80, 80) : new Color(200, 200, 200), 1, true));
            pannelloImpostazioni.setLayout(new BorderLayout());
            pannelloImpostazioni.setBackground(scuro ? new Color(48, 50, 54) : Color.WHITE);

            // --- HEADER ---
            JLabel titolo = new JLabel("Impostazioni", SwingConstants.CENTER);
            titolo.setFont(new Font("Arial", Font.BOLD, 22));
            titolo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            pannelloImpostazioni.add(titolo, BorderLayout.NORTH);

            // --- CONTENUTO (Scrollabile) ---
            JPanel centro = new JPanel();
            centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
            centro.setOpaque(false);
            centro.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

            // --- SEZIONE 1: ASPETTO E PREFERENZE ---
            centro.add(creaHeaderSezione("ASPETTO E PREFERENZE"));

            JCheckBox chkTema = new JCheckBox();
            chkTema.setSelected(scuro);
            chkTema.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chkTema.addActionListener(ev -> {
                boolean isScuro = chkTema.isSelected();
                GestoreDatabase.salvaTemaScuro(isScuro);
                try {
                    if (isScuro) {
                        javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                        optionBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24));
                    } else {
                        javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                        optionBut.setIcon(new FlatSVGIcon("icone/opzioni.svg", 24, 24));
                    }
                    SwingUtilities.updateComponentTreeUI(frame);
                    shadowOverlay.setVisible(false); // Chiudiamo per forzare l'aggiornamento visivo pulito
                    refresh();
                } catch (Exception ex) {
                }
            });
            boolean isDark = GestoreDatabase.isTemaScuro();
            String pathIcon = "";
            if(isDark)
                pathIcon = "icone/dark2.svg";
            else 
                pathIcon = "icone/dark1.svg";
            centro.add(creaCardImpostazione("Modalità Scura ", "Affatica meno la vista durante la sera", chkTema, pathIcon));
            centro.add(Box.createRigidArea(new Dimension(0, 8)));

            JButton btnOrdine = new JButton(GestoreDatabase.getOrdineScadenza() ? "Aggiunta" : "Cronologico");
            btnOrdine.putClientProperty("JButton.buttonType", "roundRect");
            btnOrdine.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOrdine.addActionListener(ez -> {
                boolean crono = btnOrdine.getText().equals("Cronologico");
                btnOrdine.setText(crono ? "Aggiunta" : "Cronologico");
                GestoreDatabase.salvaOrdineScadenze(crono);
            });
            centro.add(creaCardImpostazione("Ordine Appelli", "Scegli l'ordine nella pagina Scadenze", btnOrdine, "icone/calendar.svg"));

            // --- SEZIONE 2: LAUREA ---
            centro.add(creaHeaderSezione("LAUREA E OBIETTIVI"));

            JPanel pnlCfu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            pnlCfu.setOpaque(false);
            JTextField txtCfu = new JTextField(String.valueOf(GestoreDatabase.getObiettivoCFU()), 4);
            JButton btnSalvaCfu = new JButton("Salva");
            btnSalvaCfu.putClientProperty("JButton.buttonType", "roundRect");
            btnSalvaCfu.setBackground(new Color(33, 150, 243));
            btnSalvaCfu.setForeground(Color.WHITE);
            btnSalvaCfu.addActionListener(ev -> {
                try {
                    GestoreDatabase.salvaObiettivoCfu(Integer.parseInt(txtCfu.getText()));
                    DialoghiModerni.mostraMessaggio(pannelloImpostazioni, "Successo", "Cfu Aggiornati!", false);
                    refresh();
                } catch (Exception ex) {
                    DialoghiModerni.mostraMessaggio(pannelloImpostazioni, "Attenzione!", "Numero non valido", true);
                }
            });
            pnlCfu.add(txtCfu);
            pnlCfu.add(btnSalvaCfu);
            centro.add(creaCardImpostazione("Obiettivo CFU", "Crediti totali per completare gli studi", pnlCfu, "icone/target.svg"));
            centro.add(Box.createRigidArea(new Dimension(0, 8)));

            // Sostituiamo il menu a tendina complicato con una card pulita e diretta
            JPanel pnlParam = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            pnlParam.setOpaque(false);
            JTextField txtLode = new JTextField(GestoreDatabase.getImpostazione("LODE", "30"), 2);
            JTextField txtBonus = new JTextField(GestoreDatabase.getImpostazione("BONUS_LODE", "0"), 2);
            JButton btnSalvaParam = new JButton("Salva");
            btnSalvaParam.putClientProperty("JButton.buttonType", "roundRect");
            btnSalvaParam.setBackground(new Color(33, 150, 243));
            btnSalvaParam.setForeground(Color.WHITE);
            btnSalvaParam.addActionListener(ev -> {
                GestoreDatabase.salvaImpostazione("LODE", txtLode.getText());
                GestoreDatabase.salvaImpostazione("BONUS_LODE", txtBonus.getText());
                DialoghiModerni.mostraMessaggio(pannelloImpostazioni, "Successo","Parametri Laurea salvati!", false);
                refresh();
            });
            pnlParam.add(new JLabel("Lode:"));
            pnlParam.add(txtLode);
            pnlParam.add(Box.createRigidArea(new Dimension(5, 0)));
            pnlParam.add(new JLabel("Bonus:"));
            pnlParam.add(txtBonus);
            pnlParam.add(btnSalvaParam);
            centro.add(creaCardImpostazione("Parametri di Calcolo", "Valore lode e punti extra alla laurea", pnlParam, "icone/hat.svg"));

            // --- SEZIONE 3: DATI E BACKUP ---
            centro.add(creaHeaderSezione("DATI E BACKUP"));

            JPanel pnlBackup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            pnlBackup.setOpaque(false);
            JButton btnExport = new JButton("Esporta CSV");
            btnExport.putClientProperty("JButton.buttonType", "roundRect");
            // Verde chiaro se scuro, Verde Excel se chiaro
            btnExport.setForeground(scuro ? new Color(129, 199, 132) : new Color(33, 115, 70));
            btnExport.addActionListener(ev -> esportaLibrettoInExcel(pannelloImpostazioni));

            JButton btnImport = new JButton("Importa CSV");
            btnImport.putClientProperty("JButton.buttonType", "roundRect");
            // Azzurro chiaro se scuro, Blu classico se chiaro
            btnImport.setForeground(scuro ? new Color(100, 181, 246) : new Color(0, 102, 204));
            btnImport.addActionListener(ev -> {
                importaLibrettoDaExcel(pannelloImpostazioni);
                shadowOverlay.setVisible(false);
                refresh();
            });
            pnlBackup.add(btnImport);
            pnlBackup.add(btnExport);
            centro.add(creaCardImpostazione("Salvataggi Database", "Metti al sicuro i tuoi dati o ripristinali", pnlBackup, "icone/excel.svg"));
            centro.add(Box.createRigidArea(new Dimension(0, 8)));

            JButton btnPDF = new JButton("Crea PDF");
            btnPDF.putClientProperty("JButton.buttonType", "roundRect");
            // Rosso pastello se scuro, Rosso scuro se chiaro
            btnPDF.setForeground(scuro ? new Color(229, 115, 115) : new Color(140, 24, 26));
            btnPDF.addActionListener(ev -> { /* La tua logica PDF rimane identica a prima */
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Salva il tuo libretto");
                fileChooser.setSelectedFile(new java.io.File("Libretto_UniPlanner.pdf"));
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    try {
                        String path = fileChooser.getSelectedFile().getAbsolutePath();
                        if (!path.toLowerCase().endsWith(".pdf"))
                            path += ".pdf";
                        EsportatorePDF.generaLibretto(path);
                        DialoghiModerni.mostraMessaggio(null, "Successo", "PDF creato con successo!", false);
                    } catch (Exception ex) {
                        DialoghiModerni.mostraMessaggio(null, "Attenzione!", "Errore: " + ex.getMessage(), true);
                    }
                }
            });
            centro.add(creaCardImpostazione("Esporta Libretto", "Genera un file PDF del tuo libretto", btnPDF, "icone/pdf.svg"));

            // --- SEZIONE 4: PERICOLO ---
            centro.add(creaHeaderSezione("ZONA PERICOLOSA"));

            JButton btnReset = new JButton("Azzera Dati");
            btnReset.putClientProperty("JButton.buttonType", "roundRect");
            btnReset.setForeground(scuro ? new Color(255, 100, 100) : Color.RED);
            btnReset.addActionListener(ev -> {
                if (DialoghiModerni.chiediConferma(pannelloImpostazioni, 
                    "Conferma Reset", 
                    "Vuoi davvero svuotare il libretto?",
                    "Si, svuota", true)) {

                    GestoreDatabase.resetTutto();
                    DialoghiModerni.mostraMessaggio(pannelloImpostazioni, "Successo", 
                            "Dati azzerati. L'applicazione si chiuderà", false);
                    System.exit(0);
                }
            });
            JPanel cardReset = creaCardImpostazione("Reset Completo", "Attenzione: l'azione è irreversibile", btnReset, "icone/bin1.svg");
            cardReset.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 100, 100), 1, true),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)));
            centro.add(cardReset);

            // Applichiamo lo Scroll
            JScrollPane scroll = new JScrollPane(centro);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            pannelloImpostazioni.add(scroll, BorderLayout.CENTER);

            // --- FOOTER (Bottone Chiudi) ---
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footer.setOpaque(false);
            footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
            JButton btnChiudi = new JButton("Chiudi");
            btnChiudi.putClientProperty("JButton.buttonType", "roundRect");
            btnChiudi.putClientProperty("FlatLaf.style", "arc: 99");
            btnChiudi.setFont(new Font("Arial", Font.BOLD, 14));
            btnChiudi.setBackground(scuro ? new Color(70, 70, 70) : new Color(200, 200, 200));
            btnChiudi.setForeground(scuro ? Color.WHITE : Color.DARK_GRAY);
            btnChiudi.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 30));
            btnChiudi.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnChiudi.addActionListener(chiudiEvent -> shadowOverlay.setVisible(false));
            footer.add(btnChiudi);
            pannelloImpostazioni.add(footer, BorderLayout.SOUTH);

            // Mostriamo il pannello
            shadowOverlay.add(pannelloImpostazioni, new GridBagConstraints());
            frame.setGlassPane(shadowOverlay);
            shadowOverlay.setVisible(true);
        });

        Color coloreHover = new Color(48, 68, 88);
        Color coloreSfondo = mediaPanel.getBackground();
        optionBut.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                optionBut.setContentAreaFilled(true);
                optionBut.setBackground(coloreHover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                optionBut.setContentAreaFilled(false);
                optionBut.setBackground(coloreSfondo);
            }
        });

        optionButtonPanel = new JPanel();
        optionButtonPanel.setLayout(new BorderLayout());
        optionButtonPanel.add(optionBut, BorderLayout.CENTER);
        this.add(optionButtonPanel);
    }

    private void esportaLibrettoInExcel(JPanel parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Esporta Backup Completo (CSV)");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("File CSV", "csv"));
        int scelta = fileChooser.showSaveDialog(parentComponent);
        if (scelta == JFileChooser.APPROVE_OPTION) {
            java.io.File fileDaSalvare = fileChooser.getSelectedFile();
            String percorso = fileDaSalvare.getAbsolutePath();
            if (!percorso.toLowerCase().endsWith(".csv"))
                percorso += ".csv";
            try (java.io.FileWriter fw = new java.io.FileWriter(percorso)) {
                // --- SEZIONE 1: TUTTI GLI ESAMI ---
                fw.write("### ESAMI ###\n");
                fw.write("NOME ESAME;VOTO;CFU;COMPLETATO\n");
                String[] tuttiEsami = GestoreDatabase.getEsamiSalvatiRaw();
                String[] tuttiVoti = GestoreDatabase.getVotiEsamiRaw();
                int sommaVoti = 0;
                int sommaCfu = 0;
                for (String esameRaw : tuttiEsami) {
                    if (esameRaw == null) continue;
                    String[] parti = esameRaw.split(";");
                    String nome = parti[0];
                    boolean completato = Boolean.parseBoolean(parti[1]);
                    String votoDaScrivere = "";
                    String cfuDaScrivere = "0";
                    if (completato) {
                        for (String v : tuttiVoti) {
                            if (v == null) continue;
                            String[] pVoto = v.split(";");
                            if (pVoto.length >= 2 && pVoto[1].equals(nome)) {
                                votoDaScrivere = pVoto[0];
                                if (pVoto.length > 2) cfuDaScrivere = pVoto[2];
                                try { 
                                    int cfuNum = Integer.parseInt(cfuDaScrivere);
                                    int votoNum = (votoDaScrivere.equalsIgnoreCase("30L") || votoDaScrivere.equalsIgnoreCase("30 E LODE"))
                                                    ? GestoreDatabase.getPesoLode() : Integer.parseInt(votoDaScrivere);
                                    sommaVoti += votoNum * cfuNum;
                                    sommaCfu += cfuNum;
                                } catch (Exception e) {}
                                break;
                            }
                        }
                    }
                    fw.write(nome + ";" + votoDaScrivere + ";" + cfuDaScrivere + ";" + completato + "\n");
                }
                fw.write("\nTOTALE CFU;" + sommaCfu + ";\n");
                if (sommaCfu > 0) {
                    double media = Math.round(((double) sommaVoti / sommaCfu) * 100.0) / 100.0;
                    fw.write("MEDIA PONDERATA;" + media + ";\n");
                }

                // --- SEZIONE 2: IMPOSTAZIONI GLOBALI ---
                fw.write("\n### IMPOSTAZIONI ###\n");
                fw.write("OBIETTIVO_CFU;" + GestoreDatabase.getObiettivoCFU() + "\n");
                fw.write("OBIETTIVO_MEDIA;" + GestoreDatabase.getObiettivoMedia() + "\n");
                fw.write("TEMA_SCURO;" + GestoreDatabase.isTemaScuro() + "\n");
                fw.write("ORDINE_SCADENZE;" + GestoreDatabase.getOrdineScadenza() + "\n");
                fw.write("LODE;" + GestoreDatabase.getImpostazione("LODE", "30") + "\n");
                fw.write("BONUS_LODE;" + GestoreDatabase.getImpostazione("BONUS_LODE", "0") + "\n");
                // NUOVE IMPOSTAZIONI POMODORO Aggiunte qui:
                fw.write("POMODORI;" + GestoreDatabase.getPomodori() + "\n");
                fw.write("POMODORI_DATA;" + GestoreDatabase.getDataPomodori() + "\n");
                fw.write("POMODORI_MAX;" + GestoreDatabase.getMaxPomodoriGiornalieri() + "\n");
                fw.write("MINUTI_STUDIO;" + GestoreDatabase.getMinutiStudio() + "\n");
                fw.write("MINUTI_PAUSA;" + GestoreDatabase.getMinutiPausa() + "\n");

                // --- SEZIONE 3: SCADENZE ---
                fw.write("\n### SCADENZE ###\n");
                String[] scadenze = GestoreDatabase.getScadenzeRaw();
                if (scadenze != null) {
                    for (String sc : scadenze) {
                        if (sc != null && !sc.trim().isEmpty()) { fw.write(sc + "\n"); }
                    }
                }

                // --- SEZIONE 4: TEMPO DI STUDIO (NUOVA) ---
                fw.write("\n### STUDIO ###\n");
                String[] studio = GestoreDatabase.getTuttoLoStudioRaw();
                if (studio != null) {
                    for (String st : studio) {
                        if (st != null && !st.trim().isEmpty()) { fw.write(st + "\n"); }
                    }
                }
                DialoghiModerni.mostraMessaggio(parentComponent, "Successo", "Backup esportato con successo in:\n" + percorso, false);

            } catch (Exception ex) {
                DialoghiModerni.mostraMessaggio(parentComponent, "Attenzione!", "Errore durante l'esportazione", true);
            }
        }
    }
    private void importaLibrettoDaExcel(JPanel parentComponent) {
        if (GestoreDatabase.getEsamiSalvatiRaw().length != 0 || GestoreDatabase.getScadenzeRaw().length != 0 || 
            GestoreDatabase.getVotiEsamiRaw().length != 0) {
            DialoghiModerni.mostraMessaggio(this, "Attenzione!", 
                    "Assicurati di aver cancellato tutti i dati (Fai reset da impostazioni prima di importare)", true);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Importa Backup Completo (CSV)");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("File CSV", "csv"));
        int scelta = fileChooser.showOpenDialog(parentComponent);
        if (scelta == JFileChooser.APPROVE_OPTION) {
            java.io.File fileDaLeggere = fileChooser.getSelectedFile();
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(fileDaLeggere))) {
                String linea;
                String sezioneAttuale = "ESAMI";
                int esamiImportati = 0;
                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.isEmpty()) continue;
                    
                    // Gestione delle Sezioni (Aggiunto il blocco STUDIO)
                    if (linea.startsWith("### ESAMI ###")) { sezioneAttuale = "ESAMI"; continue; }
                    if (linea.startsWith("### IMPOSTAZIONI ###")) { sezioneAttuale = "IMPOSTAZIONI"; continue; }
                    if (linea.startsWith("### SCADENZE ###")) { sezioneAttuale = "SCADENZE"; continue; }
                    if (linea.startsWith("### STUDIO ###")) { sezioneAttuale = "STUDIO"; continue; }

                    if (sezioneAttuale.equals("ESAMI")) {
                        if (linea.startsWith("NOME ESAME") || linea.startsWith("TOTALE") || linea.startsWith("MEDIA")) continue;
                        
                        String[] parti = linea.split(";");
                        if (parti.length >= 1) { 
                            String nomeEsame = parti[0];
                            String voto = (parti.length > 1) ? parti[1].trim() : "";
                            String cfuStr = (parti.length > 2) ? parti[2].trim() : "0";
                            boolean completato = (parti.length > 3) ? Boolean.parseBoolean(parti[3].trim()) : (!voto.isEmpty());
                            boolean idoneita = (parti.length > 4) && Boolean.parseBoolean(parti[4].trim());
                            GestoreDatabase.salvaEsame(nomeEsame, idoneita, "N/D"); //Nelle vecchie versioni non c'era il tag
                            if (completato) {
                                GestoreDatabase.aggiornaStatoEsame(nomeEsame, true);
                                if (!voto.isEmpty()) GestoreDatabase.setVotiEsami(voto, nomeEsame, 0);
                                try {
                                    GestoreDatabase.addCfuEsame(nomeEsame, Integer.parseInt(cfuStr));
                                } catch (NumberFormatException e) {}
                            }
                            esamiImportati++;
                        }
                    } 
                    else if (sezioneAttuale.equals("IMPOSTAZIONI")) {
                        String[] parti = linea.split(";");
                        if (parti.length == 2) {
                            String chiave = parti[0];
                            String valore = parti[1];
                            switch (chiave) {
                                case "OBIETTIVO_CFU": GestoreDatabase.salvaObiettivoCfu(Integer.parseInt(valore)); break;
                                case "OBIETTIVO_MEDIA": GestoreDatabase.salvaObiettivoMedia(Integer.parseInt(valore)); break;
                                case "TEMA_SCURO": GestoreDatabase.salvaTemaScuro(Boolean.parseBoolean(valore)); break;
                                case "ORDINE_SCADENZE": GestoreDatabase.salvaOrdineScadenze(Boolean.parseBoolean(valore)); break;
                                case "LODE": GestoreDatabase.salvaImpostazione("LODE", valore); break;
                                case "BONUS_LODE": GestoreDatabase.salvaImpostazione("BONUS_LODE", valore); break;
                                // NUOVE IMPOSTAZIONI POMODORO
                                case "POMODORI": GestoreDatabase.salvaPomodori(Integer.parseInt(valore)); break;
                                case "POMODORI_DATA": GestoreDatabase.salvaDataPomodori(valore); break;
                                case "POMODORI_MAX": GestoreDatabase.salvaMaxPomodoriGiornalieri(Integer.parseInt(valore)); break;
                                case "MINUTI_STUDIO": GestoreDatabase.salvaImpostazione("MINUTI_STUDIO", valore); break;
                                case "MINUTI_PAUSA": GestoreDatabase.salvaImpostazione("MINUTI_PAUSA", valore); break;
                            }
                        }
                    }
                    else if (sezioneAttuale.equals("SCADENZE")) {
                        String[] parti = linea.split(";");
                        if (parti.length >= 2) {
                            String nomeEsameScadenza = parti[0];
                            String dataScadenza = parti[1];
                            GestoreDatabase.salvaScadenza(nomeEsameScadenza, dataScadenza);
                        }
                    }
                    // NUOVA SEZIONE STUDIO Aggiunta qui:
                    else if (sezioneAttuale.equals("STUDIO")) {
                        String[] parti = linea.split(";");
                        if (parti.length >= 2) {
                            try {
                                String nomeEsame = parti[0];
                                int minutiStrudiati = Integer.parseInt(parti[1].trim());
                                // Salviamo le ore di studio per la materia
                                GestoreDatabase.setNuovoTempoStudio(nomeEsame, minutiStrudiati);
                            } catch (NumberFormatException e) {}
                        }
                    }
                }
                
                if (GestoreDatabase.isTemaScuro()) {
                    javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                } else {
                    javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                }
                SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(this));
                DialoghiModerni.mostraMessaggio(parentComponent, "Successo", 
                        "Backup ripristinato!\nSono stati importati " + esamiImportati
                                + " esami, le ore di studio e le tue impostazioni", false);
            } catch (Exception ex) {
                DialoghiModerni.mostraMessaggio(parentComponent, "Attenzione!", 
                        "Errore durante l'importazione del file", true);
            }
        }
    }

    public void refresh() {
        examLeftPanel.removeAll();
        setExamLeft(examLeftPanel);
        mediaPanel.removeAll();
        setPanelMedia(mediaPanel);
        votiEsamiPanel.removeAll();
        setVotiEsami(votiEsamiPanel);
        panelInfo.removeAll();
        setPanelInfo(panelInfo);
        panelGraph.removeAll();
        setGraphPanel(panelGraph);

        applyResponsiveLayout();
        GestoreNotifiche.aggiornaTrofeiEAvvisa(this);

        this.revalidate();
        this.repaint();
    }
}

package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.minec.dati.GestoreDati;

// Questa riga significa: "SchermataVoti è un tipo personalizzato di JPanel"
public class PannelloVoti extends JPanel {

    private ArrayList<JPanel> examsList = new ArrayList<>();
    private JPanel mediaPanel = new JPanel();
    private JPanel examLeftPanel = new JPanel();
    private JPanel votiEsamiPanel = new JPanel();
    private JPanel panelInfo = new JPanel();
    private JPanel panelGraph = new JPanel();
    private JPanel graphVoti = new JPanel();

    private int obiettivo = 25;

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
    }

    public void setPanelMedia(JPanel mediaPanel) {
        mediaPanel.setBounds(50, 40, 200, 200);
        mediaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        mediaPanel.setLayout(new BorderLayout());
        String[] voti = GestoreDati.getVotiEsamiRaw();
        int sommaVoti = 0; // Per la ponderata (voto * cfu)
        int sommaVotiSemplice = 0; // Per l'aritmetica (solo voto)
        int sommaCfu = 0;
        int esamiValidi = 0;
        int pesoLode = GestoreDati.getPesoLode();
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

        JLabel title = new JLabel("Media Ponderata");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setHorizontalAlignment(JLabel.CENTER);

        JPanel mediaF = new JPanel();
        mediaF.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 60));
        JLabel mediaLabel = new JLabel(textP);
        mediaLabel.setFont(new Font("Arial", Font.BOLD, 35));
        mediaLabel.setHorizontalAlignment(JLabel.CENTER);
        JLabel outOfLabel = new JLabel("/30");
        outOfLabel.setFont(new Font("Arial", Font.BOLD, 27));
        outOfLabel.setHorizontalAlignment(JLabel.CENTER);

        mediaF.add(mediaLabel);
        mediaF.add(outOfLabel);

        mediaPanel.add(title, BorderLayout.NORTH);
        mediaPanel.add(mediaF, BorderLayout.CENTER);
        mediaPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Ora le maiuscole combaciano e non c'è più il refresh()!
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
        examLeftPanel.setBounds(50, 255, 200, 80);
        examLeftPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 7, 5));
        examLeftPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        int examAdded = GestoreDati.numeroEsami();
        int numVoti = GestoreDati.numeroVoti();
        for (int i = 0; i < examAdded; i++) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(10, 20));
            if (i < numVoti) {
                panel.setBackground(new Color(36, 166, 6));
            } else {
                panel.setBackground(Color.WHITE);
            }
            panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            examLeftPanel.add(panel);
            examsList.add(panel);
        }
    }

    public void setPanelInfo(JPanel panelInfo) {
        panelInfo.setBounds(50, 350, 200, 200);
        panelInfo.setLayout(new GridLayout(3, 1));

        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel panel4 = new JPanel();

        Border b = BorderFactory.createLineBorder(Color.GRAY, 2, true);
        panel1.setBorder(b);
        panel1.setLayout(new BorderLayout());
        panel2.setBorder(b);
        panel2.setLayout(new BorderLayout());
        panel3.setBorder(b);
        panel3.setLayout(new BorderLayout());
        panel4.setBorder(b);
        panel4.setLayout(new BorderLayout());

        //pannello esami fatti
        Font f = new Font("Arial", Font.BOLD, 15);
        int esamiDone = GestoreDati.numeroVoti();
        int numeroEsami = GestoreDati.numeroEsami();
        JLabel esamiRimasti = new JLabel(esamiDone + "/" + numeroEsami);
        JLabel title1 = new JLabel("Esami Passati");
        title1.setHorizontalAlignment(JLabel.CENTER);
        esamiRimasti.setFont(f);
        esamiRimasti.setHorizontalAlignment(JLabel.CENTER);
        panel1.add(esamiRimasti, BorderLayout.CENTER);
        panel1.add(title1, BorderLayout.NORTH);

        //pannello voto base di laurea
        //pannello voto base di laurea
        String[] voti = GestoreDati.getVotiEsamiRaw();
        int sommaVoti = 0;
        int sommaCfu = 0;
        int numeroLodi = 0; // Nuovo contatore per le lodi!
        int pesoLode = GestoreDati.getPesoLode();
        int bonusLode = GestoreDati.getBonusLode();
        for (int i = 0; i < voti.length; i++) {
            String[] pair = voti[i].split(";");
            if (pair.length >= 3) {
                try {
                    int cfuSingolo = Integer.parseInt(pair[2]);
                    int votoSingolo;
                    // Se l'utente ha scritto "30L" o "30l"
                    if (pair[0].equalsIgnoreCase("30L") || pair[0].equalsIgnoreCase("30 e lode")) {
                        votoSingolo = pesoLode; // Usiamo il valore scelto (es. 30 o 31)
                        numeroLodi++; // Trovata una lode!
                    } else {
                        votoSingolo = Integer.parseInt(pair[0]); // Numero normale
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
            baseL += (numeroLodi * bonusLode); // MAGIA: Aggiungiamo i punti bonus!
        }
        JLabel title2 = new JLabel("Base Laurea");
        title2.setHorizontalAlignment(JLabel.CENTER);
        JLabel baseLaurea = new JLabel(Math.round(baseL) + "/110");
        baseLaurea.setHorizontalAlignment(JLabel.CENTER);
        baseLaurea.setFont(f);
        panel2.add(title2, BorderLayout.NORTH);
        panel2.add(baseLaurea, BorderLayout.CENTER);

        //pannello obiettivo
        JLabel title3 = new JLabel("Obiettivo");
        title3.setHorizontalAlignment(JLabel.CENTER);
        JPanel votoOb = new JPanel();
        votoOb.setLayout(new GridLayout(2, 1));
        votoOb.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String newOb = JOptionPane.showInputDialog(PannelloVoti.this, "Inserire obiettivo");
                if (newOb != null && !newOb.trim().isEmpty()) {
                    try {
                        obiettivo = Integer.parseInt(newOb);
                        if (obiettivo < 18 || obiettivo > 30) {
                            throw new NumberFormatException();
                        }
                        refresh();
                    } catch (NumberFormatException e1) {
                        JOptionPane.showMessageDialog(PannelloVoti.this, "Inserire un voto valido tra 18 e 30");
                        obiettivo = 25;
                    }
                } else {
                    obiettivo = 25;
                    refresh();
                }
            }
        });
        panel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        double differenza = mediaVoti - obiettivo;
        differenza = Math.round(differenza * 10.0) / 10.0;
        JLabel obb = new JLabel(obiettivo + "/30");
        String testoDifferenza = (differenza > 0 ? "+" : "") + differenza;
        JLabel diff = new JLabel(testoDifferenza);
        if (differenza >= 0) {
            diff.setForeground(new Color(0, 150, 0));
        } else {
            diff.setForeground(Color.RED);
        }
        obb.setFont(f);
        obb.setHorizontalAlignment(JLabel.CENTER);
        diff.setFont(f);
        diff.setHorizontalAlignment(JLabel.CENTER);
        votoOb.add(obb);
        votoOb.add(diff);
        panel3.add(votoOb, BorderLayout.CENTER);
        panel3.add(title3, BorderLayout.NORTH);

        //pannello crediti rimanenti
        JLabel title4 = new JLabel("Crediti");
        title4.setHorizontalAlignment(JLabel.CENTER);
        int maxCfu = GestoreDati.getObiettivoCFU();
        if (maxCfu <= 0) {
            maxCfu = 1;
        }
        JLabel cfuRimasti = new JLabel(sommaCfu + "/" + maxCfu);
        cfuRimasti.setFont(f);
        cfuRimasti.setHorizontalAlignment(JLabel.CENTER);
        JProgressBar jp = new JProgressBar(0, maxCfu);
        jp.setValue(Math.min(sommaCfu, maxCfu));
        jp.setStringPainted(true);
        jp.setString(Math.round(((double) sommaCfu / maxCfu) * 100.0) + "%");
        jp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jp.setForeground(new Color(36, 166, 6));
        //!jp.setBackground(Color.WHITE);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        progressPanel.add(jp, BorderLayout.CENTER);
        panel4.add(progressPanel, BorderLayout.SOUTH);
        panel4.add(cfuRimasti, BorderLayout.CENTER);
        panel4.add(title4, BorderLayout.NORTH);

        panelInfo.add(panel1);
        panelInfo.add(panel2);
        panelInfo.add(panel3);
        panelInfo.add(panel4);
    }

    public void setVotiEsami(JPanel votiEsamePanel) {
        votiEsamePanel.setBounds(300, 35, 350, 270);
        votiEsamePanel.setLayout(new BorderLayout());
        Border b = BorderFactory.createMatteBorder(2, 0, 0, 0, Color.GRAY);

        String[] votiRaw = GestoreDati.getVotiEsamiRaw();
        int numVoti = GestoreDati.numeroVoti();

        JPanel votiOnly = new JPanel();
        votiOnly.setLayout(new BoxLayout(votiOnly, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(votiOnly);
        scrollPane.setBorder(BorderFactory.createTitledBorder(b, 
                      "Voti salvati", 
                            TitledBorder.CENTER, 
                            TitledBorder.TOP, 
                            new Font("Arial", Font.BOLD, 16)));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        votiEsamePanel.add(scrollPane, BorderLayout.CENTER);

        for (int i = 0; i < numVoti; i++) {
            String rigaVoto = votiRaw[i];
            String[] parti = rigaVoto.split(";");
            String voto = parti[0];
            String nomeEsame = parti[1];
            // Creiamo il pannellino per la riga
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            // Fissiamo le dimensioni per non far schiacciare il layout (come in PannelloAggiungi)
            Dimension dim = new Dimension(280, 30);
            panel.setPreferredSize(dim);
            panel.setMaximumSize(dim);
            panel.setMinimumSize(dim);
            panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            JLabel etichettaVoto = new JLabel(nomeEsame + ": " + voto);
            etichettaVoto.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(etichettaVoto);
            votiOnly.add(panel);
            // Aggiungiamo un piccolo spazio vuoto tra una riga e l'altra per l'estetica
            votiOnly.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }

    public void setGraphPanel(JPanel panelGraph) {
        panelGraph.setBounds(300, 330 , 350, 150);
        panelGraph.setLayout(new BorderLayout());
        Border b = BorderFactory.createMatteBorder(2, 0, 0, 0, Color.GRAY);
        panelGraph.setBorder(BorderFactory.createTitledBorder(b,
                "Grafico voti",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16)));
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
            String[] votiRaw = GestoreDati.getVotiEsamiRaw();
            int numVoti = GestoreDati.numeroVoti();
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
                return GestoreDati.getPesoLode();
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
            boolean isDarkMode = GestoreDati.isTemaScuro();
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
            int maxVoto = Math.max(30, GestoreDati.getPesoLode());
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
                    g2.drawString(etichette[i], x - 6, top + graphH + 15);
                    g2.setColor(colorLinea);
                }
                prevX = x;
                prevY = y;
            }
            g2.dispose();
        }
    }

    public void setOptionButton() {
        JButton refreshBut = new JButton("");
        if (GestoreDati.isTemaScuro()) {
            refreshBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24)); 
        }else {
            refreshBut.setIcon(new FlatSVGIcon("icone/opzioni.svg", 24, 24));
        }
        refreshBut.setBorderPainted(false);
        refreshBut.setFocusPainted(false);
        refreshBut.setContentAreaFilled(false);
        refreshBut.setCursor(new Cursor(Cursor.HAND_CURSOR));

        refreshBut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (GestoreDati.isTemaScuro()) {
                    refreshBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24)); 
                }else {
                    refreshBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24));
                }
                refresh();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (GestoreDati.isTemaScuro()) {
                    refreshBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24)); 
                }else {
                    refreshBut.setIcon(new FlatSVGIcon("icone/opzioni.svg", 24, 24));
                }
                refresh();
            }
        });
        refreshBut.addActionListener(e -> {
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

            JPanel pannelloImpostazioni = new JPanel();
            pannelloImpostazioni.setPreferredSize(new Dimension(360, 480));
            //!pannelloImpostazioni.setBackground(Color.WHITE);
            pannelloImpostazioni.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            pannelloImpostazioni.setLayout(new BorderLayout());

            JLabel titolo = new JLabel("Impostazioni", SwingConstants.CENTER);
            titolo.setFont(new Font("Arial", Font.BOLD, 22));
            titolo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            pannelloImpostazioni.add(titolo, BorderLayout.NORTH);

            // --- CONTENUTO CENTRALE ---
            JPanel centro = new JPanel();
            centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
            //!centro.setBackground(Color.WHITE);
            centro.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            // 1. Cambio CFU
            JPanel pnlCfu = new JPanel(new FlowLayout(FlowLayout.LEFT));
            //!pnlCfu.setBackground(Color.WHITE);
            JLabel lblCfu = new JLabel("Obiettivo CFU totali: ");
            lblCfu.setIcon(new FlatSVGIcon("icone/target.svg", 22, 22));
            pnlCfu.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
            JTextField txtCfu = new JTextField(String.valueOf(GestoreDati.getObiettivoCFU()), 4);
            JButton btnSalvaCfu = new JButton("Salva");
            btnSalvaCfu.addActionListener(ev -> {
                try {
                    int nuovoObiettivo = Integer.parseInt(txtCfu.getText());
                    GestoreDati.salvaObiettivoCfu(nuovoObiettivo);
                    JOptionPane.showMessageDialog(pannelloImpostazioni, "Obiettivo salvato!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(pannelloImpostazioni, "Inserisci un numero valido!");
                }
            });
            pnlCfu.add(lblCfu);
            pnlCfu.add(txtCfu);
            pnlCfu.add(btnSalvaCfu);

            // 2. Ordine scadenze
            JPanel pnlOrdine = new JPanel(new FlowLayout(FlowLayout.LEFT));
            //!pnlOrdine.setBackground(Color.WHITE);
            pnlOrdine.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            JLabel lblOrdine = new JLabel("Ordine predefinito scadenze: ");
            lblOrdine.setIcon(new FlatSVGIcon("icone/calendar.svg", 22, 22));
            lblOrdine.setIconTextGap(5);
            boolean ordinePreferito = GestoreDati.getOrdineScadenza();
            JButton btnOrdine = new JButton(ordinePreferito ? "Aggiunta" : "Cronologico");
            btnOrdine.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOrdine.addActionListener(ez -> {
                String text1 = btnOrdine.getText();
                if (text1.equals("Cronologico")) {
                    btnOrdine.setText("Aggiunta");
                    GestoreDati.salvaOrdineScadenze(true);
                } else {
                    btnOrdine.setText("Cronologico");
                    GestoreDati.salvaOrdineScadenze(false);
                }
            });
            pnlOrdine.add(lblOrdine);
            pnlOrdine.add(btnOrdine);

            // 3. MENU A SCOMPARSA: Parametri Laurea
            JPanel pnlGruppoParametri = new JPanel();
            pnlGruppoParametri.setLayout(new BoxLayout(pnlGruppoParametri, BoxLayout.Y_AXIS));
            //!pnlGruppoParametri.setBackground(Color.WHITE);
            pnlGruppoParametri.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
            pnlGruppoParametri.setMaximumSize(new Dimension(340, 200));

            // A. L'Intestazione (Cliccabile)
            JPanel pnlHeader = new JPanel(new BorderLayout());
            //!pnlHeader.setBackground(new Color(245, 245, 250));
            pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            pnlHeader.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JLabel lblTitoloParam = new JLabel("Parametri Laurea");
            lblTitoloParam.setIcon(new FlatSVGIcon("icone/hat.svg", 22, 22));
            lblTitoloParam.setIconTextGap(5);
            lblTitoloParam.setFont(new Font("Arial", Font.BOLD, 14));
            JLabel lblFreccia = new JLabel("▼");
            pnlHeader.add(lblTitoloParam, BorderLayout.WEST);
            pnlHeader.add(lblFreccia, BorderLayout.EAST);

            // B. Il Contenuto (Invisibile all'inizio)
            JPanel pnlContenuto = new JPanel(new GridLayout(3, 2, 10, 10));
            //!pnlContenuto.setBackground(Color.WHITE);
            pnlContenuto.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            pnlContenuto.setVisible(false);
            pnlContenuto.add(new JLabel(" Valore Lode (es. 30):"));
            JTextField txtLode = new JTextField(GestoreDati.getImpostazione("LODE", "30"));
            pnlContenuto.add(txtLode);
            pnlContenuto.add(new JLabel(" Punti Bonus extra:"));
            JTextField txtBonus = new JTextField(GestoreDati.getImpostazione("BONUS_LODE", "0"));
            pnlContenuto.add(txtBonus);
            JButton btnSalvaParametri = new JButton("Salva");
            btnSalvaParametri.addActionListener(ev -> {
                GestoreDati.salvaImpostazione("LODE", txtLode.getText());
                GestoreDati.salvaImpostazione("BONUS_LODE", txtBonus.getText());
                JOptionPane.showMessageDialog(pannelloImpostazioni, "Parametri salvati!");
            });
            pnlContenuto.add(new JLabel(""));
            pnlContenuto.add(btnSalvaParametri);

            // C. L'Azione del Click sull'Intestazione
            pnlHeader.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    boolean isVisible = pnlContenuto.isVisible();
                    pnlContenuto.setVisible(!isVisible);
                    lblFreccia.setText(isVisible ? "▼" : "▲");
                    pannelloImpostazioni.revalidate();
                    pannelloImpostazioni.repaint();
                }
            });
            pnlGruppoParametri.add(pnlHeader);
            pnlGruppoParametri.add(pnlContenuto);

            // 4. Bottone Reset
            JPanel pnlReset = new JPanel(new FlowLayout(FlowLayout.LEFT));
            //!pnlReset.setBackground(Color.WHITE);
            pnlReset.setBorder(BorderFactory.createEmptyBorder(20, 60, 0, 0));
            JButton btnReset = new JButton("Cancella tutti i dati");
            btnReset.setIcon(new FlatSVGIcon("icone/bin1.svg", 22, 22));
            btnReset.setForeground(Color.RED);
            btnReset.setFont(new Font("Arial", Font.BOLD, 14));
            btnReset.addActionListener(ev -> {
                int conf1 = JOptionPane.showConfirmDialog(pannelloImpostazioni,
                        "Vuoi davvero svuotare il libretto?", "Conferma Reset", JOptionPane.YES_NO_OPTION);
                if (conf1 == JOptionPane.YES_OPTION) {
                    GestoreDati.resetTutto();
                    JOptionPane.showMessageDialog(pannelloImpostazioni, "Dati azzerati. L'applicazione si chiuderà.");
                    System.exit(0);
                }
            });
            pnlReset.add(btnReset);

            // 5. cambia modalità colore
            JPanel pnlTema = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlTema.setBorder(BorderFactory.createEmptyBorder(0, 85, 0, 0));
            JLabel lblTema = new JLabel("Modalità Scura: ");
            if (GestoreDati.isTemaScuro()) {
                lblTema.setIcon(new FlatSVGIcon("icone/dark2.svg", 20, 20)); 
            }else {
                lblTema.setIcon(new FlatSVGIcon("icone/dark1.svg", 20, 20));
            }
            JCheckBox chkTema = new JCheckBox();
            chkTema.setSelected(GestoreDati.isTemaScuro()); // Mette la spunta se era già scuro
            chkTema.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chkTema.addActionListener(ev -> {
                boolean isScuro = chkTema.isSelected();
                GestoreDati.salvaTemaScuro(isScuro);
                try {
                    if (isScuro) {
                        javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                        lblTema.setIcon(new FlatSVGIcon("icone/dark2.svg", 20, 20));
                        refreshBut.setIcon(new FlatSVGIcon("icone/opzioniH.svg", 24, 24));
                    } else {
                        javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                        lblTema.setIcon(new FlatSVGIcon("icone/dark1.svg", 20, 20));
                        refreshBut.setIcon(new FlatSVGIcon("icone/opzioni.svg", 24, 24));
                    }
                    SwingUtilities.updateComponentTreeUI(frame);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            pnlTema.add(lblTema);
            pnlTema.add(chkTema);

            // ASSEMBLIAMO I PEZZI NELL'ORDINE GIUSTO NEL CENTRO
            centro.add(Box.createRigidArea(new Dimension(0, 10)));
            centro.add(pnlCfu);
            centro.add(pnlOrdine);
            centro.add(pnlTema);
            centro.add(Box.createRigidArea(new Dimension(0, 10)));
            centro.add(pnlGruppoParametri); // <--- Inserito qui!
            centro.add(pnlReset);
            pannelloImpostazioni.add(centro, BorderLayout.CENTER);

            // --- BOTTONE CHIUDI ---
            JButton btnChiudi = new JButton("Chiudi");
            btnChiudi.setFont(new Font("Arial", Font.BOLD, 14));
            btnChiudi.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnChiudi.addActionListener(chiudiEvent -> {
                shadowOverlay.setVisible(false);
            });

            JPanel panelBottone = new JPanel(new FlowLayout(FlowLayout.CENTER));
            //!panelBottone.setBackground(Color.WHITE);
            panelBottone.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
            panelBottone.add(btnChiudi);
            pannelloImpostazioni.add(panelBottone, BorderLayout.SOUTH);

            shadowOverlay.add(pannelloImpostazioni, new GridBagConstraints());
            frame.setGlassPane(shadowOverlay);
            shadowOverlay.setVisible(true);
        });

        Color coloreHover = new Color(48, 68, 88);
        Color coloreSfondo = mediaPanel.getBackground();
        refreshBut.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refreshBut.setContentAreaFilled(true);
                refreshBut.setBackground(coloreHover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refreshBut.setContentAreaFilled(false);
                refreshBut.setBackground(coloreSfondo);
            }
        });

        JPanel butPanel = new JPanel();
        butPanel.setBounds(710, 30, 40, 40);
        butPanel.setLayout(new BorderLayout());
        butPanel.add(refreshBut, BorderLayout.CENTER);
        this.add(butPanel);
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

        this.revalidate();
        this.repaint();
    }
}

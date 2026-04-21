package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.minec.dati.GestoreDati;

public class PannelloPomodoro extends JPanel{
    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 1.8f;

    private JLabel lblTimer;
    private JLabel lblStato;
    private JButton btnStartPause;
    private JButton btnReset;
    private JComboBox<String> comboEsami;
    private JButton optionBut;
    private JPanel optionButtonPanel;
    private JPanel optionLeftSpacerPanel;
    private int optionIconSize = 24;
    private JLabel lblContatore;
    private JLabel lblMaxPomodori;
    private int conteggioPomodori = 0;
    private int maxPomodoriGiornalieri = 0;

    private Timer timer;
    private int secondiRimanenti;
    private boolean inEsecuzione = false;
    private boolean isSessioneStudio = true;
    
    private static int MINUTI_STUDIO = 25;
    private static int MINUTI_PAUSA = 5;

    public PannelloPomodoro() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // ---Titolo e Stato---
        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        JLabel title = new JLabel("Timer Pomodoro", CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        lblStato = new JLabel("Pronto per studiare?", SwingConstants.CENTER);
        lblStato.setFont(new Font("Arial", Font.ITALIC, 18));
        lblStato.setForeground(Color.GRAY);
        topPanel.add(title);
        topPanel.add(lblStato);

        JPanel headerPanel = new JPanel(new BorderLayout());
        setOptionButton();
        optionLeftSpacerPanel = new JPanel();
        optionLeftSpacerPanel.setOpaque(false);
        headerPanel.add(optionLeftSpacerPanel, BorderLayout.WEST);
        headerPanel.add(topPanel, BorderLayout.CENTER);
        headerPanel.add(optionButtonPanel, BorderLayout.EAST);
        this.add(headerPanel, BorderLayout.NORTH);
        // --- Menu Tendina ---
        JPanel pnlSelezione = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblSelezione = new JLabel("Cosa stai studiando?");
        comboEsami = new JComboBox<>();
        aggiornaListaEsami();
        pnlSelezione.add(lblSelezione);
        pnlSelezione.add(comboEsami);
        topPanel.add(pnlSelezione);

        // --- Timer Display ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        lblTimer = new JLabel("25:00", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Monospaced", Font.BOLD, 100));
        lblTimer.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra orizzontalmente
        lblContatore = new JLabel(" Sessioni completate: 0");
        lblContatore.setIcon(new FlatSVGIcon("icone/tomato.svg", 24 ,24));
        lblContatore.setFont(new Font("Arial", Font.BOLD, 18));
        lblContatore.setForeground(new Color(231, 76, 60)); // Colore "rosso pomodoro"
        lblContatore.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra orizzontalmente
        lblMaxPomodori = new JLabel(" Max Pomodori: 0");
        lblMaxPomodori.setIcon(new FlatSVGIcon("icone/fire.svg", 24, 24));
        lblMaxPomodori.setFont(new Font("Arial", Font.BOLD, 18));
        lblMaxPomodori.setForeground(new Color(231, 76, 60)); // Colore "rosso pomodoro"
        lblMaxPomodori.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra orizzontalmente

        // Aggiungiamo spazi flessibili per centrare perfettamente tutto nel mezzo
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(lblTimer);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spazio tra timer e testo
        centerPanel.add(lblContatore);
        centerPanel.add(lblMaxPomodori);
        centerPanel.add(Box.createVerticalGlue());
        this.add(centerPanel, BorderLayout.CENTER);
        setPomoCounter();

        // --- Controlli ---
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnStartPause = new JButton("Avvia");
        btnReset = new JButton("Reset");
        btnStartPause.setPreferredSize(new Dimension(120, 40));
        btnReset.setPreferredSize(new Dimension(120, 40));
        btnStartPause.addActionListener(e -> toggleTimer());
        btnReset.addActionListener(e -> resetTimer());
        botPanel.add(btnStartPause);
        botPanel.add(btnReset);
        this.add(botPanel, BorderLayout.SOUTH);
        setNewMinutes();
        resetTimer(); // Inizializza lo stato

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveOptionButtonLayout();
            }
        });
        SwingUtilities.invokeLater(this::applyResponsiveOptionButtonLayout);
    }

    private void toggleTimer() {
        sincronizzaContatorePomodoriGiornaliero();
        if (inEsecuzione) {
            pausaTimer();
        } else {
            avviaTimer();
        }
    }

    private void avviaTimer() {
        inEsecuzione = true;
        btnStartPause.setText("Pausa");
        if(isSessioneStudio) {
            lblStato.setIcon(new FlatSVGIcon("icone/books.svg" , 20, 20));
            lblStato.setText(" Sessione di Studio...");
        } else {
            lblStato.setIcon(new FlatSVGIcon("icone/coffee.svg" , 20, 20));
            lblStato.setText(" Pausa...");
        }
        lblStato.setForeground(isSessioneStudio ? new Color(231, 76, 60) : new Color(46, 204, 113));

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (secondiRimanenti > 0) {
                    secondiRimanenti--;
                    aggiornaLabelTimer();
                } else {
                    timerFinito();
                }
            }
        }, 1000, 1000);
    }

    private void pausaTimer() {
        inEsecuzione = false;
        btnStartPause.setText("Riprendi");
        if (timer != null) timer.cancel();
    }

    private void resetTimer() {
        sincronizzaContatorePomodoriGiornaliero();
        pausaTimer();
        isSessioneStudio = true;
        secondiRimanenti = MINUTI_STUDIO * 60;
        btnStartPause.setText("Avvia");
        lblStato.setIcon(new FlatSVGIcon("icone/books.svg", 20, 20));
        lblStato.setText("Pronto per studiare?");
        lblStato.setForeground(Color.GRAY);
        aggiornaLabelTimer();
    }

    private void aggiornaLabelTimer() {
        int min = secondiRimanenti / 60;
        int sec = secondiRimanenti % 60;
        // SwingUtilities.invokeLater assicura che il cambio grafico avvenga nel thread giusto
        SwingUtilities.invokeLater(() -> lblTimer.setText(String.format("%02d:%02d", min, sec)));
    }

    public void aggiornaListaEsami() {
        comboEsami.removeAllItems();
        String[] esamiRaw = com.minec.dati.GestoreDati.getEsamiSalvatiRaw();
        for (String riga : esamiRaw) {
            String nome = riga.split(";")[0];
            comboEsami.addItem(nome);
        }
    }

    private void timerFinito() {
        sincronizzaContatorePomodoriGiornaliero();
        pausaTimer();
        riproduciSuono();
        // Notifica di sistema usando il tuo GestoreNotifiche!
        String titolo = isSessioneStudio ? "Studio Completato!" : "Pausa Finita!";
        String msg = isSessioneStudio ? "Ottimo lavoro! Ora prenditi 5 minuti di pausa." : "La pausa è finita, torna sui libri!";
        if (isSessioneStudio) {
            conteggioPomodori++;
            String esameSelezionato = (String) comboEsami.getSelectedItem();
            if(esameSelezionato != null) {
                GestoreDati.aggiungiTempoStudio(esameSelezionato, MINUTI_STUDIO);
            }
            GestoreDati.salvaPomodori(conteggioPomodori);
            aggiornaSerieMaxSeNecessario();
            aggiornaLblContatore();
            titolo = "Studio Completato!";
            msg = "Ottimo lavoro! Ora prenditi 5 minuti di pausa.";
        } else {
            titolo = "Pausa Finita!";
            msg = "La pausa è finita, torna sui libri!";
        }
        JOptionPane.showMessageDialog(this, msg, titolo, JOptionPane.INFORMATION_MESSAGE);
        // Cambia modalità (se era studio passa a pausa, e viceversa)
        isSessioneStudio = !isSessioneStudio;
        secondiRimanenti = (isSessioneStudio ? MINUTI_STUDIO : MINUTI_PAUSA) * 60;
        aggiornaLabelTimer();
        btnStartPause.setText("Avvia " + (isSessioneStudio ? "Studio" : "Pausa"));
    }

    private void riproduciSuono() {
        java.net.URL urlAudio = getClass().getResource("/suoni/ding.wav");
        if (urlAudio == null) {
            System.out.println("DEBUG: File audio non trovato nelle risorse interne (/suoni/ding.wav)");
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        // Riproduci in un thread dedicato per non bloccare la UI e migliorare la compatibilita' audio.
        Thread audioThread = new Thread(() -> {
            try (AudioInputStream audioOriginale = AudioSystem.getAudioInputStream(urlAudio)) {
                AudioFormat formatoBase = audioOriginale.getFormat();
                AudioFormat formatoPCM = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    formatoBase.getSampleRate(),
                    16,
                    formatoBase.getChannels(),
                    formatoBase.getChannels() * 2,
                    formatoBase.getSampleRate(),
                    false
                );

                try (AudioInputStream audioDecodificato = AudioSystem.getAudioInputStream(formatoPCM, audioOriginale)) {
                    DataLine.Info info = new DataLine.Info(Clip.class, formatoPCM);
                    Clip clip = (Clip) AudioSystem.getLine(info);
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                    clip.open(audioDecodificato);
                    clip.setFramePosition(0);
                    clip.start();
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Errore durante la riproduzione del suono - " + e.getMessage());
                Toolkit.getDefaultToolkit().beep();
            }
        }, "PomodoroAudioThread");

        audioThread.setDaemon(true);
        audioThread.start();
    }

    public void setOptionButton() {
        optionBut = new JButton("");
        optionBut.setBorderPainted(false);
        optionBut.setFocusPainted(false);
        optionBut.setContentAreaFilled(false);
        optionBut.setCursor(new Cursor(Cursor.HAND_CURSOR));

        optionBut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                applyOptionButtonAppearance(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                applyOptionButtonAppearance(false);
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

            JPanel pannelloImpostazioni = new JPanel();
            pannelloImpostazioni.setPreferredSize(new Dimension(360, 480));
            pannelloImpostazioni.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            pannelloImpostazioni.setLayout(new BorderLayout());

            JLabel titolo = new JLabel("Impostazioni", SwingConstants.CENTER);
            titolo.setFont(new Font("Arial", Font.BOLD, 22));
            titolo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            pannelloImpostazioni.add(titolo, BorderLayout.NORTH);

            // --- CONTENUTO CENTRALE ---
            JPanel centro = new JPanel();
            centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
            centro.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            // 1. MENU A SCOMPARSA: Parametri Laurea
            final int dropdownWidth = 340;
            final int dropdownClosedHeight = 48;
            final int dropdownOpenHeight = 150;

            JPanel pnlGruppoParametri = new JPanel();
            pnlGruppoParametri.setLayout(new BoxLayout(pnlGruppoParametri, BoxLayout.Y_AXIS));
            pnlGruppoParametri.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
            pnlGruppoParametri.setMaximumSize(new Dimension(dropdownWidth, dropdownClosedHeight));
            pnlGruppoParametri.setPreferredSize(new Dimension(dropdownWidth, dropdownClosedHeight));

            // A. L'Intestazione (Cliccabile)
            JPanel pnlHeader = new JPanel(new BorderLayout());
            pnlHeader.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            pnlHeader.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JLabel lblTitoloParam = new JLabel(" Parametri Timer:");
            lblTitoloParam.setIcon(new FlatSVGIcon("icone/clock.svg", 22, 22));
            lblTitoloParam.setIconTextGap(5);
            lblTitoloParam.setFont(new Font("Arial", Font.BOLD, 14));
            lblTitoloParam.setBorder(BorderFactory.createEmptyBorder(0,65,0,0));
            JLabel lblFreccia = new JLabel("▼");
            pnlHeader.add(lblTitoloParam, BorderLayout.WEST);
            pnlHeader.add(lblFreccia, BorderLayout.EAST);

            // B. Il Contenuto (Invisibile all'inizio)
            JPanel pnlContenuto = new JPanel(new GridLayout(3, 2, 6, 6));
            pnlContenuto.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            pnlContenuto.setVisible(false);
            pnlContenuto.add(new JLabel(" Minuti di studio:"));
            JTextField txtStudio = new JTextField(String.valueOf(MINUTI_STUDIO));
            pnlContenuto.add(txtStudio);
            pnlContenuto.add(new JLabel(" Minuti di pausa:"));
            JTextField txtPausa = new JTextField(String.valueOf(MINUTI_PAUSA));
            pnlContenuto.add(txtPausa);
            JButton btnSalvaParametri = new JButton("Salva");
            btnSalvaParametri.addActionListener(ev -> {
                try {
                    int nuoviMinutiStudio = Integer.parseInt(txtStudio.getText().trim());
                    int nuoviMinutiPausa = Integer.parseInt(txtPausa.getText().trim());
                    if (nuoviMinutiStudio <= 0 || nuoviMinutiPausa <= 0) {
                        throw new NumberFormatException();
                    }

                    GestoreDati.salvaImpostazione("MINUTI_STUDIO", String.valueOf(nuoviMinutiStudio));
                    GestoreDati.salvaImpostazione("MINUTI_PAUSA", String.valueOf(nuoviMinutiPausa));
                    setNewMinutes();
                    if (!inEsecuzione) {
                        resetTimer();
                    }
                    JOptionPane.showMessageDialog(pannelloImpostazioni, "Parametri salvati!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(pannelloImpostazioni, "Inserisci minuti validi (numeri interi maggiori di 0).");
                }
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

                    int nuovaAltezza = isVisible ? dropdownClosedHeight : dropdownOpenHeight;
                    Dimension nuovaDimensione = new Dimension(dropdownWidth, nuovaAltezza);
                    pnlGruppoParametri.setMaximumSize(nuovaDimensione);
                    pnlGruppoParametri.setPreferredSize(nuovaDimensione);

                    pannelloImpostazioni.revalidate();
                    pannelloImpostazioni.repaint();
                }
            });
            pnlGruppoParametri.add(pnlHeader);
            pnlGruppoParametri.add(pnlContenuto);

            // 4. Bottone Reset
            JPanel pnlReset = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlReset.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 0));
            JButton btnReset = new JButton("Azzera Max Pomodori");
            btnReset.setIcon(new FlatSVGIcon("icone/bin1.svg", 22, 22));
            btnReset.setForeground(Color.RED);
            btnReset.setFont(new Font("Arial", Font.BOLD, 14));
            btnReset.addActionListener(ev -> {
                int conf1 = JOptionPane.showConfirmDialog(pannelloImpostazioni,
                        "Vuoi davvero azzerare il max pomodori?", "Conferma Reset", JOptionPane.YES_NO_OPTION);
                if (conf1 == JOptionPane.YES_OPTION) {
                    GestoreDati.salvaMaxPomodoriGiornalieri(0);
                    maxPomodoriGiornalieri = 0;
                    aggiornaLblContatore();
                    JOptionPane.showMessageDialog(pannelloImpostazioni, "Max pomodori azzerato.");
                }
            });
            pnlReset.add(btnReset);

            // ASSEMBLIAMO I PEZZI NELL'ORDINE GIUSTO NEL CENTRO
            centro.add(Box.createRigidArea(new Dimension(0, 10)));
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
            panelBottone.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
            panelBottone.add(btnChiudi);
            pannelloImpostazioni.add(panelBottone, BorderLayout.SOUTH);

            shadowOverlay.add(pannelloImpostazioni, new GridBagConstraints());
            frame.setGlassPane(shadowOverlay);
            shadowOverlay.setVisible(true);
        });

        optionButtonPanel = new JPanel(new GridBagLayout());
        optionButtonPanel.setOpaque(false);
        GridBagConstraints gbcOption = new GridBagConstraints();
        gbcOption.gridx = 0;
        gbcOption.gridy = 0;
        gbcOption.anchor = GridBagConstraints.NORTHEAST;
        gbcOption.fill = GridBagConstraints.NONE;
        gbcOption.weightx = 1;
        gbcOption.weighty = 1;
        optionButtonPanel.add(optionBut, gbcOption);
        applyOptionButtonAppearance(false);
    }

    public void setPomoCounter() {
        sincronizzaContatorePomodoriGiornaliero();
        conteggioPomodori = GestoreDati.getPomodori();
        maxPomodoriGiornalieri = GestoreDati.getMaxPomodoriGiornalieri();
        aggiornaSerieMaxSeNecessario();
        aggiornaLblContatore();
    }

    private void aggiornaSerieMaxSeNecessario() {
        if (conteggioPomodori > maxPomodoriGiornalieri) {
            maxPomodoriGiornalieri = conteggioPomodori;
            GestoreDati.salvaMaxPomodoriGiornalieri(maxPomodoriGiornalieri);
        }
    }

    private void aggiornaLblContatore() {
        lblContatore.setText(" Sessioni completate: " + conteggioPomodori);
        lblContatore.setIcon(new FlatSVGIcon("icone/tomato.svg", 24, 24));
        lblMaxPomodori.setText(" Max Pomodori: " + maxPomodoriGiornalieri);
        lblMaxPomodori.setIcon(new FlatSVGIcon("icone/fire.svg", 24, 24));
    }

    private void sincronizzaContatorePomodoriGiornaliero() {
        maxPomodoriGiornalieri = GestoreDati.getMaxPomodoriGiornalieri();
        String oggi = LocalDate.now().toString();
        String dataSalvata = GestoreDati.getDataPomodori();
        if (!oggi.equals(dataSalvata)) {
            conteggioPomodori = 0;
            GestoreDati.salvaPomodori(0);
            GestoreDati.salvaDataPomodori(oggi);
            aggiornaLblContatore();
            return;
        }
        conteggioPomodori = GestoreDati.getPomodori();
    }

    private void applyResponsiveOptionButtonLayout() {
        if (optionButtonPanel == null || optionBut == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        float scaleX = (float) getWidth() / BASE_WIDTH;
        float scaleY = (float) getHeight() / BASE_HEIGHT;
        float currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.min(scaleX, scaleY)));

        optionIconSize = Math.max(20, Math.round(24 * currentScale));
        int marginTop = Math.max(6, Math.round(8 * currentScale));
        int marginRight = Math.max(6, Math.round(10 * currentScale));
        optionButtonPanel.setBorder(new EmptyBorder(marginTop, 0, 0, marginRight));
        int buttonSize = optionIconSize + 8;
        Dimension buttonDim = new Dimension(buttonSize, buttonSize);
        optionBut.setPreferredSize(buttonDim);
        optionBut.setMinimumSize(buttonDim);
        optionBut.setMaximumSize(buttonDim);
        optionButtonPanel.setPreferredSize(new Dimension(buttonSize + marginRight, buttonSize + marginTop));
        if (optionLeftSpacerPanel != null) {
            optionLeftSpacerPanel.setPreferredSize(optionButtonPanel.getPreferredSize());
        }
        applyOptionButtonAppearance(optionBut.getModel().isRollover());
    }

    private void updateOptionIcon(boolean hover) {
        if (optionBut == null) {
            return;
        }

        String iconPath;
        if (GestoreDati.isTemaScuro()) {
            iconPath = "icone/opzioniH.svg";
        } else {
            iconPath = hover ? "icone/opzioniH.svg" : "icone/opzioni.svg";
        }
        optionBut.setIcon(new FlatSVGIcon(iconPath, optionIconSize, optionIconSize));
    }

    private void applyOptionButtonAppearance(boolean hover) {
        if (optionBut == null) {
            return;
        }

        Color coloreHover = new Color(48, 68, 88);
        Color coloreSfondo = getBackground();
        updateOptionIcon(hover);

        if (hover) {
            optionBut.setContentAreaFilled(true);
            optionBut.setBackground(coloreHover);
        } else {
            optionBut.setContentAreaFilled(false);
            optionBut.setBackground(coloreSfondo);
        }
    }

    public void refresh() {
        if (optionBut != null) {
            applyOptionButtonAppearance(optionBut.getModel().isRollover());
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (optionBut != null) {
            SwingUtilities.invokeLater(() -> applyOptionButtonAppearance(optionBut.getModel().isRollover()));
        }
    }

    public void setNewMinutes() {
        try {
            int minutiStudio = GestoreDati.getMinutiStudio();
            int minutiPausa = GestoreDati.getMinutiPausa();
            if (minutiStudio <= 0 || minutiPausa <= 0) {
                throw new NumberFormatException();
            }
            MINUTI_STUDIO = minutiStudio;
            MINUTI_PAUSA = minutiPausa;
        } catch (Exception e) {
            MINUTI_STUDIO = 25;
            MINUTI_PAUSA = 5;
            GestoreDati.salvaImpostazione("MINUTI_STUDIO", String.valueOf(MINUTI_STUDIO));
            GestoreDati.salvaImpostazione("MINUTI_PAUSA", String.valueOf(MINUTI_PAUSA));
        }
        this.revalidate();
        this.repaint();
    }
}

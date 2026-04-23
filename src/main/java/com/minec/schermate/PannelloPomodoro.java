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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.minec.dati.GestoreDati;

public class PannelloPomodoro extends JPanel{
    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;
    private static final float MIN_SCALE = 0.9f;
    private static final float MAX_SCALE = 2.0f;

    private JLabel lblTimer;
    private JLabel lblStato;
    private JButton btnStartPause;
    private JButton btnReset;
    private JComboBox<String> comboEsami;
    private JButton optionBut;
    private JButton trophyBut;
    private JRadioButton radioStudio;
    private JRadioButton radioPausa;
    private ButtonGroup gruppoSessione;
    private JProgressBar barraProgressi;
    private JPanel optionButtonPanel;
    private JPanel trophyButtonPanel;
    private JPanel optionLeftSpacerPanel;
    private int optionIconSize = 24;
    private JLabel lblContatore;
    private JLabel lblMaxPomodori;
    private JLabel lblTitle;
    private JLabel lblSelezione;
    private int conteggioPomodori = 0;
    private int maxPomodoriGiornalieri = 0;

    private Timer timer;
    private int secondiRimanenti;
    private long millisecondiRimanenti;
    private long timestampFineTimer;
    private boolean inEsecuzione = false;
    private boolean isSessioneStudio = true;
    
    private static int MINUTI_STUDIO = 25;
    private static int MINUTI_PAUSA = 5;

    public PannelloPomodoro() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // ---Titolo e Stato---
        JPanel topPanel = new JPanel(new GridLayout(4, 1));
        lblTitle = new JLabel("Timer Pomodoro", CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblStato = new JLabel("Pronto per studiare?", SwingConstants.CENTER);
        lblStato.setFont(new Font("Arial", Font.ITALIC, 18));
        lblStato.setForeground(Color.GRAY);
        setTrophyButton();
        topPanel.add(lblTitle);
        topPanel.add(lblStato);

        JPanel headerPanel = new JPanel(new BorderLayout());
        setOptionButton();
        headerPanel.add(trophyButtonPanel, BorderLayout.WEST);
        headerPanel.add(topPanel, BorderLayout.CENTER);
        headerPanel.add(optionButtonPanel, BorderLayout.EAST);
        this.add(headerPanel, BorderLayout.NORTH);
        // --- Menu Tendina ---
        JPanel pnlSelezione = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lblSelezione = new JLabel("Cosa stai studiando?");
        comboEsami = new JComboBox<>();
        aggiornaListaEsami();
        pnlSelezione.add(lblSelezione);
        pnlSelezione.add(comboEsami);
        topPanel.add(pnlSelezione);
        
        // --- Selettore Timer ---
        JPanel pnlTipoSezione = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlTipoSezione.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
        radioStudio = new JRadioButton(" Studio");
        radioStudio.setIcon(new FlatSVGIcon("icone/books.svg", 20, 20));
        radioStudio.setFont(new Font("Arial", Font.BOLD, 14));
        radioStudio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        radioStudio.setSelected(true);
        radioPausa = new JRadioButton(" Pausa");
        radioPausa.setIcon(new FlatSVGIcon("icone/coffee.svg", 20, 20));
        radioPausa.setFont(new Font("Arial", Font.BOLD, 14));
        radioPausa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gruppoSessione = new ButtonGroup();
        gruppoSessione.add(radioStudio);
        gruppoSessione.add(radioPausa);
        pnlTipoSezione.add(radioStudio);
        pnlTipoSezione.add(radioPausa);
        topPanel.add(pnlTipoSezione);
        radioStudio.addActionListener(e -> cambiaTipoSessione(true));
        radioPausa.addActionListener(e -> cambiaTipoSessione(false));

        // --- Timer Display ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        lblTimer = new JLabel("25:00", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Monospaced", Font.BOLD, 100));
        lblTimer.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra orizzontalmente
        lblContatore = new JLabel(" Sessioni completate: 0");
        lblContatore.setIcon(new FlatSVGIcon("icone/tomato.svg", 15 ,15));
        lblContatore.setFont(new Font("Arial", Font.BOLD, 14));
        lblContatore.setForeground(new Color(231, 76, 60)); // Colore "rosso pomodoro"
        lblContatore.setHorizontalAlignment(SwingConstants.CENTER);
        lblContatore.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra orizzontalmente
        lblMaxPomodori = new JLabel(" Max Pomodori: 0");
        lblMaxPomodori.setIcon(new FlatSVGIcon("icone/fire.svg", 15, 15));
        lblMaxPomodori.setFont(new Font("Arial", Font.BOLD, 14));
        lblMaxPomodori.setForeground(new Color(231, 76, 60)); // Colore "rosso pomodoro"
        lblMaxPomodori.setHorizontalAlignment(SwingConstants.CENTER);
        lblMaxPomodori.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra orizzontalmente
        barraProgressi = new JProgressBar(0, (isSessioneStudio ? MINUTI_STUDIO : MINUTI_PAUSA) * 60);
        barraProgressi.setForeground((isSessioneStudio ? Color.RED : Color.BLUE));
        barraProgressi.setBackground(Color.GRAY);
        barraProgressi.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        barraProgressi.setValue((isSessioneStudio ? MINUTI_STUDIO : MINUTI_PAUSA) * 60);
        barraProgressi.setPreferredSize(new Dimension(400, 14));
        barraProgressi.setStringPainted(false);

        // Container dedicato per barra (non si espande)
        JPanel panelBarra = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBarra.setOpaque(false);
        panelBarra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panelBarra.add(barraProgressi);

        // Panel interno con barra, timer e contatori (layout fisso compatto)
        JPanel timerBlock = new JPanel();
        timerBlock.setLayout(new BoxLayout(timerBlock, BoxLayout.Y_AXIS));
        timerBlock.setOpaque(false);
        timerBlock.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerBlock.add(panelBarra);
        timerBlock.add(Box.createRigidArea(new Dimension(0, 15)));
        lblTimer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        timerBlock.add(lblTimer);
        timerBlock.add(Box.createRigidArea(new Dimension(0, 10))); 
        lblContatore.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        timerBlock.add(lblContatore);
        lblMaxPomodori.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        timerBlock.add(lblMaxPomodori);

        // Aggiungiamo spazi elastici solo sopra e sotto il blocco timer
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(timerBlock);
        centerPanel.add(Box.createVerticalGlue());
        this.add(centerPanel, BorderLayout.CENTER);
        setPomoCounter();

        // --- Controlli ---
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        btnStartPause = new JButton("Avvia");
        btnStartPause.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Sans-Serif", Font.BOLD, 15));
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
                applyResponsiveProgressBarSize();
                applyResponsiveFonts();
            }
        });
        SwingUtilities.invokeLater(() -> {
            applyResponsiveOptionButtonLayout();
            applyResponsiveProgressBarSize();
            applyResponsiveFonts();
        });
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

        timestampFineTimer = System.currentTimeMillis() + millisecondiRimanenti;
        timer = new Timer(100, e -> {
            millisecondiRimanenti = Math.max(0, timestampFineTimer - System.currentTimeMillis());
            secondiRimanenti = (int) Math.ceil(millisecondiRimanenti / 1000.0);
            aggiornaTimerEProgressBar();
            if (millisecondiRimanenti <= 0) {
                timerFinito();
            }
        });
        timer.start();
    }

    private void pausaTimer() {
        inEsecuzione = false;
        btnStartPause.setText("Riprendi");
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void resetTimer() {
        sincronizzaContatorePomodoriGiornaliero();
        pausaTimer();
        isSessioneStudio = radioStudio != null ? radioStudio.isSelected() : isSessioneStudio;
        impostaDurataSessioneCorrente();
        btnStartPause.setText("Avvia");
        lblStato.setIcon(new FlatSVGIcon(isSessioneStudio ? "icone/books.svg" : "icone/coffee.svg", 20, 20));
        lblStato.setText(isSessioneStudio ? "Pronto per studiare?" : "Pronto per la pausa");
        lblStato.setForeground(Color.GRAY);
        aggiornaTimerEProgressBar();
    }

    private void aggiornaLabelTimer() {
        int min = secondiRimanenti / 60;
        int sec = secondiRimanenti % 60;
        // SwingUtilities.invokeLater assicura che il cambio grafico avvenga nel thread giusto
        SwingUtilities.invokeLater(() -> lblTimer.setText(String.format("%02d:%02d", min, sec)));
    }

    private int getDurataSessioneCorrenteSecondi() {
        return (isSessioneStudio ? MINUTI_STUDIO : MINUTI_PAUSA) * 60;
    }

    private void impostaDurataSessioneCorrente() {
        secondiRimanenti = getDurataSessioneCorrenteSecondi();
        millisecondiRimanenti = secondiRimanenti * 1000L;
    }

    private void aggiornaTimerEProgressBar() {
        aggiornaLabelTimer();
        int max = Math.max(1, getDurataSessioneCorrenteSecondi());
        int valoreCorrente = (int) Math.max(0, Math.min(millisecondiRimanenti / 1000.0, max));
        barraProgressi.setMaximum(max);
        barraProgressi.setValue(valoreCorrente);
        barraProgressi.setForeground(isSessioneStudio ? new Color(231, 76, 60) : new Color(52, 152, 219));
        barraProgressi.repaint();
    }

    public void cambiaTipoSessione(boolean isStudio) {
        if(inEsecuzione) {
            pausaTimer();
        }
        isSessioneStudio = isStudio;
        impostaDurataSessioneCorrente();
        lblStato.setText(isSessioneStudio ? "Pronto per studiare?" : "Pronto per la pausa");
        lblStato.setForeground(Color.GRAY);
        lblStato.setIcon(new FlatSVGIcon(isSessioneStudio ? "icone/books.svg" : "icone/coffee.svg", 20, 20));
        btnStartPause.setText("Avvia");
        aggiornaTimerEProgressBar();
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
        // Notifica di sistema usando il tuo GestoreNotifiche
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
        radioStudio.setSelected(isSessioneStudio);
        radioPausa.setSelected(!isSessioneStudio);
        impostaDurataSessioneCorrente();
        lblStato.setIcon(new FlatSVGIcon(isSessioneStudio ? "icone/books.svg" : "icone/coffee.svg", 20, 20));
        lblStato.setText(isSessioneStudio ? "Pronto per studiare?" : "Pronto per la pausa");
        lblStato.setForeground(Color.GRAY);
        aggiornaTimerEProgressBar();
        btnStartPause.setText("Avvia");
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

    public void setTrophyButton() {
        trophyBut = new JButton("");
        trophyBut.setBorderPainted(false);
        trophyBut.setFocusPainted(false);
        trophyBut.setContentAreaFilled(false);
        trophyBut.setOpaque(false);
        trophyBut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        trophyBut.setIcon(new FlatSVGIcon("icone/trophy.svg", 35, 35));
        trophyBut.addActionListener(e -> {
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

            JPanel pannelloTrofei = new JPanel();
            pannelloTrofei.setPreferredSize(new Dimension(550, 450));
            pannelloTrofei.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            pannelloTrofei.setLayout(new BorderLayout());

            JLabel titolo = new JLabel("I Tuoi Obiettivi", SwingConstants.CENTER);
            titolo.setFont(new Font("Arial", Font.BOLD, 22));
            titolo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            pannelloTrofei.add(titolo, BorderLayout.NORTH);

            // --- CONTENUTO CENTRALE ---
            JPanel centro = new JPanel();
            centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
            centro.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            // --- CALCOLO STATISTICHE REALI ---
            int totaleMinuti = 0;
            for (String riga : GestoreDati.getTuttoLoStudioRaw()) {
                if (riga != null && riga.contains(";")) {
                    try {
                        totaleMinuti += Integer.parseInt(riga.split(";")[1]);
                    } catch (Exception ex) {
                    }
                }
            }
            int oreTotali = totaleMinuti / 60;
            int totaleLodi = 0;
            for (String riga : GestoreDati.getVotiEsamiRaw()) {
                if (riga != null && (riga.startsWith("30L") || riga.toLowerCase().startsWith("30 e lode"))) {
                    totaleLodi++;
                }
            }
            int numeroDiciotto = 0;
            for(String riga : GestoreDati.getVotiEsamiRaw()) {
                if(riga != null && (riga.startsWith("18"))) {
                    numeroDiciotto++;
                }
            }
            int cfuTotali = 0;
            for (String riga : GestoreDati.getVotiEsamiRaw()) {
                String[] parti = riga.split(";");
                if(parti.length == 3) {
                    cfuTotali += Integer.parseInt(parti[2]);
                }
            }
            int cfuMaxImpostati = Integer.parseInt(GestoreDati.getImpostazione("CFU", "180"));

            // --- LISTA DEGLI ACHIEVEMENT ---
            JPanel listaObiettivi = new JPanel();
            listaObiettivi.setLayout(new BoxLayout(listaObiettivi, BoxLayout.Y_AXIS));
            listaObiettivi.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            // Obiettivo 1: Studioso (10 ore totali)
            listaObiettivi.add(creaPannelloObiettivo("Studioso", "Studia per 10 ore totali", oreTotali >= 10));
            listaObiettivi.add(Box.createRigidArea(new Dimension(0, 15)));
            // Obiettivo 2: Secchione (3 Lodi)
            listaObiettivi.add(creaPannelloObiettivo("Secchione", "Ottieni 3 Lodi", totaleLodi >= 3));
            listaObiettivi.add(Box.createRigidArea(new Dimension(0, 15)));
            // Obiettivo 3: Maratoneta (5 Pomodori oggi)
            listaObiettivi.add(creaPannelloObiettivo("Maratoneta", "Fai 5 Pomodori in un giorno", conteggioPomodori >= 5));
            listaObiettivi.add(Box.createRigidArea(new Dimension(0, 15)));
            // Obiettivo 4: Horto Muso (5 volte 18)
            listaObiettivi.add(creaPannelloObiettivo("Intenditore di Hippica", "Prendi almeno tre 18", numeroDiciotto >= 3));
            listaObiettivi.add(Box.createRigidArea(new Dimension(0, 15)));
            // Obiettivo: The End?
            listaObiettivi.add(creaPannelloObiettivo("The End?", "Raggiungi il numero di crediti massimi", cfuTotali >= cfuMaxImpostati));
            
            JScrollPane scrollPane = new JScrollPane(listaObiettivi);
            scrollPane.setBorder(null); // Togliamo il bordo di default dello scrollpane
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false); // Rende trasparente anche l'area interna
            scrollPane.getVerticalScrollBar().setUnitIncrement(12);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            centro.add(scrollPane, BorderLayout.CENTER);

            // ASSEMBLIAMO I PEZZI NELL'ORDINE GIUSTO NEL CENTRO
            pannelloTrofei.add(centro, BorderLayout.CENTER);

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
            pannelloTrofei.add(panelBottone, BorderLayout.SOUTH);

            shadowOverlay.add(pannelloTrofei, new GridBagConstraints());
            frame.setGlassPane(shadowOverlay);
            shadowOverlay.setVisible(true);
        });

        trophyButtonPanel = new JPanel(new GridBagLayout());
        trophyButtonPanel.setOpaque(false);
        GridBagConstraints gbcOption = new GridBagConstraints();
        gbcOption.gridx = 0;
        gbcOption.gridy = 0;
        gbcOption.anchor = GridBagConstraints.NORTHEAST;
        gbcOption.fill = GridBagConstraints.NONE;
        gbcOption.weightx = 1;
        gbcOption.weighty = 1;
        trophyButtonPanel.add(trophyBut, gbcOption);
        applyOptionButtonAppearance(false);
    }

    private JPanel creaPannelloObiettivo(String nome, String descrizione, boolean sbloccato) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createLineBorder(sbloccato ? new Color(46, 204, 113) : Color.LIGHT_GRAY, 2, true));
        pnl.setBackground(sbloccato ? new Color(230, 255, 230) : null); // Sfondo verdino se sbloccato
        pnl.setMaximumSize(new Dimension(300, 60));
        pnl.setPreferredSize(new Dimension(300, 60));
        // Icona: Lucchetto o Medaglia
        JLabel icona = new JLabel();
        icona.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        icona.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        if(sbloccato)
            icona.setIcon(new FlatSVGIcon("icone/medal.svg", 25, 25));
        else
            icona.setIcon(new FlatSVGIcon("icone/locker.svg", 25, 25));
        // Testi
        JPanel pnlTesti = new JPanel(new GridLayout(2, 1));
        pnlTesti.setOpaque(false);
        JLabel lblNome = new JLabel(nome);
        lblNome.setFont(new Font("Arial", Font.BOLD, 14));
        lblNome.setForeground(sbloccato ? new Color(39, 174, 96) : Color.GRAY);
        JLabel lblDesc = new JLabel(descrizione);
        lblDesc.setFont(new Font("Arial", Font.ITALIC, 11));
        lblDesc.setForeground(Color.DARK_GRAY);
        pnlTesti.add(lblNome);
        pnlTesti.add(lblDesc);

        pnl.add(icona, BorderLayout.WEST);
        pnl.add(pnlTesti, BorderLayout.CENTER);
        return pnl;
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
                    if (inEsecuzione) {
                        pausaTimer();
                    }
                    impostaDurataSessioneCorrente();
                    btnStartPause.setText("Avvia");
                    lblStato.setText(isSessioneStudio ? "Pronto per studiare?" : "Pronto per la pausa");
                    lblStato.setForeground(Color.GRAY);
                    lblStato.setIcon(new FlatSVGIcon(isSessioneStudio ? "icone/books.svg" : "icone/coffee.svg", 20, 20));
                    aggiornaTimerEProgressBar();
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
            centro.add(pnlGruppoParametri);
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
        if (trophyButtonPanel != null && trophyBut != null) {
            int marginLeft = Math.max(6, Math.round(10 * currentScale));
            trophyButtonPanel.setBorder(new EmptyBorder(marginTop, marginLeft, 0, 0));
            trophyBut.setPreferredSize(buttonDim);
            trophyBut.setMinimumSize(buttonDim);
            trophyBut.setMaximumSize(buttonDim);
            trophyButtonPanel.setPreferredSize(new Dimension(buttonSize + marginLeft, buttonSize + marginTop));
        }
        applyOptionButtonAppearance(optionBut.getModel().isRollover());
    }

    private void applyResponsiveProgressBarSize() {
        if (barraProgressi == null || getWidth() <= 0) {
            return;
        }

        int targetWidth = (int) (getWidth() * 0.45f);
        targetWidth = Math.max(220, Math.min(400, targetWidth));
        barraProgressi.setPreferredSize(new Dimension(targetWidth, 14));
        barraProgressi.revalidate();
        barraProgressi.repaint();
    }

    private void applyResponsiveFonts() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        float scaleX = (float) getWidth() / BASE_WIDTH;
        float scaleY = (float) getHeight() / BASE_HEIGHT;
        float currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.min(scaleX, scaleY)));

        int titleSize = Math.round(28 * currentScale);
        int statoSize = Math.round(18 * currentScale);
        int radioSize = Math.round(14 * currentScale);
        int selezioneSize = Math.round(14 * currentScale);
        int timerSize = Math.round(100 * currentScale);
        int contatorSize = Math.round(16 * currentScale);

        if (lblTitle != null) {
            lblTitle.setFont(new Font("Arial", Font.BOLD, titleSize));
        }
        if (lblStato != null) {
            lblStato.setFont(new Font("Arial", Font.ITALIC, statoSize));
        }
        if (radioStudio != null) {
            radioStudio.setFont(new Font("Arial", Font.BOLD, radioSize));
        }
        if (radioPausa != null) {
            radioPausa.setFont(new Font("Arial", Font.BOLD, radioSize));
        }
        if (lblSelezione != null) {
            lblSelezione.setFont(new Font("Arial", Font.PLAIN, selezioneSize));
        }
        if (lblTimer != null) {
            lblTimer.setFont(new Font("Monospaced", Font.BOLD, timerSize));
        }
        if (lblContatore != null) {
            lblContatore.setFont(new Font("Arial", Font.BOLD, contatorSize));
        }
        if (lblMaxPomodori != null) {
            lblMaxPomodori.setFont(new Font("Arial", Font.BOLD, contatorSize));
        }
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
        if (!inEsecuzione) {
            impostaDurataSessioneCorrente();
            aggiornaTimerEProgressBar();
        }
        this.revalidate();
        this.repaint();
    }
}

package com.minec;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.AlphaComposite;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JTextArea;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.minec.dati.GestoreDatabase;

public class GestoreNotifiche {

    private static final String PREFISSO_TROFEO_NOTIFICATO = "ACH_NOTIFIED_";
        private static final Set<String> TROFEI_FISSI = Set.of(
            "ACH_SPEEDRUNNER",
            "ACH_TOCCA_ERBA_SESSION",
            "ACH_CREATURA_NOTTE",
            "ACH_PROCRASTINAZIONE_SERIALE");
    private static final int LARGHEZZA_NOTIFICA_TROFEO = 350;
    private static final int ALTEZZA_NOTIFICA_TROFEO = 150;
    private static final int MARGINE_NOTIFICA_TROFEO = 16;
    private static final int DURATA_VISIBILE_MS = 2200;
    private static final int RITARDO_FADE_MS = 30;
    private static final float STEP_OPACITA = 0.08f;
    private static TrayIcon trayIconGlobale;
    private static final Object lockTrofei = new Object();
    private static final Map<String, Boolean> statoTrofeiSessione = new HashMap<>();
    private static boolean trofeiInizializzati = false;

    private record TrofeoObiettivo(String id, String nome, String descrizione, boolean sbloccato) {
    }

    public static void avviaNotifiche(String percorsoIcona) {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray non supportato su questo pc");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = new FlatSVGIcon(percorsoIcona, 16, 16).getImage();
        TrayIcon trayIcon = new TrayIcon(image, "UniPlanner In Background");
        trayIcon.setImageAutoSize(true);

        PopupMenu popup = new PopupMenu();
        MenuItem esciItem = new MenuItem("Chiudi UniPlanner");
        esciItem.addActionListener(e -> System.exit(0));
        popup.add(esciItem);
        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
            trayIconGlobale = trayIcon;
            avviaTimerControlloScadenze();
        } catch (AWTException e) {
            System.out.println("Errore: impossibile aggiungere l'icona alla barra");
        }
    }

    public static void avviaTimerControlloScadenze() {
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                controllaScadenze();
            }
        }, 1000 * 5, 1000 * 60 * 60 * 24);
    }

    public static void controllaScadenze() {
        if (trayIconGlobale == null) {
            return;
        }

        String[] scadenzeRaw = GestoreDatabase.getScadenzeRaw();
        LocalDate oggi = LocalDate.now();
        boolean trovataNotifica = false;
        StringBuilder messaggio = new StringBuilder();

        for (String riga : scadenzeRaw) {
            if (riga == null || !riga.contains(";")) {
                continue;
            }

            String[] parti = riga.split(";");
            if (parti.length < 2) {
                continue;
            }

            try {
                String nomeEsame = parti[0].trim();
                LocalDate dataEsame = LocalDate.parse(parti[1].trim());
                long giorniMancanti = ChronoUnit.DAYS.between(oggi, dataEsame);
                if (giorniMancanti == 7 || giorniMancanti == 3 || giorniMancanti == 1 || giorniMancanti == 0) {
                    trovataNotifica = true;
                    if (giorniMancanti == 0) {
                        messaggio.append("Oggi hai l'esame di ").append(nomeEsame).append("!\n");
                    } else {
                        messaggio.append(nomeEsame).append(" tra ").append(giorniMancanti).append(" giorni!\n");
                    }
                }
            } catch (Exception e) {
            }
        }

        if (trovataNotifica) {
            trayIconGlobale.displayMessage("Promemoria Esami!", messaggio.toString(), TrayIcon.MessageType.WARNING);
        }
    }

    public static void mostraNotifica(String titolo, String descrizione, Component frame) {
        if (trayIconGlobale != null) {
            trayIconGlobale.displayMessage(titolo, descrizione, TrayIcon.MessageType.INFO);
            return;
        }

        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, descrizione, titolo,
                JOptionPane.INFORMATION_MESSAGE));
    }

    public static void mostraNotificaTrofeoInterna(String titolo, String descrizione, Component frame) {
        SwingUtilities.invokeLater(() -> {
            JRootPane rootPane = frame != null ? SwingUtilities.getRootPane(frame) : null;
            if (rootPane == null) {
                JOptionPane.showMessageDialog(frame, descrizione, titolo, JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JLayeredPane layeredPane = rootPane.getLayeredPane();
            PannelloNotificaTrofeo notifica = creaPannelloNotificaTrofeo(titolo, descrizione);
            notifica.setSize(LARGHEZZA_NOTIFICA_TROFEO, ALTEZZA_NOTIFICA_TROFEO);

                int xCentrato = (layeredPane.getWidth() - LARGHEZZA_NOTIFICA_TROFEO) / 2;
                int xMassimo = Math.max(MARGINE_NOTIFICA_TROFEO,
                    layeredPane.getWidth() - LARGHEZZA_NOTIFICA_TROFEO - MARGINE_NOTIFICA_TROFEO);
                int x = Math.max(MARGINE_NOTIFICA_TROFEO, Math.min(xCentrato, xMassimo));
                int y = MARGINE_NOTIFICA_TROFEO;

            notifica.setBounds(x, y, LARGHEZZA_NOTIFICA_TROFEO, ALTEZZA_NOTIFICA_TROFEO);
            notifica.setAlpha(0f);

            layeredPane.add(notifica, JLayeredPane.POPUP_LAYER);
            layeredPane.revalidate();
            layeredPane.repaint();

            avviaAnimazioneDissolvenza(notifica, layeredPane);
        });
    }

    private static PannelloNotificaTrofeo creaPannelloNotificaTrofeo(String titolo, String descrizione) {
        PannelloNotificaTrofeo pannello = new PannelloNotificaTrofeo();
        pannello.setLayout(new BorderLayout(0, 8));
        pannello.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        pannello.setBackground(new Color(244, 248, 255));
        pannello.setOpaque(false);

        JLabel lblTitolo = new JLabel(titolo);
        lblTitolo.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTitolo.setForeground(new Color(20, 45, 87));

        JTextArea txtDescrizione = new JTextArea(descrizione == null ? "" : descrizione);
        txtDescrizione.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtDescrizione.setForeground(new Color(34, 47, 62));
        txtDescrizione.setLineWrap(true);
        txtDescrizione.setWrapStyleWord(true);
        txtDescrizione.setEditable(false);
        txtDescrizione.setFocusable(false);
        txtDescrizione.setOpaque(false);
        txtDescrizione.setBorder(null);

        pannello.add(lblTitolo, BorderLayout.NORTH);
        pannello.add(txtDescrizione, BorderLayout.CENTER);
        return pannello;
    }

    private static void avviaAnimazioneDissolvenza(PannelloNotificaTrofeo notifica, JLayeredPane layeredPane) {
        final float[] opacita = { 0f };
        final int[] fase = { 0 };
        final long[] inizioPausa = { 0L };

        Timer timer = new Timer(RITARDO_FADE_MS, null);
        timer.addActionListener(e -> {
            if (notifica.getParent() == null) {
                ((Timer) e.getSource()).stop();
                return;
            }

            if (fase[0] == 0) {
                opacita[0] = Math.min(1f, opacita[0] + STEP_OPACITA);
                notifica.setAlpha(opacita[0]);
                if (opacita[0] >= 1f) {
                    fase[0] = 1;
                    inizioPausa[0] = System.currentTimeMillis();
                }
                return;
            }

            if (fase[0] == 1) {
                if (System.currentTimeMillis() - inizioPausa[0] >= DURATA_VISIBILE_MS) {
                    fase[0] = 2;
                }
                return;
            }

            opacita[0] = Math.max(0f, opacita[0] - STEP_OPACITA);
            notifica.setAlpha(opacita[0]);
            if (opacita[0] <= 0f) {
                ((Timer) e.getSource()).stop();
                layeredPane.remove(notifica);
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });
        timer.start();
    }

    private static class PannelloNotificaTrofeo extends JPanel {
        private static final int ARCO_BORDO = 24;
        private static final Color COLORE_BORDO = new Color(39, 93, 173);
        private float alpha = 1f;

        private void setAlpha(float alpha) {
            this.alpha = Math.max(0f, Math.min(1f, alpha));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARCO_BORDO, ARCO_BORDO);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLORE_BORDO);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARCO_BORDO, ARCO_BORDO);
            g2.dispose();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paint(g2);
            g2.dispose();
        }
    }

    public static void aggiornaTrofeiEAvvisa(Component frame) {
        List<TrofeoObiettivo> trofei = calcolaTrofei();
        List<TrofeoObiettivo> daNotificare = new ArrayList<>();

        synchronized (lockTrofei) {
            if (!trofeiInizializzati) {
                for (TrofeoObiettivo trofeo : trofei) {
                    statoTrofeiSessione.put(trofeo.id(), trofeo.sbloccato());
                    if (trofeo.sbloccato() && isTrofeoFisso(trofeo.id())) {
                        GestoreDatabase.salvaImpostazione(PREFISSO_TROFEO_NOTIFICATO + trofeo.id(), "true");
                    }
                }
                trofeiInizializzati = true;
                return;
            }

            for (TrofeoObiettivo trofeo : trofei) {
                Boolean statoPrecedente = statoTrofeiSessione.put(trofeo.id(), trofeo.sbloccato());

                if (!trofeo.sbloccato()) {
                    continue;
                }

                if (isTrofeoFisso(trofeo.id())) {
                    String chiaveNotifica = PREFISSO_TROFEO_NOTIFICATO + trofeo.id();
                    if (Boolean.parseBoolean(GestoreDatabase.getImpostazione(chiaveNotifica, "false"))) {
                        continue;
                    }

                    boolean eraGiaSbloccato = statoPrecedente != null && statoPrecedente;
                    if (eraGiaSbloccato) {
                        continue;
                    }

                    GestoreDatabase.salvaImpostazione(chiaveNotifica, "true");
                    daNotificare.add(trofeo);
                    continue;
                }

                boolean eraGiaSbloccato = statoPrecedente != null && statoPrecedente;
                if (eraGiaSbloccato) {
                    continue;
                }
                daNotificare.add(trofeo);
            }
        }

        for (TrofeoObiettivo trofeo : daNotificare) {
            mostraNotificaTrofeoInterna("Trofeo sbloccato: " + trofeo.nome(), trofeo.descrizione(), frame);
        }
    }

    private static boolean isTrofeoFisso(String idTrofeo) {
        return TROFEI_FISSI.contains(idTrofeo);
    }

    private static List<TrofeoObiettivo> calcolaTrofei() {
        int totaleMinuti = 0;
        for (String riga : GestoreDatabase.getTuttoLoStudioRaw()) {
            if (riga == null || !riga.contains(";")) {
                continue;
            }
            try {
                totaleMinuti += Integer.parseInt(riga.split(";")[1].trim());
            } catch (Exception ex) {
            }
        }
        int oreTotali = totaleMinuti / 60;

        String[] esamiRaw = GestoreDatabase.getVotiEsamiRaw();
        int totaleLodi = 0;
        int numeroDiciotto = 0;
        int cfuTotali = 0;
        boolean[] votiDal18Al30 = new boolean[13];
        boolean hasLode = false;
        boolean has18 = false;

        for (String riga : esamiRaw) {
            if (riga == null || riga.isBlank()) {
                continue;
            }

            String rigaPulita = riga.trim();
            if (rigaPulita.startsWith("30L") || rigaPulita.toLowerCase().startsWith("30 e lode")) {
                totaleLodi++;
            }
            if (rigaPulita.startsWith("18")) {
                numeroDiciotto++;
            }

            String[] parti = rigaPulita.split(";");
            if (parti.length >= 3) {
                try {
                    cfuTotali += Integer.parseInt(parti[2].trim());
                } catch (Exception ex) {
                }
            }

            String votoRaw = parti[0].trim();
            if (votoRaw.equalsIgnoreCase("30L") || votoRaw.equalsIgnoreCase("30 e lode")) {
                hasLode = true;
                votiDal18Al30[12] = true;
                continue;
            }

            try {
                int voto = Integer.parseInt(votoRaw);
                if (voto >= 18 && voto <= 30) {
                    if (voto == 18) {
                        has18 = true;
                    }
                    votiDal18Al30[voto - 18] = true;
                }
            } catch (NumberFormatException ex) {
            }
        }

        boolean haTuttiIVotiDal18Al30 = true;
        for (boolean votoPresente : votiDal18Al30) {
            if (!votoPresente) {
                haTuttiIVotiDal18Al30 = false;
                break;
            }
        }

        boolean speedrunnerSbloccato = false;
        List<String> esamiSuperati = new ArrayList<>();
        for (String riga : GestoreDatabase.getEsamiSalvatiRaw()) {
            if (riga == null || !riga.contains(";")) {
                continue;
            }
            String[] parti = riga.split(";");
            if (parti.length > 1 && parti[1].equalsIgnoreCase("true")) {
                esamiSuperati.add(parti[0].trim());
            }
        }

        List<LocalDate> dateEsamiPassati = new ArrayList<>();
        for (String riga : GestoreDatabase.getScadenzeRaw()) {
            if (riga == null || !riga.contains(";")) {
                continue;
            }
            String[] parti = riga.split(";");
            if (parti.length <= 1) {
                continue;
            }

            String nomeEsame = parti[0].trim();
            if (!esamiSuperati.contains(nomeEsame)) {
                continue;
            }

            try {
                dateEsamiPassati.add(LocalDate.parse(parti[1].trim()));
            } catch (Exception ex) {
            }
        }
        Collections.sort(dateEsamiPassati);
        for (int i = 0; i < dateEsamiPassati.size() - 1; i++) {
            long giorni = ChronoUnit.DAYS.between(dateEsamiPassati.get(i), dateEsamiPassati.get(i + 1));
            if (giorni >= 0 && giorni <= 3) {
                speedrunnerSbloccato = true;
                break;
            }
        }

        int cfuMaxImpostati = Integer.parseInt(GestoreDatabase.getImpostazione("CFU", "180"));

        Map<String, TrofeoObiettivo> trofei = new LinkedHashMap<>();
        trofei.put("ACH_STUDIOSO_SESSION",
                new TrofeoObiettivo("ACH_STUDIOSO_SESSION", "Studioso", "Studia per 10 ore totali",
                        oreTotali >= 10));
        trofei.put("ACH_SECCHIONE_SESSION",
                new TrofeoObiettivo("ACH_SECCHIONE_SESSION", "Secchione", "Ottieni 3 Lodi",
                        totaleLodi >= 3));
        trofei.put("ACH_MARATONETA_SESSION",
                new TrofeoObiettivo("ACH_MARATONETA_SESSION", "Maratoneta", "Fai 5 Pomodori in un giorno",
                        GestoreDatabase.getPomodori() >= 5));
        trofei.put("ACH_INTENDITORE_IPPIHA_SESSION",
                new TrofeoObiettivo("ACH_INTENDITORE_IPPIHA_SESSION", "Intenditore di Ippiha",
                        "Prendi almeno tre 18", numeroDiciotto >= 3));
        trofei.put("ACH_HOW_DID_SESSION",
                new TrofeoObiettivo("ACH_HOW_DID_SESSION", "How Did We Get Here",
                        "Prendi tutti i voti da 18 a 30L", haTuttiIVotiDal18Al30));
        trofei.put("ACH_THE_END_SESSION",
                new TrofeoObiettivo("ACH_THE_END_SESSION", "The End?",
                        "Raggiungi il numero di crediti massimi", cfuTotali >= cfuMaxImpostati));
        trofei.put("ACH_TOCCA_ERBA_SESSION",
                new TrofeoObiettivo("ACH_TOCCA_ERBA_SESSION", "Tocca l'Erba",
                        "Hai completato 10 Pomodori in un solo giorno. Esci fuori, il sole esiste ancora!",
                        GestoreDatabase.getPomodori() >= 10));
        trofei.put("ACH_SPEEDRUNNER",
            new TrofeoObiettivo("ACH_SPEEDRUNNER", "SpeedRunner",
                        "Hai superato due esami a meno di 3 giorni di distanza l'uno dall'altro. Pura follia",
                        speedrunnerSbloccato));
        trofei.put("ACH_18_30_SESSION",
                new TrofeoObiettivo("ACH_18_30_SESSION", "Il Diciotto e il nuovo Trenta",
                        "Hai un 30 e Lode e un 18 nello stesso libretto. L'equilibrio perfetto dell'universo",
                        has18 && hasLode));
        trofei.put("ACH_PARKOUR_SESSION",
                new TrofeoObiettivo("ACH_PARKOUR_SESSION", "Parkour!",
                        "Hai accettato il tuo primo 18. Basta che respiri, l'importante e portarlo a casa", has18));
        trofei.put("ACH_FORMA_SESSION",
                new TrofeoObiettivo("ACH_FORMA_SESSION", "Forma Ergonomica",
                        "Hai superato le 100 ore totali di studio. La tua sedia ha ormai preso la forma della tua schiena",
                        oreTotali >= 100));

        return new ArrayList<>(trofei.values());
    }
}
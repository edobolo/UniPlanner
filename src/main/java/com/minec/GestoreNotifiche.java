package com.minec;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.minec.dati.GestoreDati;

public class GestoreNotifiche {

    private static final String PREFISSO_TROFEO_NOTIFICATO = "ACH_NOTIFIED_";
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

        String[] scadenzeRaw = GestoreDati.getScadenzeRaw();
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

    public static void aggiornaTrofeiEAvvisa(Component frame) {
        List<TrofeoObiettivo> trofei = calcolaTrofei();
        List<TrofeoObiettivo> daNotificare = new ArrayList<>();

        synchronized (lockTrofei) {
            if (!trofeiInizializzati) {
                for (TrofeoObiettivo trofeo : trofei) {
                    statoTrofeiSessione.put(trofeo.id(), trofeo.sbloccato());
                    if (trofeo.sbloccato()) {
                        GestoreDati.salvaImpostazione(PREFISSO_TROFEO_NOTIFICATO + trofeo.id(), "true");
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

                String chiaveNotifica = PREFISSO_TROFEO_NOTIFICATO + trofeo.id();
                if (Boolean.parseBoolean(GestoreDati.getImpostazione(chiaveNotifica, "false"))) {
                    continue;
                }

                boolean eraGiaSbloccato = statoPrecedente != null && statoPrecedente;
                if (eraGiaSbloccato) {
                    continue;
                }

                GestoreDati.salvaImpostazione(chiaveNotifica, "true");
                daNotificare.add(trofeo);
            }
        }

        for (TrofeoObiettivo trofeo : daNotificare) {
            mostraNotifica("Trofeo sbloccato: " + trofeo.nome(), trofeo.descrizione(), frame);
        }
    }

    private static List<TrofeoObiettivo> calcolaTrofei() {
        int totaleMinuti = 0;
        for (String riga : GestoreDati.getTuttoLoStudioRaw()) {
            if (riga == null || !riga.contains(";")) {
                continue;
            }
            try {
                totaleMinuti += Integer.parseInt(riga.split(";")[1].trim());
            } catch (Exception ex) {
            }
        }
        int oreTotali = totaleMinuti / 60;

        String[] esamiRaw = GestoreDati.getVotiEsamiRaw();
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
        for (String riga : GestoreDati.getEsamiSalvatiRaw()) {
            if (riga == null || !riga.contains(";")) {
                continue;
            }
            String[] parti = riga.split(";");
            if (parti.length > 1 && parti[1].equalsIgnoreCase("true")) {
                esamiSuperati.add(parti[0].trim());
            }
        }

        List<LocalDate> dateEsamiPassati = new ArrayList<>();
        for (String riga : GestoreDati.getScadenzeRaw()) {
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

        int cfuMaxImpostati = Integer.parseInt(GestoreDati.getImpostazione("CFU", "180"));

        Map<String, TrofeoObiettivo> trofei = new LinkedHashMap<>();
        trofei.put("ACH_STUDIOSO_SESSION",
                new TrofeoObiettivo("ACH_STUDIOSO_SESSION", "Studioso", "Studia per 10 ore totali",
                        oreTotali >= 10));
        trofei.put("ACH_SECCHIONE_SESSION",
                new TrofeoObiettivo("ACH_SECCHIONE_SESSION", "Secchione", "Ottieni 3 Lodi",
                        totaleLodi >= 3));
        trofei.put("ACH_MARATONETA_SESSION",
                new TrofeoObiettivo("ACH_MARATONETA_SESSION", "Maratoneta", "Fai 5 Pomodori in un giorno",
                        GestoreDati.getPomodori() >= 5));
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
                        GestoreDati.getPomodori() >= 10));
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
package com.minec;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.minec.dati.GestoreDati;

public class GestoreNotifiche {

    private static TrayIcon trayIconGlobale;

    public static void avviaNotifiche(String percorsoIcona) {
        if(!SystemTray.isSupported()) {
            System.out.println("SystemTray non supportato su questo pc");
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();
        Image image = new FlatSVGIcon(percorsoIcona, 16, 16).getImage();
        TrayIcon trayIcon = new TrayIcon(image, "UniPlanner In Background");
        trayIcon.setImageAutoSize(true);
        //menu quando clicco tasto destro
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
            System.out.println("Erroe: impossibile aggiungere l'icona alla barra");
        }
    }

    public static void avviaTimerControlloScadenze() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                controllaScadenze();
            }
        }, 1000 * 5, 1000 * 60 * 60 * 24);
         //aspetta 5 secondi per fare il primo controllo, poi ogni 24 ore
    }

    public static void controllaScadenze() {
        if(trayIconGlobale == null) return;

        String[] scadenzeRaw = GestoreDati.getScadenzeRaw();
        LocalDate oggi = LocalDate.now();
        boolean trovataNotifica = false;
        StringBuilder messaggio = new StringBuilder();
        for(String riga: scadenzeRaw) {
            String[] parti = riga.split(";");
            if(parti.length >= 2) {
                try {
                    String nomeEsame = parti[0];
                    LocalDate dataEsame = LocalDate.parse(parti[1]);
                    long giorniMancanti = ChronoUnit.DAYS.between(oggi, dataEsame);
                    if(giorniMancanti == 7 || giorniMancanti == 3 || giorniMancanti == 1 || giorniMancanti == 0) {
                        trovataNotifica = true;
                        if(giorniMancanti == 0) {
                            messaggio.append("Oggi hai l'esame di ").append(nomeEsame).append("!\n");
                        }
                        else {
                            messaggio.append(" ").append(nomeEsame).append(" tra ").append(giorniMancanti).append(" giorni!\n");
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        if (trovataNotifica) {
            trayIconGlobale.displayMessage(
                "Promemoria Esami!", 
                messaggio.toString(), 
                TrayIcon.MessageType.WARNING
            );
        }
    }
}

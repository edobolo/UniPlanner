package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

import org.ietf.jgss.GSSException;
import org.w3c.dom.css.Rect;

import com.minec.dati.GestoreDati;

// Questa riga significa: "SchermataVoti è un tipo personalizzato di JPanel"
public class PannelloVoti extends JPanel {
    
    private ArrayList<JPanel> examsList = new ArrayList<>();
    private JPanel mediaPanel = new JPanel();
    private JPanel examLeftPanel = new JPanel();
    private JPanel votiEsamiPanel = new JPanel();
    private JPanel panelInfo = new JPanel();
    
    public PannelloVoti() {
        this.setLayout(null);
        setPanelMedia(mediaPanel);
        setExamLeft(examLeftPanel);
        setVotiEsami(votiEsamiPanel);
        setPanelInfo(panelInfo);
        setRefreshButton();

        this.add(mediaPanel);
        this.add(examLeftPanel);
        this.add(votiEsamiPanel);
        this.add(panelInfo);
    }

    public void setPanelMedia(JPanel mediaPanel) {
        mediaPanel.setBounds(40, 40, 200, 200);
        mediaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        mediaPanel.setLayout(new BorderLayout());
        String[] voti = GestoreDati.getVotiEsamiRaw();
       
        int sommaVoti = 0;
        int sommaCfu = 0;
        for(int i = 0; i < voti.length; i++) {
            String[] pair = voti[i].split(";");
            if (pair.length >= 3) {
                try {
                    sommaVoti += Integer.parseInt(pair[0]) * Integer.parseInt(pair[2]);
                    sommaCfu += Integer.parseInt(pair[2]);
                } catch (NumberFormatException e) {
                }
            }
        }
        double mediaVoti = 0;
        if(sommaCfu != 0)
            mediaVoti = Math.round(((double) sommaVoti / sommaCfu)*10.0)/10.0;
        JLabel title = new JLabel("Media ponderata");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setHorizontalAlignment(JLabel.CENTER);

        JPanel mediaF = new JPanel();
        mediaF.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 60));
        JLabel mediaLabel = new JLabel("" + mediaVoti);
        mediaLabel.setFont(new Font("Arial", Font.BOLD, 35));
        mediaLabel.setHorizontalAlignment(JLabel.CENTER);
        JLabel outOfLabel = new JLabel("/30");
        outOfLabel.setFont(new Font("Arial", Font.BOLD, 27));
        outOfLabel.setHorizontalAlignment(JLabel.CENTER);
        mediaF.add(mediaLabel);
        mediaF.add(outOfLabel);

        mediaPanel.add(title, BorderLayout.NORTH);
        mediaPanel.add(mediaF, BorderLayout.CENTER);
    }

    public void setExamLeft(JPanel examLeftPanel) {
        examLeftPanel.setBounds(40, 240, 200, 80);
        examLeftPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        int examAdded = GestoreDati.numeroEsami();
        int numVoti = GestoreDati.numeroVoti();
        for(int i = 0; i < examAdded; i++) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(10,20));
            if(i < numVoti) {
                panel.setBackground(new Color(36, 166, 6));
            }
            else {
                panel.setBackground(Color.WHITE);
            }
            panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            examLeftPanel.add(panel);
            examsList.add(panel);
        }
    }

    public void setPanelInfo(JPanel panelInfo) {
        panelInfo.setBounds(40, 320, 200, 200);
        panelInfo.setLayout(new GridLayout(3, 1));

        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel panel4 = new JPanel();

        
        Border b = BorderFactory.createLineBorder(Color.GRAY, 2,true);
        panel1.setBorder(b); panel1.setLayout(new BorderLayout());
        panel2.setBorder(b); panel2.setLayout(new BorderLayout());
        panel3.setBorder(b); panel3.setLayout(new BorderLayout());
        panel4.setBorder(b); panel4.setLayout(new BorderLayout());

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
        String[] voti = GestoreDati.getVotiEsamiRaw();
        int sommaVoti = 0;
        int sommaCfu = 0;
        for (int i = 0; i < voti.length; i++) {
            String[] pair = voti[i].split(";");
            if (pair.length >= 3) {
                try {
                    sommaVoti += Integer.parseInt(pair[0]) * Integer.parseInt(pair[2]);
                    sommaCfu += Integer.parseInt(pair[2]);
                } catch (NumberFormatException e) {
                }
            }
        }
        double mediaVoti = 0;
        double baseL = 0;
        if (sommaCfu != 0) {
            mediaVoti = Math.round(((double) sommaVoti / sommaCfu)*10.0)/10.0;
            baseL = (mediaVoti*110)/30;
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
        int obiettivo = 26;
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
        title4.setFont(f);
        title4.setHorizontalAlignment(JLabel.CENTER);
        JLabel cfuRimasti = new JLabel(sommaCfu + "/180");
        cfuRimasti.setFont(f);
        cfuRimasti.setHorizontalAlignment(JLabel.CENTER);
        JProgressBar jp = new JProgressBar(0, 180);
        jp.setValue(sommaCfu);
        jp.setBorder(BorderFactory.createEmptyBorder());
        jp.setForeground(new Color(36, 166, 6));
        jp.setBackground(Color.WHITE);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        progressPanel.add(jp, BorderLayout.CENTER);
        panel4.add(progressPanel ,BorderLayout.SOUTH);
        panel4.add(cfuRimasti, BorderLayout.CENTER);
        panel4.add(title4, BorderLayout.NORTH);

        
        panelInfo.add(panel1);
        panelInfo.add(panel2);
        panelInfo.add(panel3);
        panelInfo.add(panel4);
    }

    public void setVotiEsami(JPanel votiEsamePanel) {
        votiEsamePanel.setBounds(400, 70, 300, 400);
        votiEsamePanel.setLayout(new BorderLayout());
        
        JLabel text1 = new JLabel("Voti salvati");
        text1.setFont(new Font("Arial", Font.BOLD, 16));
        text1.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 
        text1.setHorizontalAlignment(JLabel.CENTER);
        votiEsamePanel.add(text1, BorderLayout.NORTH);
        
        String[] votiRaw = GestoreDati.getVotiEsamiRaw();
        int numVoti = GestoreDati.numeroVoti();
        
        JPanel votiOnly = new JPanel();
        votiOnly.setLayout(new BoxLayout(votiOnly, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(votiOnly);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        votiEsamePanel.add(scrollPane, BorderLayout.CENTER);

        for(int i = 0; i < numVoti; i++) {
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

    public void setRefreshButton() {
        String rotella = "\u21BB";
        JButton refreshBut = new JButton(rotella);
        refreshBut.addActionListener(e -> {
            refresh();
        });
        JPanel butPanel = new JPanel();
        butPanel.setBounds(710, 30, 50, 40);
        butPanel.add(refreshBut);
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

        this.revalidate();
        this.repaint();
    }
}

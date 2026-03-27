package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.*;
import javax.xml.crypto.dsig.spec.HMACParameterSpec;

import org.w3c.dom.css.RGBColor;

import com.minec.dati.GestoreDati;

// Questa riga significa: "SchermataVoti è un tipo personalizzato di JPanel"
public class PannelloVoti extends JPanel {
    
    private ArrayList<JPanel> examsList = new ArrayList<>();
    private JPanel mediaPanel = new JPanel();
    private JPanel examLeftPanel = new JPanel();
    private JPanel votiEsamiPanel = new JPanel();
    
    public PannelloVoti() {
        this.setLayout(null);
        setPanelMedia(mediaPanel);
        setExamLeft(examLeftPanel);
        setVotiEsami(votiEsamiPanel);
        setRefreshButton();

        this.add(mediaPanel);
        this.add(examLeftPanel);
        this.add(votiEsamiPanel);
    }

    public void setPanelMedia(JPanel mediaPanel) {
        mediaPanel.setBounds(40, 40, 200, 200);
        mediaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        mediaPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 80));
        String[] voti = GestoreDati.getVotiEsamiRaw();
        int somma = 0;
        for(int i = 0; i < voti.length; i++) {
            String[] pair = voti[i].split(";");
            somma += Integer.parseInt(pair[0]);
        }
        double mediaVoti = 0;
        if(voti.length != 0)
            mediaVoti = Math.round(((double) somma / voti.length)*10.0)/10.0;
        JLabel mediaLabel = new JLabel("" + mediaVoti);
        mediaLabel.setFont(new Font("Arial", Font.BOLD, 35));
        JLabel outOfLabel = new JLabel("/30");
        outOfLabel.setFont(new Font("Arial", Font.BOLD, 27));

        mediaPanel.add(mediaLabel);
        mediaPanel.add(outOfLabel);
    }

    public void setExamLeft(JPanel examLeftPanel) {
        examLeftPanel.setBounds(40, 240, 200, 100);
        examLeftPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        int examAdded = GestoreDati.numeroEsami();
        int numVoti = GestoreDati.numeroVoti();
        for(int i = 0; i < examAdded; i++) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(5,20));
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

    public void setVotiEsami(JPanel votiEsamePanel) {
        votiEsamePanel.setBounds(400, 70, 300, 490);
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
        
        // 1. CORREZIONE: Aggiungo lo scrollPane alla finestra UNA SOLA VOLTA (fuori dal ciclo)
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

        this.revalidate();
        this.repaint();
    }
}

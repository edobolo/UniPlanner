package com.minec.schermate;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.font.TextAttribute;
import java.util.Map;
import com.minec.dati.GestoreDati;

public class PannelloAggiungi extends JPanel {

    private int index = 0;
    private String examRemoved = "";
    private PannelloVoti pv;
    
    // Riferimenti ai componenti che devono cambiare nel tempo
    private JPanel esamiPanel;   // Il contenitore della lista grafica
    private JComboBox<String> tendina; // Il menu a tendina

    public PannelloAggiungi(PannelloVoti pv) {
        this.pv = pv;

        JPanel esamiAggiuntiPanel = new JPanel();
        JPanel aggiungiEsamePanel = new JPanel();
        
        aggiungiEsamePanel.setPreferredSize(new Dimension(800, 250));
        this.setLayout(new BorderLayout());
        // Inizializziamo prima la struttura
        setAddedExamsLayout(esamiAggiuntiPanel); 
        setAddExamLayout(aggiungiEsamePanel, esamiAggiuntiPanel); 

        this.add(aggiungiEsamePanel, BorderLayout.NORTH);  
        this.add(esamiAggiuntiPanel, BorderLayout.CENTER); 
        // Carichiamo i dati iniziali
        aggiornaTutto();
    }

    // Metodo unico per aggiornare sia la lista che il menu a tendina
    private void aggiornaTutto() {
        // 1. Aggiorna la lista grafica (il centro)
        esamiPanel.removeAll();
        String[] esami = GestoreDati.getEsamiSalvatiRaw();
        index = 0;
        for (String s : esami) {
            if (s != null) {
                disegnaEsameSuSchermo(s);
                index++;
            }
        }
        // 2. Aggiorna il JComboBox
        tendina.removeAllItems();
        for (String s : esami) {
            String[] parti = s.split(";");
            String nome = parti[0];
            if (s != null) tendina.addItem(nome);
        }
        tendina.setSelectedIndex(-1); // Deseleziona tutto
        
        // 3. Forza il ridisegno della UI
        esamiPanel.revalidate();
        esamiPanel.repaint();
    }

    private void disegnaEsameSuSchermo(String raw) {
        String[] parti = raw.split(";");
        String nome = parti[0];
        boolean isCompletato = Boolean.parseBoolean(parti[1]);

        JPanel panelSingoloEsame = new JPanel(new BorderLayout());
        Dimension dimensioneFissa = new Dimension(750, 50);
        panelSingoloEsame.setPreferredSize(dimensioneFissa);
        panelSingoloEsame.setMaximumSize(dimensioneFissa);
        panelSingoloEsame.setMinimumSize(dimensioneFissa);
        panelSingoloEsame.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JLabel nomeEsameLabel = new JLabel(nome);
        JCheckBox markAsDone = new JCheckBox();
        markAsDone.setSelected(isCompletato); // Imposta lo stato iniziale senza scatenare dialog

        Font fontOriginale = new Font("Arial", Font.PLAIN, 18);
        nomeEsameLabel.setFont(fontOriginale);

        // Funzione interna per aggiornare il font (sbarrato o normale)
        autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);

        // USARE ADD_ACTION_LISTENER (Scatta solo al click dell'utente)
        markAsDone.addActionListener(e -> {
            if (markAsDone.isSelected()) {
                String input = JOptionPane.showInputDialog(this, "Inserire il voto per " + nome);
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int voto = Integer.parseInt(input);
                        if (voto >= 18 && voto <= 31) {
                            GestoreDati.setVotiEsami(voto, nome);
                            GestoreDati.aggiornaStatoEsame(nome, true);
                            autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);
                            pv.refresh();
                        } else {
                            throw new Exception();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Voto non valido! Inserire un numero tra 18 e 31.");
                        markAsDone.setSelected(false);
                    }
                } else {
                    markAsDone.setSelected(false);
                }
            } else {
                // Se l'utente toglie la spunta
                GestoreDati.aggiornaStatoEsame(nome, false);
                GestoreDati.removeVotiEsame(nome);
                autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);
                pv.refresh();
            }
        });

        JButton buttonCFU = new JButton("Agg. CFU");

        JPanel pannelloSinistra = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pannelloSinistra.add(markAsDone);
        pannelloSinistra.add(nomeEsameLabel);

        JPanel pannelloDestra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pannelloDestra.add(buttonCFU);

        panelSingoloEsame.add(pannelloSinistra, BorderLayout.WEST);
        panelSingoloEsame.add(pannelloDestra, BorderLayout.EAST);
        esamiPanel.add(panelSingoloEsame);
        esamiPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    // Metodo di supporto per applicare lo sbarramento
    private void autoAggiornaSbarramento(JLabel label, JCheckBox check, Font originale) {
        Map attributes = originale.getAttributes();
        if (check.isSelected()) {
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        } else {
            attributes.put(TextAttribute.STRIKETHROUGH, false);
        }
        label.setFont(originale.deriveFont(attributes));
    }

    public void setAddExamLayout(JPanel aggiungiEsame, JPanel esamiAggiunti) {
        aggiungiEsame.setLayout(null);
        
        JLabel title = new JLabel("Aggiungi gli esami del tuo corso");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBounds(200, 20, 700, 40);
        aggiungiEsame.add(title);

        JLabel text1 = new JLabel("Nome Esame:");
        text1.setFont(new Font("Arial", Font.BOLD, 20));
        text1.setBounds(60, 80, 150, 40);
        aggiungiEsame.add(text1);
        
        JTextField campoNome = new JTextField();
        campoNome.setBounds(250, 80, 300, 40);
        campoNome.setFont(new Font("Arial", Font.PLAIN, 15));
        campoNome.setHorizontalAlignment(JTextField.CENTER);
        aggiungiEsame.add(campoNome);
        
        JButton btnSalva = new JButton("Salva Esame");
        btnSalva.setBounds(600, 80, 150, 40);
        aggiungiEsame.add(btnSalva);

        // Sezione RIMOZIONE (creata una volta sola)
        JLabel textRem = new JLabel("Rimuovi esame:");
        textRem.setFont(new Font("Arial", Font.BOLD, 20));
        textRem.setBounds(60, 170, 200, 40);
        aggiungiEsame.add(textRem);

        tendina = new JComboBox<>(); // Inizializzato vuoto
        tendina.setBounds(290, 170, 200, 40);
        aggiungiEsame.add(tendina);

        JButton btnRemove = new JButton("Rimuovi");
        btnRemove.setBounds(550, 170, 150, 40);
        aggiungiEsame.add(btnRemove);

        // LOGICA PULSANTI
        btnSalva.addActionListener(e -> {
            String nome = campoNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci un nome!");
                return;
            }
            if (index < 40) {
                GestoreDati.salvaEsame(nome);
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
                GestoreDati.removeNomeEsame(selezionato);
                GestoreDati.removeVotiEsame(selezionato);
                aggiornaTutto(); // Ricarica tutto dal file
                pv.refresh();
            }
        });
    }

    public void setAddedExamsLayout(JPanel esamiAggiunti) {
        esamiAggiunti.setLayout(new BorderLayout(0, 10)); 
        esamiAggiunti.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JLabel titleB = new JLabel("Aggiunti di recente");
        titleB.setFont(new Font("Arial", Font.BOLD, 17));
        JPanel container = new JPanel();
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        container.add(titleB);

        esamiAggiunti.add(container, BorderLayout.NORTH);

        esamiPanel = new JPanel();
        esamiPanel.setLayout(new BoxLayout(esamiPanel, BoxLayout.Y_AXIS)); 
        JScrollPane scrollPane = new JScrollPane(esamiPanel);
        scrollPane.setBorder(null);
        esamiAggiunti.add(scrollPane, BorderLayout.CENTER);
    }
}
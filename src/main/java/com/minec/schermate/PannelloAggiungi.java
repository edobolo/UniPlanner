package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.minec.dati.GestoreDati;

public class PannelloAggiungi extends JPanel {

    private int index = 0;
    private PannelloVoti pv;

    // Riferimenti ai componenti che devono cambiare nel tempo
    private JPanel esamiPanel; // Il contenitore della lista grafica
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
            if (s != null)
                tendina.addItem(nome);
        }
        tendina.setSelectedIndex(-1); // Deseleziona tutto

        // 3. Forza il ridisegno della UI
        esamiPanel.revalidate();
        esamiPanel.repaint();
    }

    private void disegnaEsameSuSchermo(String raw) {
        // la stringa 'raw' viene da esami.txt (es. "Analisi 1;true")
        String[] parti = raw.split(";");
        String nome = parti[0];
        boolean isCompletato = Boolean.parseBoolean(parti[1]);

        // --- NOVITÀ: Cerca i CFU nel file voti.txt ---
        int cfuSalvati = 0;
        if (isCompletato) {
            String[] votiRaw = GestoreDati.getVotiEsamiRaw();
            for (String rigaVoto : votiRaw) {
                String[] pVoto = rigaVoto.split(";");
                // Se la riga corrisponde al nostro esame e ha 3 elementi (Voto;Nome;CFU)
                if (pVoto.length >= 2 && pVoto[1].equals(nome)) {
                    if (pVoto.length > 2) {
                        try {
                            cfuSalvati = Integer.parseInt(pVoto[2]);
                        } catch (NumberFormatException e) {
                        }
                    }
                    break;
                }
            }
        }

        JPanel panelSingoloEsame = new JPanel(new BorderLayout());
        Dimension dimensioneFissa = new Dimension(750, 50);
        panelSingoloEsame.setPreferredSize(dimensioneFissa);
        panelSingoloEsame.setMaximumSize(dimensioneFissa);
        panelSingoloEsame.setMinimumSize(dimensioneFissa);
        panelSingoloEsame.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JLabel nomeEsameLabel = new JLabel(nome);
        JCheckBox markAsDone = new JCheckBox();
        markAsDone.setSelected(isCompletato);

        Font fontOriginale = new Font("Arial", Font.PLAIN, 18);
        nomeEsameLabel.setFont(fontOriginale);
        autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);

        // --- GESTIONE GRAFICA DEI CFU ---
        JButton buttonCFU = new JButton("Agg. CFU");
        JPanel pannelloCfuLocale = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        if (cfuSalvati > 0) {
            // Se ho già i CFU, mostro il testo e nascondo il bottone
            JLabel textCfu = new JLabel(cfuSalvati + " CFU");
            textCfu.setFont(new Font("Arial", Font.BOLD, 14));
            pannelloCfuLocale.add(textCfu);
            buttonCFU.setVisible(false);
        } else {
            // Se non ho i CFU, il bottone si vede SOLO se l'esame ha il voto (isCompletato)
            buttonCFU.setVisible(isCompletato);
        }

        // Azione Bottone CFU
        buttonCFU.addActionListener(e -> {
            String[] opzioni = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" };
            String scelta = (String) JOptionPane.showInputDialog(null,
                    "Seleziona il numero di CFU per " + nome + ":",
                    "Selezione CFU", JOptionPane.QUESTION_MESSAGE, null, opzioni, opzioni[0]);
            if (scelta != null) {
                int cfu = Integer.parseInt(scelta);
                GestoreDati.addCfuEsame(nome, cfu); // Scrive nel file

                // Aggiorna l'interfaccia
                JLabel textCfu = new JLabel(cfu + " CFU");
                textCfu.setFont(new Font("Arial", Font.BOLD, 14));
                pannelloCfuLocale.add(textCfu);
                buttonCFU.setVisible(false); // Nasconde il bottone

                pannelloCfuLocale.revalidate();
                pannelloCfuLocale.repaint();
                pv.refresh();
            }
        });

        // Azione Checkbox Voti
        markAsDone.addActionListener(e -> {
            if (markAsDone.isSelected()) {
                String input = JOptionPane.showInputDialog(this, "Inserire il voto per " + nome);
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int voto = Integer.parseInt(input);
                        if (voto >= 18 && voto <= 31) {
                            GestoreDati.setVotiEsami(voto, nome, 0);
                            GestoreDati.aggiornaStatoEsame(nome, true);
                            autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);

                            buttonCFU.setVisible(true); //se il voto c'è faccio comparire il bottone CFU
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
                // SE TOLGO LA SPUNTA: Rimuovo tutto
                GestoreDati.aggiornaStatoEsame(nome, false);
                GestoreDati.removeVotiEsame(nome); // Elimina riga da voti.txt (inclusi i CFU)
                autoAggiornaSbarramento(nomeEsameLabel, markAsDone, fontOriginale);
                // Resetto l'interfaccia CFU per questo esame
                pannelloCfuLocale.removeAll();
                buttonCFU.setVisible(false); // Nascondo il bottone
                pannelloCfuLocale.revalidate();
                pannelloCfuLocale.repaint();
                pv.refresh();
            }
        });
        // Assemblaggio finale delle "scatole"
        JPanel pannelloSinistra = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pannelloSinistra.add(markAsDone);
        pannelloSinistra.add(nomeEsameLabel);

        JPanel pannelloDestra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pannelloDestra.add(pannelloCfuLocale);
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
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        esamiAggiunti.add(scrollPane, BorderLayout.CENTER);
    }
}
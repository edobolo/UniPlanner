package com.minec.schermate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.minec.dati.GestoreDati;

public class PannelloScadenze extends JPanel {

    private JPanel scadenzePanel; // Contenitore della lista in basso
    private JComboBox<String> tendinaRimozione; // Menu a tendina per rimuovere

    public PannelloScadenze() {
        this.setLayout(new BorderLayout());

        JPanel aggiungiScadenzaPanel = new JPanel();
        aggiungiScadenzaPanel.setPreferredSize(new Dimension(800, 250));

        JPanel listaScadenzePanel = new JPanel();
        
        setAddScadenzaLayout(aggiungiScadenzaPanel);
        setListaScadenzeLayout(listaScadenzePanel);

        this.add(aggiungiScadenzaPanel, BorderLayout.NORTH);
        this.add(listaScadenzePanel, BorderLayout.CENTER);

        aggiornaTutto();
    }

    private void aggiornaTutto() {
        scadenzePanel.removeAll();
        tendinaRimozione.removeAllItems();

        String[] scadenzeRaw = GestoreDati.getScadenzeRaw();
        if (scadenzeRaw != null) {
            for (String riga : scadenzeRaw) {
                if (riga != null && !riga.trim().isEmpty()) {
                    disegnaScadenzaSuSchermo(riga);
                    
                    // Aggiungo anche al menu a tendina per la rimozione
                    String nome = riga.split(";")[0];
                    tendinaRimozione.addItem(nome);
                }
            }
        }
        tendinaRimozione.setSelectedIndex(-1);

        scadenzePanel.revalidate();
        scadenzePanel.repaint();
    }

    private void disegnaScadenzaSuSchermo(String riga) {
        String[] parti = riga.split(";");
        String nomeEsame = parti[0];
        String dataStringa = parti[1]; // Es. "25/06/2026"

        // --- MATEMATICA DELLE DATE ---
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataEsame = LocalDate.parse(dataStringa, formato);
        LocalDate oggi = LocalDate.now();

        // Calcoliamo la differenza in giorni
        long giorniMancanti = ChronoUnit.DAYS.between(oggi, dataEsame);

        // --- CREAZIONE GRAFICA ---
        JPanel panelSingolaScadenza = new JPanel(new BorderLayout());
        Dimension dim = new Dimension(750, 50);
        panelSingolaScadenza.setPreferredSize(dim);
        panelSingolaScadenza.setMaximumSize(dim);
        panelSingolaScadenza.setMinimumSize(dim);
        panelSingolaScadenza.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        // Testo a sinistra (Nome e Data)
        JLabel infoLabel = new JLabel("⏳ " + nomeEsame + " - " + dataStringa);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JPanel pannelloSinistra = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pannelloSinistra.add(infoLabel);

        // Testo a destra (Giorni mancanti con colori dinamici!)
        String testoGiorni = "";
        Color coloreGiorni = Color.BLACK;

        if (giorniMancanti < 0) {
            testoGiorni = "Scaduto da " + Math.abs(giorniMancanti) + " giorni";
            coloreGiorni = Color.GRAY;
        } else if (giorniMancanti == 0) {
            testoGiorni = "È OGGI!";
            coloreGiorni = Color.RED;
        } else {
            testoGiorni = "- " + giorniMancanti + " giorni";
            if (giorniMancanti <= 7) coloreGiorni = Color.RED; // Meno di una settimana: Rosso!
            else if (giorniMancanti <= 30) coloreGiorni = new Color(255, 140, 0); // Arancione
            else coloreGiorni = new Color(0, 150, 0); // Verde
        }

        JLabel giorniLabel = new JLabel(testoGiorni);
        giorniLabel.setFont(new Font("Arial", Font.BOLD, 18));
        giorniLabel.setForeground(coloreGiorni);
        
        JPanel pannelloDestra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pannelloDestra.add(giorniLabel);

        panelSingolaScadenza.add(pannelloSinistra, BorderLayout.WEST);
        panelSingolaScadenza.add(pannelloDestra, BorderLayout.EAST);

        scadenzePanel.add(panelSingolaScadenza);
        scadenzePanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    public void setAddScadenzaLayout(JPanel aggiungiScadenzaPanel) {
        aggiungiScadenzaPanel.setLayout(null);

        JLabel title = new JLabel("Aggiungi Scadenza Esame");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBounds(200, 20, 400, 40);
        aggiungiScadenzaPanel.add(title);

        // --- RIGA 1: NOME ESAME ---
        JLabel text1 = new JLabel("Nome:");
        text1.setFont(new Font("Arial", Font.BOLD, 18));
        text1.setBounds(40, 80, 80, 30);
        aggiungiScadenzaPanel.add(text1);

        JTextField campoNome = new JTextField();
        campoNome.setBounds(110, 80, 200, 30);
        aggiungiScadenzaPanel.add(campoNome);

        // --- RIGA 1: I 3 MENU A TENDINA PER LA DATA ---
        JLabel textData = new JLabel("Data:");
        textData.setFont(new Font("Arial", Font.BOLD, 18));
        textData.setBounds(330, 80, 60, 30);
        aggiungiScadenzaPanel.add(textData);

        // Genero i numeri per Giorni (01-31) e Mesi (01-12)
        String[] giorni = new String[31];
        for(int i=0; i<31; i++) giorni[i] = String.format("%02d", i+1);
        
        String[] mesi = new String[12];
        for(int i=0; i<12; i++) mesi[i] = String.format("%02d", i+1);
        
        // Genero gli anni (Anno corrente + i prossimi 5)
        int annoCorrente = LocalDate.now().getYear();
        String[] anni = new String[6];
        for(int i=0; i<6; i++) anni[i] = String.valueOf(annoCorrente + i);

        JComboBox<String> comboGiorno = new JComboBox<>(giorni);
        comboGiorno.setBounds(390, 80, 50, 30);
        aggiungiScadenzaPanel.add(comboGiorno);

        JComboBox<String> comboMese = new JComboBox<>(mesi);
        comboMese.setBounds(450, 80, 50, 30);
        aggiungiScadenzaPanel.add(comboMese);

        JComboBox<String> comboAnno = new JComboBox<>(anni);
        comboAnno.setBounds(510, 80, 70, 30);
        aggiungiScadenzaPanel.add(comboAnno);

        // --- PULSANTE SALVA ---
        JButton btnSalva = new JButton("Salva");
        btnSalva.setBounds(610, 80, 100, 30);
        aggiungiScadenzaPanel.add(btnSalva);

        // --- RIGA 2: RIMOZIONE ---
        JLabel textRem = new JLabel("Rimuovi:");
        textRem.setFont(new Font("Arial", Font.BOLD, 18));
        textRem.setBounds(40, 150, 100, 30);
        aggiungiScadenzaPanel.add(textRem);

        tendinaRimozione = new JComboBox<>();
        tendinaRimozione.setBounds(130, 150, 250, 30);
        aggiungiScadenzaPanel.add(tendinaRimozione);

        JButton btnRemove = new JButton("Elimina");
        btnRemove.setBounds(400, 150, 100, 30);
        aggiungiScadenzaPanel.add(btnRemove);


        // --- LOGICA PULSANTI ---
        btnSalva.addActionListener(e -> {
            String nome = campoNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci un nome per l'esame!");
                return;
            }

            // Costruisco la stringa della data unendo i menu a tendina
            String g = (String) comboGiorno.getSelectedItem();
            String m = (String) comboMese.getSelectedItem();
            String a = (String) comboAnno.getSelectedItem();
            String dataUnita = g + "/" + m + "/" + a;

            // Controllo Sicurezza: Verifico che la data esista (evito il 31 Febbraio!)
            try {
                DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate.parse(dataUnita, formato); // Provo a leggerla: se fallisce, va in errore
                
                // Se arrivo qui, la data è valida! Salvo nel file.
                GestoreDati.salvaScadenza(nome, dataUnita);
                campoNome.setText("");
                aggiornaTutto();

            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Attenzione: " + dataUnita + " non è una data valida sul calendario!", "Errore Data", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRemove.addActionListener(e -> {
            String selezionato = (String) tendinaRimozione.getSelectedItem();
            if (selezionato != null) {
                GestoreDati.removeScadenza(selezionato);
                aggiornaTutto();
            }
        });
    }

    public void setListaScadenzeLayout(JPanel listaScadenzePanel) {
        listaScadenzePanel.setLayout(new BorderLayout(0, 10));
        listaScadenzePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JLabel titleB = new JLabel("Scadenze Imminenti");
        titleB.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        container.add(titleB);

        listaScadenzePanel.add(container, BorderLayout.NORTH);

        scadenzePanel = new JPanel();
        scadenzePanel.setLayout(new BoxLayout(scadenzePanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(scadenzePanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll veloce
        
        listaScadenzePanel.add(scrollPane, BorderLayout.CENTER);
    }
}
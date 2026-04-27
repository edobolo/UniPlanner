package com.minec;

import java.io.IOException;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.minec.dati.GestoreDati;

public class EsportatorePDF {

    public static void generaLibretto(String percorsoSalvataggio) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            // Variabili per le statistiche
            double sommaVotiPonderata = 0;
            int totaleCFU = 0;
            double sommaVotiAritmetica = 0;
            int contaEsamiConVoto = 0;

            try (PDPageContentStream contenuto = new PDPageContentStream(documento, pagina)) {
                // --- INTESTAZIONE ---
                contenuto.beginText();
                contenuto.setFont(PDType1Font.HELVETICA_BOLD, 22);
                contenuto.newLineAtOffset(50, 750);
                contenuto.showText("Libretto Universitario - UniPlanner");
                contenuto.endText();

                int y = 700;
                contenuto.setFont(PDType1Font.HELVETICA_BOLD, 12);
                disegnaRiga(contenuto, 50, y, "Esame", "Voto", "CFU");
                y -= 20;
                
                // --- CICLO ESAMI E CALCOLO MEDIA ---
                contenuto.setFont(PDType1Font.HELVETICA, 11);
                String[] votiRaw = GestoreDati.getVotiEsamiRaw();

                for (String riga : votiRaw) {
                    if (riga == null || !riga.contains(";")) continue;
                    String[] parti = riga.split(";");
                    
                    String votoStr = parti[0];
                    String nome = parti[1];
                    int cfu = (parti.length > 2) ? Integer.parseInt(parti[2]) : 0;

                    // Calcolo logico dei voti per la media
                    if (!votoStr.equalsIgnoreCase("IDO")) {
                        int valoreVoto;
                        if (votoStr.equalsIgnoreCase("30L")) {
                            valoreVoto = GestoreDati.getPesoLode(); // es. 30 o 31
                        } else {
                            valoreVoto = Integer.parseInt(votoStr);
                        }

                        sommaVotiAritmetica += valoreVoto;
                        sommaVotiPonderata += (valoreVoto * cfu);
                        totaleCFU += cfu;
                        contaEsamiConVoto++;
                    }

                    disegnaRiga(contenuto, 50, y, nome, votoStr, String.valueOf(cfu));
                    y -= 15;
                    if (y < 100) break; // Semplice controllo fine pagina
                }

                // --- CALCOLO FINALE ---
                double mediaAritmetica = (contaEsamiConVoto > 0) ? sommaVotiAritmetica / contaEsamiConVoto : 0;
                double mediaPonderata = (totaleCFU > 0) ? sommaVotiPonderata / totaleCFU : 0;

                // --- FOOTER CON STATISTICHE ---
                y -= 40;
                contenuto.setNonStrokingColor(41 / 255f, 128 / 255f, 185 / 255f); // Blu UniPlanner
                contenuto.beginText();
                contenuto.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contenuto.newLineAtOffset(50, y);
                contenuto.showText("Riepilogo Carriera:");
                contenuto.endText();

                contenuto.setNonStrokingColor(0, 0, 0); // Torna al nero
                contenuto.setFont(PDType1Font.HELVETICA, 12);
                
                y -= 25;
                contenuto.beginText();
                contenuto.newLineAtOffset(50, y);
                contenuto.showText(String.format(Locale.US, "Media Ponderata: %.2f", mediaPonderata));
                contenuto.endText();

                y -= 15;
                contenuto.beginText();
                contenuto.newLineAtOffset(50, y);
                contenuto.showText(String.format(Locale.US, "Media Aritmetica: %.2f", mediaAritmetica));
                contenuto.endText();

                y -= 15;
                contenuto.beginText();
                contenuto.newLineAtOffset(50, y);
                contenuto.showText("Obiettivo prefissato: " + GestoreDati.getObiettivoMedia());
                contenuto.endText();
            }

            documento.save(percorsoSalvataggio);
        }
    }

    private static void disegnaRiga(PDPageContentStream stream, int x, int y, String col1, String col2, String col3) throws IOException {
        stream.beginText();
        stream.newLineAtOffset(x, y);
        stream.showText(limitString(col1, 45));
        stream.endText();

        stream.beginText();
        stream.newLineAtOffset(x + 350, y);
        stream.showText(col2);
        stream.endText();

        stream.beginText();
        stream.newLineAtOffset(x + 420, y);
        stream.showText(col3);
        stream.endText();
    }

    private static String limitString(String s, int max) {
        return (s.length() > max) ? s.substring(0, max) + "..." : s;
    }
}
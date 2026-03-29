package com.minec.dati;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class GestoreDati {
    private static final String fileEsami = "src/main/java/com/minec/dati/esami.txt";
    private static final String fileVoti = "src/main/java/com/minec/dati/voti.txt";
    private static final String fileScadenze = "src/main/java/com/minec/dati/scadenze.txt";
    private static final String fileImpostazioni = "src/main/java/com/minec/dati/impostazioni.txt";
    private static boolean salvato = false;

    //FILE ESAMI

    public static void salvaEsame(String nomeEsame) {
        String rigaFinal = nomeEsame;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileEsami, true))) {
            bw.write(rigaFinal + ";false");
            bw.newLine();
            System.out.println("Esame salvato con successo");
            salvato = true;
        } catch (IOException e) {
            salvato = false;
            System.out.println("Errore nell'apertura e scrittura del file");
        }
    }
    public static int numeroEsami() {
        int count = 0;
        try {
            FileReader fileIn = new FileReader(fileEsami);
            Scanner scan = new Scanner(fileIn);
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (!line.trim().isEmpty() || line != null) {
                    count++;
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("File non trovato");
        }
        return count;
    }
    public static void aggiornaStatoEsame(String nomeEsame, boolean completato) {
        String[] esami = getEsamiSalvatiRaw(); // Legge tutte le righe Nome;Stato
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileEsami))) {
            for (String riga : esami) {
                String[] parti = riga.split(";");
                if (parti[0].equals(nomeEsame)) {
                    bw.write(parti[0] + ";" + completato);
                } else {
                    bw.write(riga);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String[] getEsamiSalvatiRaw() {
        String[] result = new String[numeroEsami()];
        try (BufferedReader br = new BufferedReader(new FileReader(fileEsami))) {
            int i = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result[i++] = line;
                }
            }
        } catch (IOException e) {
            System.out.println("Errore nella lettura del file");
        }
        return result;
    }
    public static void removeNomeEsame(String nome) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileEsami))) {
            reWriteEsami(getEsamiSalvatiRaw(), nome);
        } catch (IOException e) {
            System.out.println("Errore nella lettura del file");
        }
    }
    public static void reWriteEsami(String[] righeRaw, String nomeDaRimuovere) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileEsami))) {
            for (String riga : righeRaw) {
                // Dividiamo la riga per prendere solo il nome
                String[] parti = riga.split(";");
                String nomeNelFile = parti[0];
                // Se il nome nel file NON è quello da rimuovere, lo riscriviamo
                if (!nomeNelFile.equals(nomeDaRimuovere)) {
                    bw.write(riga);
                    bw.newLine();
                }
            }
            System.out.println("Eliminazione completata");
        } catch (IOException e) {
            System.out.println("Errore nella riscrittura del file");
        }
    }

    //FILE VOTI

    public static void setVotiEsami(int voto, String nome, int CFU) {
        String rigaFinal = voto + ";" + nome;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileVoti, true))) {
            bw.write(rigaFinal);
            bw.newLine();
            System.out.println("Voto salvato con successo");
            salvato = true;
        } catch (IOException e) {
            salvato = false;
            System.out.println("Errore nell'apertura e scrittura del file");
        }
    }
    public static void addCfuEsame(String nome, int CFU) {
        String[] line = getVotiEsamiRaw();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileVoti))) {
            for(String riga: line) {
                String[] parti = riga.split(";");
                if (parti[1].equals(nome)) {
                    bw.write(parti[0] + ";" + parti[1] + ";" + CFU);
                } else {
                    bw.write(riga);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int numeroVoti() {
        int count = 0;
        try {
            FileReader fileIn = new FileReader(fileVoti);
            Scanner scan = new Scanner(fileIn);
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line != null || !line.trim().isEmpty()) {
                    count++;
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("File non trovato");
        }
        return count;
    }
    public static String[] getVotiEsamiRaw() {
        String[] result = new String[numeroVoti()];
        try (BufferedReader br = new BufferedReader(new FileReader(fileVoti))) {
            int i = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result[i++] = line;
                }
            }
        } catch (IOException e) {
            System.out.println("Errore nella lettura del file");
        }
        return result;
    }
    public static void removeVotiEsame(String nome) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileVoti))) {
            reWriteVoti(getVotiEsamiRaw(), nome);
        } catch (IOException e) {
            System.out.println("Errore nella lettura del file");
        }
    }
    public static void reWriteVoti(String[] righeRaw, String nomeDaRimuovere) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileVoti))) {
            for (String riga : righeRaw) {
                // Dividiamo la riga per prendere solo il nome
                String[] parti = riga.split(";");
                String nomeNelFile = parti[1];
                // Se il nome nel file NON è quello da rimuovere, lo riscriviamo
                if (!nomeNelFile.equals(nomeDaRimuovere)) {
                    bw.write(riga);
                    bw.newLine();
                }
            }
            System.out.println("Voto eliminato");
        } catch (IOException e) {
            System.out.println("Errore nella riscrittura del file");
        }
    }

    //FILE SCADENZE

    public static void salvaScadenza(String nomeEsame, String data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileScadenze, true))) {
            bw.write(nomeEsame + ";" + data);
            bw.newLine();
            System.out.println("Scadenza salvata con successo");
        } catch (IOException e) {
            System.out.println("Errore nella scrittura del file scadenze");
        }
    }
    public static int numeroScadenze() {
        int count = 0;
        try (Scanner scan = new Scanner(new FileReader(fileScadenze))) {
            while (scan.hasNextLine()) {
                if (!scan.nextLine().trim().isEmpty()) count++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File scadenze non trovato");
        }
        return count;
    }
    public static String[] getScadenzeRaw() {
        String[] result = new String[numeroScadenze()];
        try (BufferedReader br = new BufferedReader(new FileReader(fileScadenze))) {
            int i = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result[i++] = line;
                }
            }
        } catch (IOException e) {
            System.out.println("Errore lettura scadenze");
        }
        return result;
    }
    public static void removeScadenza(String nomeDaRimuovere) {
        String[] righe = getScadenzeRaw();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileScadenze))) {
            for (String riga : righe) {
                String[] parti = riga.split(";");
                if (!parti[0].equals(nomeDaRimuovere)) {
                    bw.write(riga);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Errore rimozione scadenza");
        }
    }

    //FILE IMPOSTAZIONI

    public static int getObiettivoCFU() {
        try (Scanner scan = new Scanner(new FileReader(fileImpostazioni))) {
            if (scan.hasNextLine()) {
                return Integer.parseInt(scan.nextLine().trim());
            }
        } catch (Exception e) {
        }
        return 180;
    }
    public static void salvaObiettivoCfu(int cfu) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileImpostazioni))) {
            bw.write(String.valueOf(cfu));
        } catch (IOException e) {
        }
    }
    public static void resetTutto() {
        try {
            new java.io.PrintWriter(fileEsami).close();
            new java.io.PrintWriter(fileVoti).close();
            new java.io.PrintWriter(fileScadenze).close();
        } catch (Exception e) {
            System.out.println("Errore durante il reset dei dati.");
        }
    }
    public static boolean getSalvato() {
        return salvato;
    }
}

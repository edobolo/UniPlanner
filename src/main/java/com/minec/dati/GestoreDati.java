package com.minec.dati;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class GestoreDati {
    private static final String PERCORSO_BASE = System.getProperty("user.home") + java.io.File.separator
            + "UniplannerDati";
    private static final java.io.File cartellaApp = new java.io.File(PERCORSO_BASE);

    // 2. Creiamo i file all'interno di quella cartella specifica
    private static final java.io.File fileEsami = new java.io.File(cartellaApp, "esami.txt");
    private static final java.io.File fileVoti = new java.io.File(cartellaApp, "voti.txt");
    private static final java.io.File fileScadenze = new java.io.File(cartellaApp, "scadenze.txt");
    private static final java.io.File fileImpostazioni = new java.io.File(cartellaApp, "impostazioni.txt");
    public static boolean salvato = false;

    // 3. Questo blocco "static" viene eseguito non appena l'app si avvia.
    static {
        if (!cartellaApp.exists()) {
            cartellaApp.mkdirs(); // Crea la cartella "UniplannerDati"
        }
        try {

            fileEsami.createNewFile();
            fileVoti.createNewFile();
            fileScadenze.createNewFile();
            fileImpostazioni.createNewFile();
        } catch (java.io.IOException e) {
            System.out.println("Errore critico: Impossibile creare i file di memoria.");
        }
    }

    // --- FILE ESAMI ---

    public static void salvaEsame(String nomeEsame) {
        String rigaFinal = nomeEsame;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileEsami, true))) {
            bw.write(rigaFinal + ";false");
            bw.newLine();
            salvato = true;
        } catch (IOException e) {
            salvato = false;
        }
    }

    public static int numeroEsami() {
        int count = 0;
        try {
            FileReader fileIn = new FileReader(fileEsami);
            Scanner scan = new Scanner(fileIn);
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                // CORREZIONE: Ora contiamo SOLO le righe che contengono effettivamente del
                // testo
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
        }
        return count;
    }

    public static void aggiornaStatoEsame(String nomeEsame, boolean completato) {
        String[] esami = getEsamiSalvatiRaw();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileEsami))) {
            for (String riga : esami) {
                // Aggiungiamo un controllo di sicurezza per evitare blocchi
                if (riga == null || riga.trim().isEmpty())
                    continue;

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
                if (!line.trim().isEmpty() && i < result.length) {
                    result[i++] = line;
                }
            }
        } catch (IOException e) {
        }
        return result;
    }

    public static void removeNomeEsame(String nome) {
        try {
            reWriteEsami(getEsamiSalvatiRaw(), nome);
        } catch (Exception e) {
        }
    }

    public static void reWriteEsami(String[] righeRaw, String nomeDaRimuovere) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileEsami))) {
            for (String riga : righeRaw) {
                if (riga == null)
                    continue;
                String[] parti = riga.split(";");
                String nomeNelFile = parti[0];
                if (!nomeNelFile.equals(nomeDaRimuovere)) {
                    bw.write(riga);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
        }
    }

    // --- FILE VOTI ---

    public static void setVotiEsami(String voto, String nome, int CFU) {
        String rigaFinal = voto + ";" + nome;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileVoti, true))) {
            bw.write(rigaFinal);
            bw.newLine();
            salvato = true;
        } catch (IOException e) {
            salvato = false;
        }
    }

    public static void addCfuEsame(String nome, int CFU) {
        String[] line = getVotiEsamiRaw();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileVoti))) {
            for (String riga : line) {
                if (riga == null)
                    continue;
                String[] parti = riga.split(";");
                if (parti.length > 1 && parti[1].equals(nome)) {
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
                // CORREZIONE: Anche qui evitiamo di contare le righe vuote
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
        }
        return count;
    }

    public static String[] getVotiEsamiRaw() {
        String[] result = new String[numeroVoti()];
        try (BufferedReader br = new BufferedReader(new FileReader(fileVoti))) {
            int i = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty() && i < result.length) {
                    result[i++] = line;
                }
            }
        } catch (IOException e) {
        }
        return result;
    }

    public static void removeVotiEsame(String nome) {
        try {
            reWriteVoti(getVotiEsamiRaw(), nome);
        } catch (Exception e) {
        }
    }

    public static void reWriteVoti(String[] righeRaw, String nomeDaRimuovere) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileVoti))) {
            for (String riga : righeRaw) {
                if (riga == null)
                    continue;
                String[] parti = riga.split(";");
                if (parti.length > 1) {
                    String nomeNelFile = parti[1];
                    if (!nomeNelFile.equals(nomeDaRimuovere)) {
                        bw.write(riga);
                        bw.newLine();
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    // --- FILE SCADENZE ---

    public static void salvaScadenza(String nomeEsame, String data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileScadenze, true))) {
            bw.write(nomeEsame + ";" + data);
            bw.newLine();
        } catch (IOException e) {
        }
    }

    public static int numeroScadenze() {
        int count = 0;
        try (Scanner scan = new Scanner(new FileReader(fileScadenze))) {
            while (scan.hasNextLine()) {
                // Ottimo lavoro qui! Questo era già scritto in modo perfetto.
                if (!scan.nextLine().trim().isEmpty())
                    count++;
            }
        } catch (FileNotFoundException e) {
        }
        return count;
    }

    public static String[] getScadenzeRaw() {
        String[] result = new String[numeroScadenze()];
        try (BufferedReader br = new BufferedReader(new FileReader(fileScadenze))) {
            int i = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty() && i < result.length) {
                    result[i++] = line;
                }
            }
        } catch (IOException e) {
        }
        return result;
    }

    public static void removeScadenza(String nomeDaRimuovere) {
        String[] righe = getScadenzeRaw();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileScadenze))) {
            for (String riga : righe) {
                if (riga == null)
                    continue;
                String[] parti = riga.split(";");
                if (!parti[0].equals(nomeDaRimuovere)) {
                    bw.write(riga);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
        }
    }

    // --- FILE IMPOSTAZIONI ---

    public static String getImpostazione(String chiave, String valoreDefault) {
        try (Scanner scan = new Scanner(new FileReader(fileImpostazioni))) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith(chiave + "=")) {
                    return line.split("=")[1];
                }
            }
        } catch (Exception e) {
        }
        return valoreDefault;
    }

    public static void salvaImpostazione(String chiave, String valore) {
        java.util.List<String> righe = new java.util.ArrayList<>();
        boolean trovata = false;
        try (Scanner scan = new Scanner(new FileReader(fileImpostazioni))) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith(chiave + "=")) {
                    righe.add(chiave + "=" + valore);
                    trovata = true;
                } else {
                    righe.add(line);
                }
            }
        } catch (Exception e) {
        }
        if (!trovata) {
            righe.add(chiave + "=" + valore);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileImpostazioni))) {
            for (String riga : righe) {
                bw.write(riga);
                bw.newLine();
            }
        } catch (IOException e) {
        }
    }

    // --- METODI ORIGINALI E PARAMETRI LAUREA ---

    public static int getObiettivoCFU() {
        return Integer.parseInt(getImpostazione("CFU", "180"));
    }

    public static void salvaObiettivoCfu(int cfu) {
        salvaImpostazione("CFU", String.valueOf(cfu));
    }

    public static boolean getOrdineScadenza() {
        return Boolean.parseBoolean(getImpostazione("ORDINE", "false"));
    }

    public static void salvaOrdineScadenze(boolean s) {
        salvaImpostazione("ORDINE", String.valueOf(s));
    }

    public static int getPesoLode() {
        return Integer.parseInt(getImpostazione("LODE", "30"));
    }

    public static int getBonusLode() {
        return Integer.parseInt(getImpostazione("BONUS_LODE", "0"));
    }

    public static boolean isTemaScuro() {
        return Boolean.parseBoolean(getImpostazione("TEMA_SCURO", "false"));
    }

    public static void salvaTemaScuro(boolean scuro) {
        salvaImpostazione("TEMA_SCURO", String.valueOf(scuro));
    }

    public static void resetTutto() {
        try {
            new java.io.PrintWriter(fileEsami).close();
            new java.io.PrintWriter(fileVoti).close();
            new java.io.PrintWriter(fileScadenze).close();
            new java.io.PrintWriter(fileImpostazioni).close();
        } catch (Exception e) {
        }
    }

    public static int getObiettivoMedia() {
        return Integer.parseInt(getImpostazione("OBIETTIVO_MEDIA", "25"));
    }

    public static void salvaObiettivoMedia(int obiettivo) {
        salvaImpostazione("OBIETTIVO_MEDIA", String.valueOf(obiettivo));
    }

    public static boolean getSalvato() {
        return salvato;
    }
}
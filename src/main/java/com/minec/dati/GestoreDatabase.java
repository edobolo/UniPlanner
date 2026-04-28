package com.minec.dati;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class GestoreDatabase {
    // Il database verrà salvato nella tua stessa cartella UniplannerDati!
    private static final String PERCORSO_DB = "jdbc:sqlite:" + System.getProperty("user.home")
            + java.io.File.separator + "UniplannerDati" + java.io.File.separator + "uniplanner.db";

    // Metodo per ottenere la connessione
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(PERCORSO_DB);
    }

    public static void inizializzaDatabase() {
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            // Tabella Esami (contiene anche voti e minuti studio)
            stmt.execute("CREATE TABLE IF NOT EXISTS esami ("
                    + "nome TEXT PRIMARY KEY, "
                    + "completato BOOLEAN NOT NULL DEFAULT 0, "
                    + "idoneita BOOLEAN NOT NULL DEFAULT 0, "
                    + "voto TEXT, "
                    + "cfu INTEGER DEFAULT 0, "
                    + "minuti_studio INTEGER DEFAULT 0);");

            // Tabella Scadenze
            stmt.execute("CREATE TABLE IF NOT EXISTS scadenze ("
                    + "nome_esame TEXT PRIMARY KEY, "
                    + "data TEXT);");

            // Tabella Impostazioni (chiave-valore)
            stmt.execute("CREATE TABLE IF NOT EXISTS impostazioni ("
                    + "chiave TEXT PRIMARY KEY, "
                    + "valore TEXT);");

            System.out.println("Tutte le tabelle sono pronte!");
        } catch (SQLException e) {
            System.out.println("Errore inizializzazione: " + e.getMessage());
        }
    }

    // ------ AGGIUNGI ESAMI -------

    public static String[] getEsamiSalvatiRaw() {
        java.util.List<String> listaEsami = new java.util.ArrayList<>();
        // Diciamo al DB: "Seleziona le colonne nome, completato e idoneita dalla tabella esami"
        String sql = "SELECT nome, completato, idoneita FROM esami";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            // rs.next() scorre i risultati uno ad uno finché ce ne sono
            while (rs.next()) {
                String nome = rs.getString("nome");
                boolean completato = rs.getBoolean("completato");
                boolean idoneita = rs.getBoolean("idoneita");

                listaEsami.add(nome + ";" + completato + ";" + idoneita);
            }
        } catch (SQLException e) {
            System.out.println("Errore in lettura: " + e.getMessage());
        }
        return listaEsami.toArray(new String[0]);
    }
    
    public static void salvaEsame(String nome, boolean idoneita) {
        String sql = "INSERT INTO esami(nome, idoneita) VALUES(?, ?)";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            pstmt.setBoolean(2, idoneita);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Errore nel salvataggio dell'esame: " + e.getMessage());
        }
    }

    public static void aggiornaStatoEsame(String nomeEsame, boolean completato) {
        // Diciamo al DB: "Aggiorna la tabella esami, imposta 'completato' al nuovo
        // valore, MA SOLO per la riga dove il nome è uguale a quello passato"
        String sql = "UPDATE esami SET completato = ? WHERE nome = ?";
        try (Connection conn = connect();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, completato); // Primo punto interrogativo
            pstmt.setString(2, nomeEsame); // Secondo punto interrogativo
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Errore in aggiornamento: " + e.getMessage());
        }
    }

    public static void removeNomeEsame(String nomeEsame) {
        // Diciamo al DB: "Cancella dalla tabella esami la riga che ha questo nome"
        String sql = "DELETE FROM esami WHERE nome = ?";

        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomeEsame);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Errore in eliminazione: " + e.getMessage());
        }
    }

    public static void removeScadenza(String nomeDaRimuovere) {
        String sql = "DELETE FROM scadenze WHERE nome_esame = ?";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeDaRimuovere);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- VOTI E I CFU ---

    public static String[] getVotiEsamiRaw() {
        java.util.List<String> voti = new java.util.ArrayList<>();
        String sql = "SELECT voto, nome, cfu FROM esami WHERE completato = 1";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                voti.add(rs.getString("voto") + ";" + rs.getString("nome") + ";" + rs.getInt("cfu"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return voti.toArray(new String[0]);
    }

    public static void setVotiEsami(String voto, String nome, int CFU) {
        // Aggiorna il voto e segna come completato (ignoriamo il CFU passato qui perché
        // lo aggiorni a parte)
        String sql = "UPDATE esami SET voto = ?, completato = 1 WHERE nome = ?";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, voto);
            pstmt.setString(2, nome);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void removeVotiEsame(String nome) {
        // Invece di cancellare la riga, "svuotiamo" il voto e i CFU dell'esame
        String sql = "UPDATE esami SET voto = NULL, cfu = 0, completato = 0 WHERE nome = ?";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addCfuEsame(String nome, int CFU) {
        String sql = "UPDATE esami SET cfu = ? WHERE nome = ?";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, CFU);
            pstmt.setString(2, nome);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- TEMPO DI STUDIO ---

    public static void aggiungiTempoStudio(String nomeEsame, int minuti) {
        String sql = "UPDATE esami SET minuti_studio = minuti_studio + ? WHERE nome = ?";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, minuti);
            pstmt.setString(2, nomeEsame);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void setNuovoTempoStudio(String nomeEsame, int minuti) {
        String sql = "UPDATE esami SET minuti_studio = ? WHERE nome = ?";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, minuti);
            pstmt.setString(2, nomeEsame);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String[] getTuttoLoStudioRaw() {
        java.util.List<String> studio = new java.util.ArrayList<>();
        // Peschiamo solo gli esami che hanno minuti di studio > 0
        String sql = "SELECT nome, minuti_studio FROM esami WHERE minuti_studio > 0";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                studio.add(rs.getString("nome") + ";" + rs.getInt("minuti_studio"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return studio.toArray(new String[0]);
    }

    public static int getMinutiStudioEsame(String nomeEsame) {
        String sql = "SELECT minuti_studio FROM esami WHERE nome = ?";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeEsame);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("minuti_studio");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static void salvaScadenza(String nome, String data) {
        String sql = "INSERT OR REPLACE INTO scadenze(nome_esame, data) VALUES(?, ?)";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String[] getScadenzeRaw() {
        java.util.List<String> scadenze = new java.util.ArrayList<>();
        String sql = "SELECT nome_esame, data FROM scadenze";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                scadenze.add(rs.getString("nome_esame") + ";" + rs.getString("data"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return scadenze.toArray(new String[0]);
    }

    // --- 1. METODI DI CONTEGGIO ---

    public static int numeroEsami() {
        String sql = "SELECT COUNT(*) AS totale FROM esami";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt("totale");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static int numeroVoti() {
        String sql = "SELECT COUNT(*) AS totale FROM esami WHERE completato = 1";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt("totale");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static int numeroScadenze() {
        String sql = "SELECT COUNT(*) AS totale FROM scadenze";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt("totale");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    // --- 2. GESTIONE IMPOSTAZIONI ---

    public static void salvaImpostazione(String chiave, String valore) {
        String sql = "INSERT OR REPLACE INTO impostazioni(chiave, valore) VALUES(?, ?)";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chiave);
            pstmt.setString(2, valore);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getImpostazione(String chiave, String defaultValore) {
        String sql = "SELECT valore FROM impostazioni WHERE chiave = ?";
        try (Connection conn = connect();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chiave);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getString("valore");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return defaultValore;
    }

    // --- 3. SCORCIATOIE IMPOSTAZIONI ---

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

    public static int getPomodori() {
        return Integer.parseInt(getImpostazione("POMODORI", "0"));
    }

    public static void salvaPomodori(int pomodori) {
        salvaImpostazione("POMODORI", String.valueOf(pomodori));
    }

    public static String getDataPomodori() {
        return getImpostazione("POMODORI_DATA", "");
    }

    public static void salvaDataPomodori(String data) {
        salvaImpostazione("POMODORI_DATA", data);
    }

    public static void salvaMaxPomodoriGiornalieri(int max) {
        salvaImpostazione("POMODORI_MAX", String.valueOf(max));
    }

    public static int getMaxPomodoriGiornalieri() {
        String valoreMax = getImpostazione("POMODORI_MAX", "");
        if (valoreMax.isEmpty())
            valoreMax = getImpostazione("POMODORI_SERIE", "0"); // Legacy
        try {
            return Integer.parseInt(valoreMax);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int caricaPomodoriGiornalieri() {
        String oggi = java.time.LocalDate.now().toString();
        if (!oggi.equals(getDataPomodori())) {
            salvaPomodori(0);
            salvaDataPomodori(oggi);
            return 0;
        }
        return getPomodori();
    }

    public static int getMinutiStudio() {
        return Integer.parseInt(getImpostazione("MINUTI_STUDIO", "25"));
    }

    public static int getMinutiPausa() {
        return Integer.parseInt(getImpostazione("MINUTI_PAUSA", "5"));
    }

    public static int getObiettivoMedia() {
        return Integer.parseInt(getImpostazione("OBIETTIVO_MEDIA", "25"));
    }

    public static void salvaObiettivoMedia(int ob) {
        salvaImpostazione("OBIETTIVO_MEDIA", String.valueOf(ob));
    }

    // Metodo fittizio per compatibilità con il vecchio codice di GestoreDati
    public static boolean getSalvato() {
        return true;
    }

    // --- METODO RESET ---

    public static void resetTutto() {
        // Cancella il contenuto di tutte le tabelle in un colpo solo!
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM esami");
            stmt.execute("DELETE FROM scadenze");
            stmt.execute("DELETE FROM impostazioni");
            System.out.println("Tutti i dati sono stati resettati.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

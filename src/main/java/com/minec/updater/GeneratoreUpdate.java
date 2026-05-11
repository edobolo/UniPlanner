package com.minec.updater;

import org.update4j.Configuration;
import org.update4j.FileMetadata;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GeneratoreUpdate {
    public static void main(String[] args) throws Exception {

        Configuration config = Configuration.builder()
                // 1. IL LINK GIUSTO DOVE SONO I FILE
                .baseUri("https://raw.githubusercontent.com/edobolo/UniPlanner/main/aggiornamenti")

                // 2. DOVE SALVARLI SUL PC
                .basePath("${user.dir}")

                // 3. IL FILE JAR (Aggiungiamo .classpath() per dire che è codice eseguibile)
                .file(FileMetadata.readFrom("target/UniPlanner.jar")
                        .uri("UniPlanner.jar")
                        .classpath())

                // 4. LA CLASSE DA AVVIARE (Sostituisci con il nome corretto!)
                // Scrivi qui il nome completo della classe che apre il vero UniPlanner.
                .property("default.launcher.main.class", "com.minec.MainApp")

                .build();

        try (Writer out = Files.newBufferedWriter(Paths.get("config.xml"))) {
            config.write(out);
        }

        System.out.println("Nuovo config.xml generato con i parametri di avvio corretti!");
    }
}
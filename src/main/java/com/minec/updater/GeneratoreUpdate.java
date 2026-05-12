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

                // 2. DOVE SALVARLI SUL PC DELL'UTENTE
                .basePath("${user.dir}")

                // 3. IL FILE JAR (Leggiamo direttamente quello che sta per andare online!)
                .file(FileMetadata.readFrom("aggiornamenti/UniPlanner.jar")
                        .uri("UniPlanner.jar")
                        .classpath())

                // 4. LA CLASSE DA AVVIARE
                .property("default.launcher.main.class", "com.minec.MainApp")

                .build();

        // SALVIAMO IL CONFIG DIRETTAMENTE NELLA CARTELLA DEGLI AGGIORNAMENTI
        try (Writer out = Files.newBufferedWriter(Paths.get("aggiornamenti/config.xml"))) {
            config.write(out);
        }

        System.out.println("Nuovo config.xml generato correttamente nella cartella 'aggiornamenti'!");
    }
}
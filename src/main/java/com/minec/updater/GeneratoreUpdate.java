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

                // 3. IL TRUCCO MAGICO: Legge il file per l'hash, ma gli cambia nome quando lo
                // scarica!
                .file(FileMetadata.readFrom("aggiornamenti/UniPlanner.jar")
                        .uri("UniPlanner_Aggiornato.jar") // <-- Questo bypassa il blocco di Windows!
                        .classpath())

                // 4. LA CLASSE DA AVVIARE
                .property("default.launcher.main.class", "com.minec.MainApp")

                .build();

        try (Writer out = Files.newBufferedWriter(Paths.get("aggiornamenti/config.xml"))) {
            config.write(out);
        }

        System.out.println("Nuovo config.xml generato con il trucco anti-blocco Windows!");
    }
}
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

                // 3. IL TRUCCO MAGICO CORRETTO
                .file(FileMetadata.readFrom("aggiornamenti/UniPlanner.jar")
                        .uri("UniPlanner.jar") // <-- Come si chiama su GitHub (Internet)
                        .path(Paths.get("UniPlanner_Aggiornato.jar")) // <-- Come lo salviamo sul PC (Anti-Blocco)
                        .classpath())

                // 4. LA CLASSE DA AVVIARE
                .property("default.launcher.main.class", "com.minec.MainApp")

                .build();

        try (Writer out = Files.newBufferedWriter(Paths.get("aggiornamenti/config.xml"))) {
            config.write(out);
        }

        System.out.println("Nuovo config.xml generato con il trucco anti-blocco Windows corretto!");
    }
}
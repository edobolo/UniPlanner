package com.minec.updater;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GeneratoreUpdate {
    public static void main(String[] args) throws Exception {

        // Costruiamo la mappa di aggiornamento
        Configuration config = Configuration.builder()
                // 1. Dove saranno i file su internet? (Es. una repo GitHub Pages)
                .baseUri("https://edobolo.github.io/uniplanner-updates")

                // 2. Dove andranno salvati sul PC dell'utente? (nella cartella da cui avvia il
                // programma)
                .basePath("${user.dir}")

                // 3. Quali file fanno parte dell'app? (Devi avere il tuo file .jar pronto)
                .file(FileMetadata.readFrom("target/UniPlanner.jar").uri("UniPlanner.jar"))
                .build();

        // Salviamo la mappa in un file config.xml
        try (Writer out = Files.newBufferedWriter(Paths.get("config.xml"))) {
            config.write(out);
        }

        System.out.println("File config.xml generato con successo! Pronto per l'upload.");
    }
}
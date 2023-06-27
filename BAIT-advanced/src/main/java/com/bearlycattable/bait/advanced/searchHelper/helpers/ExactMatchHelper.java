package com.bearlycattable.bait.advanced.searchHelper.helpers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

public class ExactMatchHelper {

    private static final Logger LOG = Logger.getLogger(ExactMatchHelper.class.getName());

    public static synchronized String appendMatchToFile(String priv, String matchedPKH, String targetPath) {

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(targetPath).toAbsolutePath(), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(priv + " matched the following unknownPKH: " + matchedPKH);
            writer.newLine();
        } catch (IOException e) {
            LOG.info("Error writing matched key to file at ExactMatchHelper#writeMatchToFile"
            + System.lineSeparator()
            + "Priv was: " + priv + System.lineSeparator()
            + "PKH matched: " + matchedPKH);
            e.printStackTrace();
        }

        return targetPath;
    }
}

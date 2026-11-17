package org.delcom.ifs25002springbootcashflow.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DotEnv {

    public static void load() {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            return;
        }
        try {
            for (String rawLine : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] pair = line.split("=", 2);
                if (pair.length < 2) {
                    continue;
                }
                String key = pair[0].trim();
                String value = pair[1].trim().replaceAll("^[\"']|[\"']$", "");
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
        }
    }
}
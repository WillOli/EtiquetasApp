package config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

/** Configuração: propriedade JVM > ambiente > arquivo externo > recurso do JAR. */
public final class AppConfig {
    private static final Properties PROPERTIES = load();
    private AppConfig() {}

    private static Properties load() {
        Properties result = new Properties();
        try (InputStream input = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (input != null) result.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            String file = System.getProperty("etiquetas.config.file", System.getenv("ETIQUETAS_CONFIG_FILE"));
            if (file != null && !file.isBlank()) {
                try (Reader reader = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
                    result.load(reader);
                }
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar a configuração da aplicação.", e);
        }
    }

    static String value(String key, String environment, String fallback) {
        String value = System.getProperty("etiquetas." + key);
        if (value == null) value = System.getenv(environment);
        if (value == null) value = PROPERTIES.getProperty(key, fallback);
        return value.trim();
    }

    public static int getServerPort() {
        try {
            int port = Integer.parseInt(value("server.port", "ETIQUETAS_SERVER_PORT", "8081"));
            if (port < 1 || port > 65535) throw new NumberFormatException();
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("server.port deve estar entre 1 e 65535.", e);
        }
    }

    public static boolean isDevelopment() {
        String mode = value("mode", "ETIQUETAS_MODE", "production");
        if (!mode.equals("production") && !mode.equals("development"))
            throw new IllegalArgumentException("mode deve ser production ou development.");
        return mode.equals("development");
    }

    public static String getWebDirectory() {
        return value("web.directory", "ETIQUETAS_WEB_DIRECTORY", "src/main/resources/web");
    }

    public static String getPrinterName() {
        return value("printer.name", "ETIQUETAS_PRINTER_NAME", "");
    }

    public static Path getSequenceFilePath() {
        String configured = value("sequence.file", "ETIQUETAS_SEQUENCE_FILE", "");
        return (configured.isBlank() ? Path.of(System.getProperty("user.home"),
                "EspacoVista", "EtiquetasApp", "data", "sequence.txt") : Path.of(configured))
                .toAbsolutePath().normalize();
    }
}

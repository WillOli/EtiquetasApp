package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties properties = new Properties();
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    static {
        try (InputStream input = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                logger.warn("Arquivo application.properties não encontrado. Usando valores padrão.");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            logger.error("Erro crítico ao carregar o arquivo de propriedades.", ex);
        }
    }

    public static int getServerPort() {
        String portStr = properties.getProperty("server.port", "8081");
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            logger.warn("A porta '{}' não é um número válido. Usando a porta padrão 8080.", portStr);
            return 8080;
        }
    }

    public static String getLogFilename() {
        // Este método pode ser removido se não for mais usado em outro lugar.
        return properties.getProperty("log.filename", "logs/impressao.log");
    }

    public static Path getSequenceFilePath() {

        String systemProperty =
                System.getProperty(
                        "etiquetas.sequence.file"
                );

        if (systemProperty != null &&
                !systemProperty.trim().isEmpty()) {

            return Paths.get(
                    systemProperty.trim()
            );
        }

        String environmentVariable =
                System.getenv(
                        "ETIQUETAS_SEQUENCE_FILE"
                );

        if (environmentVariable != null &&
                !environmentVariable.trim().isEmpty()) {

            return Paths.get(
                    environmentVariable.trim()
            );
        }

        String configuredPath =
                properties.getProperty(
                        "sequence.file"
                );

        if (configuredPath != null &&
                !configuredPath.trim().isEmpty()) {

            return Paths.get(
                    configuredPath.trim()
            );
        }

        /*
         * Fallback de desenvolvimento.
         *
         * Mac:
         * ~/EspacoVista/EtiquetasApp/data/sequence.txt
         *
         * Windows:
         * C:\Users\<usuario>\EspacoVista\EtiquetasApp\data\sequence.txt
         */
        return Paths.get(
                System.getProperty("user.home"),
                "EspacoVista",
                "EtiquetasApp",
                "data",
                "sequence.txt"
        );
    }
}

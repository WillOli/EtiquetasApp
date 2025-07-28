package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe responsável por carregar as configurações do arquivo application.properties.
 */
public class AppConfig {

    private static final Properties properties = new Properties();

    // Bloco estático para carregar o arquivo de propriedades uma única vez
    // quando a classe é carregada pela primeira vez.
    static {
        try (InputStream input = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                System.out.println("Não foi possível encontrar o arquivo application.properties.");
                // O programa pode continuar com os valores padrão.
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Erro ao carregar o arquivo de propriedades.");
            ex.printStackTrace();
        }
    }

    /**
     * Retorna a porta do servidor definida no arquivo de propriedades.
     * Se não for encontrada, retorna a porta padrão 8080.
     * @return A porta do servidor.
     */
    public static int getServerPort() {
        String portStr = properties.getProperty("server.port", "8080");
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            System.err.println("A porta '" + portStr + "' não é um número válido. Usando a porta padrão 8080.");
            return 8080;
        }
    }

    /**
     * Retorna o nome do arquivo de log definido no arquivo de propriedades.
     * Se não for encontrado, retorna o nome padrão "logs.txt".
     * @return O nome do arquivo de log.
     */
    public static String getLogFilename() {
        return properties.getProperty("log.filename", "logs.txt");
    }
}
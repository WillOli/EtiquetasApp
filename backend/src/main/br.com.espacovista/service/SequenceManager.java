package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SequenceManager {
    private static final Logger logger = LoggerFactory.getLogger(SequenceManager.class);

    // O arquivo será salvo automaticamente na raiz do seu projeto no Windows
    private static final Path LOG_FILE = Paths.get("log_impressoes.txt");

    /**
     * Lê o último número do arquivo, calcula o próximo início e já salva a numeração futura.
     * O 'synchronized' impede concorrência e duplicação de registros.
     */
    public static synchronized long getNextSequenceAndIncrement(int quantityToPrint) {
        long currentSequence = 1L; // Número padrão caso o arquivo ainda não exista

        try {
            // 1. Se o arquivo existir, lê o número que está lá dentro
            if (Files.exists(LOG_FILE)) {
                String content = Files.readString(LOG_FILE).trim();
                if (!content.isEmpty()) {
                    currentSequence = Long.parseLong(content);
                }
            } else {
                logger.info("Arquivo log_impressoes.txt não encontrado. Criando novo arquivo na raiz do projeto.");
            }

            // 2. Calcula qual será o número da PRÓXIMA impressão no futuro
            long nextSequenceToSave = currentSequence + quantityToPrint;

            // 3. Sobrescreve o arquivo .txt com a nova numeração futura
            Files.writeString(LOG_FILE, String.valueOf(nextSequenceToSave),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("Sequência liberada: [{}]. Próxima impressão começará em: [{}]", currentSequence, nextSequenceToSave);

        } catch (IOException | NumberFormatException e) {
            logger.error("Erro ao ler ou gravar no log_impressoes.txt. Usando sequência de fallback (1).", e);
        }

        // Retorna o número onde a impressão ATUAL deve começar
        return currentSequence;
    }
}
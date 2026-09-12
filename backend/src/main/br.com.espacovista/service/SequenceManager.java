package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SequenceManager {

    private static final Logger logger =
            LoggerFactory.getLogger(SequenceManager.class);

    private static final Path LOG_FILE =
            Paths.get("log_impressoes.txt");

    /**
     * Método utilizado pela aplicação.
     */
    public static long getNextSequenceAndIncrement(
            int quantityToPrint
    ) {
        return getNextSequenceAndIncrement(
                LOG_FILE,
                quantityToPrint
        );
    }

    /**
     * Sobrecarga que permite informar o arquivo da sequência.
     * Usada pelos testes para não alterar o arquivo real.
     */
    public static synchronized long getNextSequenceAndIncrement(
            Path sequenceFile,
            int quantityToPrint
    ) {

        if (quantityToPrint < 1) {
            throw new SequenceException(
                    "A quantidade de registros a reservar deve ser maior que zero."
            );
        }

        long currentSequence = 1L;

        try {

            if (Files.exists(sequenceFile)) {

                String content =
                        Files.readString(sequenceFile).trim();

                if (!content.isEmpty()) {
                    currentSequence =
                            Long.parseLong(content);
                }

            } else {

                logger.info(
                        "Arquivo de sequência não encontrado. Criando novo arquivo."
                );
            }

            long nextSequenceToSave =
                    currentSequence + quantityToPrint;

            Files.writeString(
                    sequenceFile,
                    String.valueOf(nextSequenceToSave),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            logger.info(
                    "Sequência reservada de [{}] até [{}]. Próximo registro: [{}]",
                    currentSequence,
                    nextSequenceToSave - 1,
                    nextSequenceToSave
            );

            return currentSequence;

        } catch (IOException e) {

            logger.error(
                    "Falha ao acessar ou gravar o arquivo de sequência.",
                    e
            );

            throw new SequenceException(
                    "Não foi possível reservar a sequência de impressão.",
                    e
            );

        } catch (NumberFormatException e) {

            logger.error(
                    "Arquivo de sequência contém um valor inválido.",
                    e
            );

            throw new SequenceException(
                    "O arquivo de sequência contém um valor inválido.",
                    e
            );
        }
    }
}
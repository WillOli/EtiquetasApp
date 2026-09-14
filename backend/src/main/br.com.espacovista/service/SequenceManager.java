package service;

import config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class SequenceManager {

    private static final Logger logger =
            LoggerFactory.getLogger(SequenceManager.class);

    /**
     * Caminho oficial utilizado pela aplicação.
     *
     * A definição do caminho fica centralizada no AppConfig.
     */
    private static final Path SEQUENCE_FILE =
            AppConfig.getSequenceFilePath();

    /**
     * Método utilizado normalmente pela aplicação.
     *
     * Reserva a quantidade necessária de registros utilizando
     * o arquivo oficial configurado no AppConfig.
     */
    public static long getNextSequenceAndIncrement(
            int quantityToPrint
    ) {

        return getNextSequenceAndIncrement(
                SEQUENCE_FILE,
                quantityToPrint
        );
    }

    /**
     * Sobrecarga utilizada também pelos testes.
     *
     * Permite informar um arquivo específico para que os testes
     * não alterem a sequência real da aplicação.
     */
    public static synchronized long getNextSequenceAndIncrement(
            Path sequenceFile,
            int quantityToPrint
    ) {

        /*
         * Validação da quantidade.
         */
        if (quantityToPrint < 1) {

            throw new SequenceException(
                    "A quantidade de registros a reservar deve ser maior que zero."
            );
        }

        /*
         * Validação do caminho.
         */
        if (sequenceFile == null) {

            throw new SequenceException(
                    "O caminho do arquivo de sequência não foi configurado."
            );
        }

        long currentSequence = 1L;

        try {

            /*
             * =========================================================
             * 1. GARANTE QUE O DIRETÓRIO EXISTA
             * =========================================================
             */

            Path parentDirectory =
                    sequenceFile.getParent();

            if (parentDirectory != null) {

                Files.createDirectories(
                        parentDirectory
                );

                if (!Files.isWritable(parentDirectory)) {

                    throw new SequenceException(
                            "Sem permissão de escrita no diretório da sequência: "
                                    + parentDirectory
                    );
                }
            }

            /*
             * =========================================================
             * 2. LÊ E VALIDA O ARQUIVO EXISTENTE
             * =========================================================
             */

            if (Files.exists(sequenceFile)) {

                /*
                 * Garante que não estamos apontando, por exemplo,
                 * para uma pasta no lugar de um arquivo.
                 */
                if (!Files.isRegularFile(sequenceFile)) {

                    throw new SequenceException(
                            "O caminho da sequência não aponta para um arquivo válido: "
                                    + sequenceFile
                    );
                }

                /*
                 * O sistema precisa conseguir ler o próximo registro.
                 */
                if (!Files.isReadable(sequenceFile)) {

                    throw new SequenceException(
                            "Sem permissão de leitura no arquivo de sequência: "
                                    + sequenceFile
                    );
                }

                /*
                 * E também precisa conseguir atualizar o arquivo.
                 */
                if (!Files.isWritable(sequenceFile)) {

                    throw new SequenceException(
                            "Sem permissão de escrita no arquivo de sequência: "
                                    + sequenceFile
                    );
                }

                String content =
                        Files.readString(sequenceFile).trim();

                /*
                 * Arquivo existente porém vazio é considerado erro.
                 *
                 * Não retornamos para 1 porque isso poderia causar
                 * reutilização de registros já impressos.
                 */
                if (content.isEmpty()) {

                    throw new SequenceException(
                            "O arquivo de sequência está vazio: "
                                    + sequenceFile
                    );
                }

                currentSequence =
                        Long.parseLong(content);

                /*
                 * O registro precisa sempre ser positivo.
                 */
                if (currentSequence < 1) {

                    throw new SequenceException(
                            "O número armazenado no arquivo de sequência é inválido: "
                                    + currentSequence
                    );
                }

            } else {

                /*
                 * Arquivo inexistente é permitido.
                 *
                 * Isso representa a primeira inicialização da sequência.
                 */
                logger.info(
                        "Arquivo de sequência não encontrado. " +
                                "Um novo arquivo será criado em [{}].",
                        sequenceFile.toAbsolutePath()
                );
            }

            /*
             * =========================================================
             * 3. CALCULA O PRÓXIMO REGISTRO DISPONÍVEL
             * =========================================================
             */

            long nextSequenceToSave =
                    Math.addExact(
                            currentSequence,
                            quantityToPrint
                    );

            /*
             * =========================================================
             * 4. GRAVA PRIMEIRO EM ARQUIVO TEMPORÁRIO
             * =========================================================
             *
             * Não truncamos diretamente o sequence.txt.
             *
             * Primeiro gravamos:
             *
             * sequence.txt.tmp
             *
             * e somente depois substituímos o arquivo oficial.
             */

            Path tempFile =
                    sequenceFile.resolveSibling(
                            sequenceFile
                                    .getFileName()
                                    .toString()
                                    + ".tmp"
                    );

            Files.writeString(
                    tempFile,
                    String.valueOf(nextSequenceToSave),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            /*
             * =========================================================
             * 5. SUBSTITUI O ARQUIVO OFICIAL
             * =========================================================
             *
             * ATOMIC_MOVE reduz o risco de termos um arquivo
             * parcialmente atualizado em caso de interrupção.
             */

            try {

                Files.move(
                        tempFile,
                        sequenceFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {

                /*
                 * Alguns sistemas de arquivos não suportam
                 * movimentação atômica.
                 *
                 * Nesse caso usamos uma substituição convencional.
                 */

                logger.warn(
                        "Movimentação atômica não suportada para [{}]. " +
                                "Usando substituição convencional.",
                        sequenceFile.toAbsolutePath()
                );

                Files.move(
                        tempFile,
                        sequenceFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            /*
             * =========================================================
             * 6. REGISTRA A RESERVA REALIZADA
             * =========================================================
             */

            logger.info(
                    "Sequência reservada de [{}] até [{}]. " +
                            "Próximo registro: [{}]. Arquivo: [{}]",
                    currentSequence,
                    nextSequenceToSave - 1,
                    nextSequenceToSave,
                    sequenceFile.toAbsolutePath()
            );

            /*
             * Retorna o primeiro registro reservado.
             */
            return currentSequence;

        } catch (NumberFormatException e) {

            /*
             * Exemplo:
             *
             * sequence.txt contém:
             *
             * ABC
             *
             * Não tentamos corrigir automaticamente.
             */

            logger.error(
                    "Arquivo de sequência contém um valor inválido. Arquivo: [{}]",
                    sequenceFile.toAbsolutePath(),
                    e
            );

            throw new SequenceException(
                    "O arquivo de sequência contém um valor inválido.",
                    e
            );

        } catch (ArithmeticException e) {

            /*
             * Proteção contra overflow de long.
             */
            logger.error(
                    "A sequência ultrapassou o limite numérico permitido.",
                    e
            );

            throw new SequenceException(
                    "A sequência atingiu um valor inválido.",
                    e
            );

        } catch (IOException e) {

            /*
             * Qualquer problema real de leitura ou escrita impede
             * a impressão para não correr risco de repetir registros.
             */

            logger.error(
                    "Falha ao acessar ou gravar o arquivo de sequência: [{}]",
                    sequenceFile.toAbsolutePath(),
                    e
            );

            throw new SequenceException(
                    "Não foi possível reservar a sequência de impressão.",
                    e
            );
        }
    }
}
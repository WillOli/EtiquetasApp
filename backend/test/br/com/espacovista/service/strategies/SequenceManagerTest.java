package br.com.espacovista.service.strategies;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import service.SequenceException;
import service.SequenceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SequenceManagerTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deve criar diretórios automaticamente e persistir a sequência")
    void shouldCreateDirectoriesAndPersistSequence()
            throws IOException {

        Path sequenceFile =
                tempDir
                        .resolve("EspacoVista")
                        .resolve("EtiquetasApp")
                        .resolve("data")
                        .resolve("sequence.txt");

        assertFalse(
                Files.exists(sequenceFile.getParent())
        );

        SequenceManager.initialize(sequenceFile, 1);

        long firstSequence =
                SequenceManager.getNextSequenceAndIncrement(
                        sequenceFile,
                        1
                );

        assertEquals(
                1L,
                firstSequence
        );

        assertTrue(
                Files.exists(sequenceFile)
        );

        assertTrue(
                Files.isDirectory(
                        sequenceFile.getParent()
                )
        );

        assertEquals(
                "2",
                Files.readString(sequenceFile).trim()
        );
    }

    @Test
    @DisplayName("Deve reservar quantidade 1 corretamente")
    void shouldReserveOneSequence()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        Files.writeString(sequenceFile, "10");

        long firstSequence =
                SequenceManager.getNextSequenceAndIncrement(
                        sequenceFile,
                        1
                );

        assertEquals(10L, firstSequence);

        assertEquals(
                "11",
                Files.readString(sequenceFile).trim()
        );
    }

    @Test
    @DisplayName("Deve reservar quantidade 2 corretamente")
    void shouldReserveTwoSequences()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        Files.writeString(sequenceFile, "20");

        long firstSequence =
                SequenceManager.getNextSequenceAndIncrement(
                        sequenceFile,
                        2
                );

        assertEquals(20L, firstSequence);

        assertEquals(
                "22",
                Files.readString(sequenceFile).trim()
        );
    }

    @Test
    @DisplayName("Deve reservar quantidade 3 corretamente")
    void shouldReserveThreeSequences()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        Files.writeString(sequenceFile, "30");

        long firstSequence =
                SequenceManager.getNextSequenceAndIncrement(
                        sequenceFile,
                        3
                );

        assertEquals(30L, firstSequence);

        assertEquals(
                "33",
                Files.readString(sequenceFile).trim()
        );
    }

    @Test
    @DisplayName("Duas reservas consecutivas não devem reutilizar registros")
    void consecutiveReservationsShouldNotReuseSequences()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        Files.writeString(sequenceFile, "100");

        long firstReservation =
                SequenceManager.getNextSequenceAndIncrement(
                        sequenceFile,
                        3
                );

        long secondReservation =
                SequenceManager.getNextSequenceAndIncrement(
                        sequenceFile,
                        2
                );

        assertEquals(100L, firstReservation);
        assertEquals(103L, secondReservation);

        assertEquals(
                "105",
                Files.readString(sequenceFile).trim()
        );
    }

    @Test
    @DisplayName("Quantidade zero deve impedir reserva de sequência")
    void zeroQuantityShouldThrowException() {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        assertThrows(
                SequenceException.class,
                () -> SequenceManager
                        .getNextSequenceAndIncrement(
                                sequenceFile,
                                0
                        )
        );

        assertFalse(
                Files.exists(sequenceFile),
                "O arquivo não deve ser criado quando a quantidade é inválida."
        );
    }

    @Test
    @DisplayName("Quantidade negativa deve impedir reserva de sequência")
    void negativeQuantityShouldThrowException() {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        assertThrows(
                SequenceException.class,
                () -> SequenceManager
                        .getNextSequenceAndIncrement(
                                sequenceFile,
                                -1
                        )
        );
    }

    @Test
    @DisplayName("Conteúdo inválido no arquivo deve interromper a reserva")
    void invalidFileContentShouldThrowException()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        Files.writeString(
                sequenceFile,
                "REGISTRO-INVALIDO"
        );

        assertThrows(
                SequenceException.class,
                () -> SequenceManager
                        .getNextSequenceAndIncrement(
                                sequenceFile,
                                1
                        )
        );

        assertEquals(
                "REGISTRO-INVALIDO",
                Files.readString(sequenceFile).trim(),
                "O arquivo inválido não deve ser sobrescrito silenciosamente."
        );
    }

    @Test
    @DisplayName("Erro de leitura deve interromper a reserva")
    void readFailureShouldThrowException()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        // Cria um diretório no lugar onde deveria existir um arquivo.
        Files.createDirectory(sequenceFile);

        assertThrows(
                SequenceException.class,
                () -> SequenceManager
                        .getNextSequenceAndIncrement(
                                sequenceFile,
                                1
                        )
        );
    }

    @Test
    @DisplayName("Falha real de acesso ao caminho deve interromper a reserva")
    void ioFailureShouldThrowException()
            throws IOException {

        /*
         * Criamos um ARQUIVO onde deveria existir um diretório.
         */
        Path invalidParent =
                tempDir.resolve(
                        "arquivo-no-lugar-de-diretorio"
                );

        Files.writeString(
                invalidParent,
                "conteudo"
        );

        /*
         * Tentamos criar sequence.txt dentro desse arquivo.
         *
         * Isso é impossível e deve gerar erro de I/O.
         */
        Path sequenceFile =
                invalidParent.resolve(
                        "sequence.txt"
                );

        assertThrows(
                SequenceException.class,
                () -> SequenceManager
                        .getNextSequenceAndIncrement(
                                sequenceFile,
                                1
                        )
        );
    }


    @Test
    @DisplayName("Arquivo de sequência vazio deve interromper a reserva")
    void emptySequenceFileShouldThrowException()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        Files.writeString(
                sequenceFile,
                ""
        );

        assertThrows(
                SequenceException.class,
                () -> SequenceManager
                        .getNextSequenceAndIncrement(
                                sequenceFile,
                                1
                        )
        );
    }

    @Test
    @DisplayName("Sequência zero deve ser rejeitada")
    void zeroStoredSequenceShouldThrowException()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        Files.writeString(
                sequenceFile,
                "0"
        );

        assertThrows(
                SequenceException.class,
                () -> SequenceManager
                        .getNextSequenceAndIncrement(
                                sequenceFile,
                                1
                        )
        );
    }

    @Test
    @DisplayName("Sequência negativa deve ser rejeitada")
    void negativeStoredSequenceShouldThrowException()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        Files.writeString(
                sequenceFile,
                "-10"
        );

        assertThrows(
                SequenceException.class,
                () -> SequenceManager
                        .getNextSequenceAndIncrement(
                                sequenceFile,
                                1
                        )
        );
    }
}
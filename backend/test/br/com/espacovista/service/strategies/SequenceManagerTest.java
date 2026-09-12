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
    @DisplayName("Deve iniciar sequência em 1 quando arquivo não existir")
    void shouldStartSequenceAtOneWhenFileDoesNotExist()
            throws IOException {

        Path sequenceFile =
                tempDir.resolve("sequence.txt");

        long firstSequence =
                SequenceManager.getNextSequenceAndIncrement(
                        sequenceFile,
                        1
                );

        assertEquals(1L, firstSequence);

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
    @DisplayName("Erro de escrita deve interromper a reserva")
    void writeFailureShouldThrowException() {

        Path sequenceFile =
                tempDir
                        .resolve("diretorio-inexistente")
                        .resolve("sequence.txt");

        // O diretório pai propositalmente não existe.
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
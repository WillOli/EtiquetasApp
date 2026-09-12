package br.com.espacovista.service.strategies;

import model.PrintRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import service.PrinterStrategyFactory;
import service.SequenceManager;
import service.strategies.ILabelStrategy;
import service.strategies.SimpleLayoutStrategy;
import service.strategies.SimpleStandardStrategy;

import static org.junit.jupiter.api.Assertions.*;

class PrinterStrategyFactoryTest {

    @Test
    @DisplayName(
            "Etiqueta simples com quantidade 1 deve reservar 1 registro"
    )
    void simpleQuantityOneShouldReserveOneSequence() {

        PrintRequest request =
                createRequest(
                        1,
                        PrintRequest.LabelType.SIXTY_TWO_MM
                );

        try (
                MockedStatic<SequenceManager> sequenceMock =
                        Mockito.mockStatic(
                                SequenceManager.class
                        )
        ) {

            sequenceMock
                    .when(
                            () -> SequenceManager
                                    .getNextSequenceAndIncrement(1)
                    )
                    .thenReturn(100L);

            ILabelStrategy strategy =
                    PrinterStrategyFactory
                            .getStrategy(request);

            assertInstanceOf(
                    SimpleLayoutStrategy.class,
                    strategy
            );

            sequenceMock.verify(
                    () -> SequenceManager
                            .getNextSequenceAndIncrement(1)
            );
        }
    }

    @Test
    @DisplayName(
            "Etiqueta simples com quantidade 3 deve reservar 3 registros"
    )
    void simpleQuantityThreeShouldReserveThreeSequences() {

        PrintRequest request =
                createRequest(
                        3,
                        PrintRequest.LabelType.SIXTY_TWO_MM
                );

        try (
                MockedStatic<SequenceManager> sequenceMock =
                        Mockito.mockStatic(
                                SequenceManager.class
                        )
        ) {

            sequenceMock
                    .when(
                            () -> SequenceManager
                                    .getNextSequenceAndIncrement(3)
                    )
                    .thenReturn(100L);

            ILabelStrategy strategy =
                    PrinterStrategyFactory
                            .getStrategy(request);

            assertInstanceOf(
                    SimpleLayoutStrategy.class,
                    strategy
            );

            sequenceMock.verify(
                    () -> SequenceManager
                            .getNextSequenceAndIncrement(3)
            );
        }
    }

    @Test
    @DisplayName(
            "Etiqueta dupla com quantidade 1 deve reservar 2 registros"
    )
    void standardQuantityOneShouldReserveTwoSequences() {

        PrintRequest request =
                createRequest(
                        1,
                        PrintRequest.LabelType.STANDARD
                );

        try (
                MockedStatic<SequenceManager> sequenceMock =
                        Mockito.mockStatic(
                                SequenceManager.class
                        )
        ) {

            sequenceMock
                    .when(
                            () -> SequenceManager
                                    .getNextSequenceAndIncrement(2)
                    )
                    .thenReturn(100L);

            ILabelStrategy strategy =
                    PrinterStrategyFactory
                            .getStrategy(request);

            assertInstanceOf(
                    SimpleStandardStrategy.class,
                    strategy
            );

            sequenceMock.verify(
                    () -> SequenceManager
                            .getNextSequenceAndIncrement(2)
            );
        }
    }

    @Test
    @DisplayName(
            "Etiqueta dupla com quantidade 2 deve reservar 4 registros"
    )
    void standardQuantityTwoShouldReserveFourSequences() {

        PrintRequest request =
                createRequest(
                        2,
                        PrintRequest.LabelType.STANDARD
                );

        try (
                MockedStatic<SequenceManager> sequenceMock =
                        Mockito.mockStatic(
                                SequenceManager.class
                        )
        ) {

            sequenceMock
                    .when(
                            () -> SequenceManager
                                    .getNextSequenceAndIncrement(4)
                    )
                    .thenReturn(100L);

            PrinterStrategyFactory.getStrategy(request);

            sequenceMock.verify(
                    () -> SequenceManager
                            .getNextSequenceAndIncrement(4)
            );
        }
    }

    @Test
    @DisplayName(
            "Etiqueta dupla com quantidade 3 deve reservar 6 registros"
    )
    void standardQuantityThreeShouldReserveSixSequences() {

        PrintRequest request =
                createRequest(
                        3,
                        PrintRequest.LabelType.STANDARD
                );

        try (
                MockedStatic<SequenceManager> sequenceMock =
                        Mockito.mockStatic(
                                SequenceManager.class
                        )
        ) {

            sequenceMock
                    .when(
                            () -> SequenceManager
                                    .getNextSequenceAndIncrement(6)
                    )
                    .thenReturn(100L);

            ILabelStrategy strategy =
                    PrinterStrategyFactory
                            .getStrategy(request);

            assertInstanceOf(
                    SimpleStandardStrategy.class,
                    strategy
            );

            sequenceMock.verify(
                    () -> SequenceManager
                            .getNextSequenceAndIncrement(6)
            );

            String zpl =
                    strategy.generateZpl();

            assertTrue(zpl.contains("00100"));
            assertTrue(zpl.contains("00101"));
            assertTrue(zpl.contains("00102"));
            assertTrue(zpl.contains("00103"));
            assertTrue(zpl.contains("00104"));
            assertTrue(zpl.contains("00105"));
        }
    }

    private PrintRequest createRequest(
            int quantity,
            PrintRequest.LabelType labelType
    ) {

        PrintRequest request =
                new PrintRequest();

        request.setText("PRODUTO TESTE");
        request.setSetor("CONFEITARIA");
        request.setDataFabricacao("12/09/2026");
        request.setDataValidade("13/09/2026");
        request.setQuantity(quantity);
        request.setLabelType(labelType.name());

        return request;
    }
}
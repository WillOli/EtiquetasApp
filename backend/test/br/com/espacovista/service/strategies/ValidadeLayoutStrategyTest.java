package br.com.espacovista.service.strategies;

import model.PrintRequest;
import model.ValidadePrintRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.strategies.ILabelStrategy;
import service.strategies.ValidadeLayoutStrategy;

import static org.junit.jupiter.api.Assertions.*;

class ValidadeLayoutStrategyTest {

    @Test
    @DisplayName("Deve gerar ZPL da etiqueta de validade simples com fabricação, abertura e validade")
    void generateZpl_forSimpleValidity_shouldGenerateExpectedContent() {

        // Arrange
        ValidadePrintRequest request = new ValidadePrintRequest();
        request.setProductName("Isca de Filé");
        request.setDataFabricacao("2026-09-11");
        request.setDataAbertura("2026-09-11");
        request.setDataValidade("2026-09-12");
        request.setQuantity(1);
        request.setLabelType(PrintRequest.LabelType.SIXTY_TWO_MM);

        ILabelStrategy strategy = new ValidadeLayoutStrategy(request);

        // Act
        String zplResult = strategy.generateZpl();

        // Assert
        assertNotNull(zplResult);
        assertFalse(zplResult.isBlank());

        assertTrue(zplResult.startsWith("^XA"));
        assertTrue(zplResult.endsWith("^XZ\n"));

        assertTrue(
                zplResult.contains("Isca de Filé"),
                "O nome do produto deve estar presente no ZPL."
        );

        assertTrue(
                zplResult.contains("11/09/2026"),
                "As datas de fabricação e abertura devem ser formatadas como DD/MM/YYYY."
        );

        assertTrue(
                zplResult.contains("12/09/2026"),
                "A data de validade deve ser formatada como DD/MM/YYYY."
        );
    }

    @Test
    @DisplayName("Deve gerar uma etiqueta simples para cada unidade solicitada")
    void generateZpl_withQuantityTwo_shouldGenerateTwoLabels() {

        // Arrange
        ValidadePrintRequest request = new ValidadePrintRequest();
        request.setProductName("Molho de Tomate");
        request.setDataFabricacao("2026-09-10");
        request.setDataAbertura("2026-09-11");
        request.setDataValidade("2026-09-13");
        request.setQuantity(2);
        request.setLabelType(PrintRequest.LabelType.SIXTY_TWO_MM);

        ILabelStrategy strategy = new ValidadeLayoutStrategy(request);

        // Act
        String zplResult = strategy.generateZpl();

        // Assert
        assertNotNull(zplResult);

        assertEquals(
                2,
                countOccurrences(zplResult, "^XA"),
                "Devem ser geradas duas etiquetas."
        );

        assertEquals(
                2,
                countOccurrences(zplResult, "^XZ"),
                "Cada etiqueta deve possuir seu próprio fechamento ZPL."
        );

        assertEquals(
                2,
                countOccurrences(zplResult, "Molho de Tomate"),
                "O produto deve aparecer uma vez em cada etiqueta."
        );
    }

    private int countOccurrences(String text, String value) {
        return text.split(
                java.util.regex.Pattern.quote(value),
                -1
        ).length - 1;
    }
}
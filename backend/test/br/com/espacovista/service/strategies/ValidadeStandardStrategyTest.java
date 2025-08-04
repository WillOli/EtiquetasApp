package br.com.espacovista.service.strategies;
import model.PrintRequest;
import model.ValidadePrintRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.strategies.ILabelStrategy;
import service.strategies.ValidadeStandardStrategy;

import static org.junit.jupiter.api.Assertions.*;

class ValidadeStandardStrategyTest {

    @Test
    @DisplayName("Deve gerar ZPL para etiqueta de validade dupla com 2 colunas")
    void generateZpl_forDoubleValidity_shouldGenerateTwoColumns() {
        // Arrange
        ValidadePrintRequest request = new ValidadePrintRequest();
        request.setProductName("Torta de Limao");
        request.setMfgDate("2025-08-04");
        request.setValidityDays(3);
        request.setQuantity(1); // Pedido de 1, deve imprimir 2
        request.setLabelType(PrintRequest.LabelType.STANDARD);

        ILabelStrategy strategy = new ValidadeStandardStrategy(request);

        // Act
        String zplResult = strategy.generateZpl();

        // Assert
        assertNotNull(zplResult);
        assertTrue(zplResult.startsWith("^XA") && zplResult.endsWith("^XZ\n"));

        // Verifica se o nome do produto aparece duas vezes
        int count = (zplResult.split("\\^FDTorta de Limao\\^FS", -1).length) - 1;
        assertEquals(2, count, "O nome do produto deve aparecer duas vezes.");
    }
}

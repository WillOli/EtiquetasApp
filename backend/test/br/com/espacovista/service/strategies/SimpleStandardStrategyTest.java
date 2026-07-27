package br.com.espacovista.service.strategies;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.strategies.ILabelStrategy;
import service.strategies.SimpleStandardStrategy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleStandardStrategyTest {

    @Test
    @DisplayName("Deve gerar ZPL para Etiqueta Dupla (Standard) com duas colunas e todos os campos")
    void generateZpl_forSimpleStandard_shouldGenerateTwoColumnsWithFields() {
        // Arrange
        ILabelStrategy strategy = new SimpleStandardStrategy(
                "Cookie Nutella",
                "CONFEITARIA",
                "27/07/2026",
                "30/07/2026",
                "00015",
                2
        );

        // Act
        String zplResult = strategy.generateZpl();

        // Assert
        assertNotNull(zplResult, "O resultado do ZPL não deve ser nulo.");
        assertTrue(zplResult.contains("^XA"), "Deve conter ^XA.");
        assertTrue(zplResult.contains("^XZ"), "Deve conter ^XZ.");
        assertTrue(zplResult.contains("COOKIE NUTELLA"), "Deve conter o nome do produto.");
        assertTrue(zplResult.contains("SETOR: CONFEITARIA"), "Deve conter o setor.");
        assertTrue(zplResult.contains("FAB.: 27/07/2026"), "Deve conter fabricação.");
        assertTrue(zplResult.contains("VAL.: 30/07/2026"), "Deve conter validade.");
        assertTrue(zplResult.contains("REG.: 00015"), "Deve conter o registro.");
        assertTrue(zplResult.contains("^FB340"), "Deve usar ^FB340 para centralização.");
    }
}
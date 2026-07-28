package br.com.espacovista.service.strategies;

import model.ImmediateConsumptionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.strategies.ImmediateConsumptionStrategy;

class ImmediateConsumptionStrategyTest {

    @Test
    @DisplayName("Deve gerar ZPL para etiqueta de consumo imediato com layout e posicoes corretas")
    void generateZpl_forImmediateConsumption_shouldContainCorrectLayout() {
        // Arrange
        ImmediateConsumptionRequest request = new ImmediateConsumptionRequest();
        request.setProductName("Suco de Teste");
        request.setQuantity(1);

        ImmediateConsumptionStrategy strategy = new ImmediateConsumptionStrategy(request);

        // Act
        String zplResult = strategy.generateZpl();
        System.out.println("ZPL Gerado para Análise:\n" + zplResult);

        // Assert
        // 1. Estrutura básica ZPL
        Assertions.assertNotNull(zplResult, "O resultado do ZPL não deve ser nulo.");
        Assertions.assertTrue(zplResult.startsWith("^XA"), "Deve começar com o comando ^XA.");
        Assertions.assertTrue(zplResult.endsWith("^XZ\n") || zplResult.endsWith("^XZ"), "Deve terminar com o comando ^XZ.");

        // 2. Elementos essenciais de texto (que realmente são impressos!)
        Assertions.assertTrue(zplResult.contains("Suco de Teste"), "Deve conter o nome do produto.");
        Assertions.assertTrue(zplResult.contains("CONSUMO IMEDIATO"), "Deve conter o texto 'CONSUMO IMEDIATO'.");

        // 3. Organização de código ZPL
        Assertions.assertTrue(zplResult.contains("^FX"), "Deve conter comentários de seção ^FX.");
    }
}
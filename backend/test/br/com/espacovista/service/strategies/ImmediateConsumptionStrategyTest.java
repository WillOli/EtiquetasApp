package br.com.espacovista.service.strategies;

import model.ImmediateConsumptionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.strategies.ImmediateConsumptionStrategy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class ImmediateConsumptionStrategyTest {

    @Test
    @DisplayName("Deve gerar ZPL para consumo imediato com produto, fabricacao automatica e validade")
    void generateZpl_forImmediateConsumption_shouldContainCorrectLayout() {
        // Arrange
        ImmediateConsumptionRequest request = new ImmediateConsumptionRequest();
        request.setProductName("Suco de Teste");
        request.setValidade("30/08/2026");
        request.setQuantity(1);

        ImmediateConsumptionStrategy strategy = new ImmediateConsumptionStrategy(request);

        // Act
        String zplResult = strategy.generateZpl();
        System.out.println("ZPL Gerado para Análise:\n" + zplResult);

        // Captura a data de hoje formatada no mesmo padrão do sistema (dd/MM/yyyy)
        String dataHojeEsperada = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Assert
        // 1. Estrutura básica ZPL
        Assertions.assertNotNull(zplResult, "O resultado do ZPL não deve ser nulo.");
        Assertions.assertTrue(zplResult.startsWith("^XA"), "Deve começar com o comando ^XA.");
        Assertions.assertTrue(zplResult.endsWith("^XZ\n") || zplResult.endsWith("^XZ"), "Deve terminar com o comando ^XZ.");

        // 2. Elementos essenciais e novos campos implementados
        Assertions.assertTrue(zplResult.contains("CONSUMO IMEDIATO"), "Deve conter o título 'CONSUMO IMEDIATO'.");
        Assertions.assertTrue(zplResult.contains("Produto: Suco de Teste"), "Deve conter o nome do produto formatado.");
        Assertions.assertTrue(zplResult.contains("Data de Fabricação: " + dataHojeEsperada), "Deve conter a data de fabricação automática do sistema.");
        Assertions.assertTrue(zplResult.contains("Validade: 30/08/2026"), "Deve conter a data de validade informada.");
    }
}
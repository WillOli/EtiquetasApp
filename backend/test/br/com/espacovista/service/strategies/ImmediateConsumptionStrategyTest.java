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
        model.ImmediateConsumptionRequest request = new ImmediateConsumptionRequest();
        request.setProductName("Suco de Teste");
        request.setQuantity(1);

        ImmediateConsumptionStrategy strategy = new ImmediateConsumptionStrategy(request);

        // Act
        String zplResult = strategy.generateZpl();
        System.out.println("ZPL Gerado para Análise:\n" + zplResult);

        // Assert
        // 1. Verifique se o ZPL não é nulo e se começa e termina corretamente
        Assertions.assertNotNull(zplResult, "O resultado do ZPL não deve ser nulo.");
        Assertions.assertTrue(zplResult.startsWith("^XA"), "Deve começar com o comando ^XA.");
        Assertions.assertTrue(zplResult.endsWith("^XZ\n"), "Deve terminar com o comando ^XZ.");

        // 2. Verifique se os textos principais estão presentes
        Assertions.assertTrue(zplResult.contains("^FDSuco de Teste^FS"), "Deve conter o nome do produto.");
        Assertions.assertTrue(zplResult.contains("^FDCONSUMO IMEDIATO^FS"), "Deve conter o texto 'CONSUMO IMEDIATO'.");
        Assertions.assertTrue(zplResult.contains("^FDProduto:^FS"), "Deve conter o rótulo 'Produto:'.");
        Assertions.assertTrue(zplResult.contains("^FDCod:^FS"), "Deve conter o rótulo 'Cod:'.");

        // 3. Verifique se a linha do código interno tem o formato de data correto (0000MMDD)
        Assertions.assertTrue(zplResult.matches("(?s).*\\^FD0000\\d{4}\\^FS.*"),
                "A linha do código interno deve ter o formato 0000MMDD correto.");

        // 4. Verifique se há comandos de moldura (^GB) presentes
        long gbCount = zplResult.lines().filter(line -> line.contains("^GB")).count();
        Assertions.assertTrue(gbCount >= 4, "Deve haver pelo menos 4 comandos ^GB (moldura + linhas divisórias).");

        // 5. Verifique se há comandos de centralização (^FB) para \"CONSUMO IMEDIATO\"
        Assertions.assertTrue(zplResult.contains("^FB"), "Deve conter comando ^FB para centralizar o texto 'CONSUMO IMEDIATO'.");

        // 6. Verifique se há comentários de seção (^FX) para organização
        Assertions.assertTrue(zplResult.contains("^FX"), "Deve conter comentários de seção ^FX.");
    }
}
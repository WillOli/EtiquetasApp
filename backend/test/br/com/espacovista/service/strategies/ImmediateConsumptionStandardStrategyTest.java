package br.com.espacovista.service.strategies;

import model.ImmediateConsumptionRequest;
import org.junit.jupiter.api.Test;
import service.strategies.ImmediateConsumptionStandardStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class ImmediateConsumptionStandardStrategyTest {

    @Test
    public void testGenerateZplForStandardColumns() {
        ImmediateConsumptionRequest request = new ImmediateConsumptionRequest();
        request.setProductName("Coxinha");
        request.setDataFabricacao("28/08/2026");
        request.setValidade("29/08/2026");
        request.setQuantity(2);

        ImmediateConsumptionStandardStrategy strategy = new ImmediateConsumptionStandardStrategy(request);

        // Testa a geração do conteúdo para a coluna 0 (esquerda) e coluna 1 (direita)
        String contentCol0 = strategy.generateLabelContent(0, 0);
        String contentCol1 = strategy.generateLabelContent(0, 1);

        assertNotNull(contentCol0);
        assertNotNull(contentCol1);

        assertTrue(contentCol0.contains("CONSUMO IMEDIATO"));
        assertTrue(contentCol0.contains("Produto: Coxinha"));

        assertTrue(contentCol1.contains("CONSUMO IMEDIATO"));
        assertTrue(contentCol1.contains("Produto: Coxinha"));
    }
}

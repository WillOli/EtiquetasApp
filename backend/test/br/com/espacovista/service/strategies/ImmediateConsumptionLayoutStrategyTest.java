package br.com.espacovista.service.strategies;

import model.ImmediateConsumptionRequest;
import org.junit.jupiter.api.Test;
import service.strategies.ImmediateConsumptionLayoutStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class ImmediateConsumptionLayoutStrategyTest {

    @Test
    public void testGenerateZplForSingleLabel() {
        ImmediateConsumptionRequest request = new ImmediateConsumptionRequest();
        request.setProductName("Bolo de Chocolate");
        request.setDataFabricacao("28/08/2026");
        request.setValidade("30/08/2026");
        request.setQuantity(1);

        ImmediateConsumptionLayoutStrategy strategy = new ImmediateConsumptionLayoutStrategy(request);
        String zpl = strategy.generateZpl();

        assertNotNull(zpl);
        assertTrue(zpl.contains("^XA"));
        assertTrue(zpl.contains("CONSUMO IMEDIATO"));
        assertTrue(zpl.contains("Produto: Bolo de Chocolate"));
        assertTrue(zpl.contains("Data de Fabricação: 28/08/2026"));
        assertTrue(zpl.contains("Validade: 30/08/2026"));
        assertTrue(zpl.contains("^XZ"));
    }
}

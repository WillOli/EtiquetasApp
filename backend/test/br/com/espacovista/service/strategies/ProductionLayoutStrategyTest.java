package br.com.espacovista.service.strategies;

import model.ProductionRequest;
import model.PrintRequest;
import org.junit.jupiter.api.Test;
import service.strategies.ProductionLayoutStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class ProductionLayoutStrategyTest {

    @Test
    public void testGenerateZplForProductionLabel() {
        ProductionRequest request = new ProductionRequest();
        request.setProductName("Frango Picado Temperado");
        request.setDataPreparacao("2026-08-31");
        request.setHorarioPreparo("14:30");
        request.setHorarioDescarte("18:30");
        request.setDataValidade("2026-09-05");
        request.setQuantity(1);
        request.setLabelType(PrintRequest.LabelType.SIXTY_TWO_MM);

        ProductionLayoutStrategy strategy = new ProductionLayoutStrategy(request);
        String zpl = strategy.generateZpl();

        assertNotNull(zpl);
        assertTrue(zpl.contains("^XA"));
        assertTrue(zpl.contains("^PW640"));
        assertTrue(zpl.contains("^LL240"));
        assertTrue(zpl.contains("ETIQUETA DE PRODUCAO"));
        assertTrue(zpl.contains("Produto: Frango Picado Temperado"));
        assertTrue(zpl.contains("Data Prep: 31/08/2026"));
        assertTrue(zpl.contains("Validade: 05/09/2026"));
        assertTrue(zpl.contains("Hora Prep: 14h 30min"));
        assertTrue(zpl.contains("Descarte: 18h 30min"));
        assertTrue(zpl.contains("^XZ"));
    }
}
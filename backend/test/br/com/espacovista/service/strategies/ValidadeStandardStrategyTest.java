package br.com.espacovista.service.strategies;

import model.ValidadePrintRequest;
import model.PrintRequest;
import org.junit.jupiter.api.Test;
import service.strategies.ValidadeStandardStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class ValidadeStandardStrategyTest {

    @Test
    public void generateZpl_forDoubleValidity_shouldGenerateTwoColumns() {
        ValidadePrintRequest request = new ValidadePrintRequest();
        request.setProductName("Bolo de Teste");
        request.setDataFabricacao("2026-08-01");
        request.setDataAbertura("2026-09-01");
        request.setDataValidade("2026-09-05");
        request.setQuantity(1);
        request.setLabelType(PrintRequest.LabelType.STANDARD);

        ValidadeStandardStrategy strategy = new ValidadeStandardStrategy(request);
        String zpl = strategy.generateZpl();

        assertNotNull(zpl);
        int occurrences = countOccurrences(zpl, "Bolo de Teste");
        assertEquals(2, occurrences, "O nome do produto deve aparecer duas vezes.");
    }

    private int countOccurrences(String str, String subStr) {
        if (subStr == null || subStr.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(subStr, idx)) != -1) {
            count++;
            idx += subStr.length();
        }
        return count;
    }
}
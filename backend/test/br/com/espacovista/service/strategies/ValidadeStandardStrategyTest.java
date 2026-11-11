package br.com.espacovista.service.strategies;

import model.ValidadePrintRequest;
import model.PrintRequest;
import org.junit.jupiter.api.Test;
import service.strategies.ValidadeStandardStrategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Test
    public void generateZpl_for25mmDoubleValidity_keepsFontProportionalAndWrapsLongTitle() {
        ValidadePrintRequest request = new ValidadePrintRequest();
        request.setProductName("Bolo recheado de chocolate com castanha e doce de leite");
        request.setDataFabricacao("2026-08-01");
        request.setDataAbertura("2026-09-01");
        request.setDataValidade("2026-09-05");
        request.setQuantity(1);
        request.setLabelType(PrintRequest.LabelType.DOUBLE_25X25);

        String zpl = new ValidadeStandardStrategy(request).generateZpl();
        Matcher title = Pattern.compile("\\^FO24,20\\^A0N,(\\d+),(\\d+)\\^FB152,(\\d+),")
                .matcher(zpl);

        assertTrue(title.find(), zpl);
        assertEquals(title.group(1), title.group(2), "Altura e largura da fonte precisam preservar a proporção.");
        assertTrue(Integer.parseInt(title.group(3)) > 1, "O título longo deve quebrar em linhas em vez de comprimir as letras.");
        assertTrue(Integer.parseInt(title.group(3)) <= 4, "O título deve caber no espaço vertical reservado.");

        Matcher date = Pattern.compile("\\^FO24,66\\^A0N,(\\d+),(\\d+)\\^FB152,1,").matcher(zpl);
        assertTrue(date.find(), zpl);
        assertEquals(date.group(1), date.group(2), "A fonte das datas também deve preservar a proporção.");
        assertTrue(Integer.parseInt(date.group(1)) >= 15, "A data deve usar a fonte maior definida para a etiqueta dupla.");
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

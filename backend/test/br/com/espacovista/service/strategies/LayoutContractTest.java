package br.com.espacovista.service.strategies;

import model.*;
import service.*;
import service.strategies.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.stream.*;
import static org.junit.jupiter.api.Assertions.*;

class LayoutContractTest {
    @TestFactory Stream<DynamicTest> simpleLayoutsPreserveEveryRecordForOddAndEvenQuantities() {
        return IntStream.of(1, 2, 3, 5, 50).boxed().flatMap(quantity -> Stream.of(false, true).map(standard ->
            DynamicTest.dynamicTest("Quantidade " + quantity + " standard " + standard, () -> {
                ILabelStrategy strategy = standard
                    ? new SimpleStandardStrategy("Pão de queijo", "Confeitaria", "2026-10-02", "2026-10-03", 100L, quantity)
                    : new SimpleLayoutStrategy("Pão de queijo", quantity, "Confeitaria", "2026-10-02", "2026-10-03", 100L);
                String zpl = strategy.generateZpl();
                int physical = standard ? quantity * 2 : quantity;
                List<String> records = java.util.regex.Pattern.compile("REG\\.: ([0-9]+)").matcher(zpl).results().map(m -> m.group(1)).toList();
                assertEquals(physical, records.size());
                for (int i = 0; i < physical; i++) assertEquals(String.format("%05d", 100 + i), records.get(i));
                assertTrue(zpl.contains("02/10/2026")); assertTrue(zpl.contains("03/10/2026"));
                assertTrue(zpl.contains("PÃO DE QUEIJO"));
                assertEquals(zpl, strategy.generateZpl(), "Renderizar novamente não pode consumir registros não reservados");
            })));
    }
    @TestFactory Stream<DynamicTest> factorySelectsEveryNonSequencedLayout() {
        return Arrays.stream(PrintRequest.LabelType.values()).flatMap(type -> Stream.of(1, 3).flatMap(quantity -> {
            ValidadePrintRequest v = new ValidadePrintRequest(); v.setQuantity(quantity); v.setLabelType(type); v.setProductName("Café");
            ImmediateConsumptionRequest i = new ImmediateConsumptionRequest(); i.setQuantity(quantity); i.setLabelType(type); i.setProductName("Café");
            return Stream.of(
                DynamicTest.dynamicTest("Validade " + type + " " + quantity, () -> {
                    var strategy = PrinterStrategyFactory.getStrategy(v);
                    assertEquals(type == PrintRequest.LabelType.STANDARD ? ValidadeStandardStrategy.class : ValidadeLayoutStrategy.class, strategy.getClass());
                    assertEquals(quantity, count(strategy.generateZpl(), "^XA"));
                    assertEquals(type == PrintRequest.LabelType.STANDARD ? quantity * 2 : quantity, count(strategy.generateZpl(), "Café"));
                }),
                DynamicTest.dynamicTest("Consumo " + type + " " + quantity, () -> {
                    var strategy = PrinterStrategyFactory.getStrategy(i);
                    assertEquals(type == PrintRequest.LabelType.STANDARD ? ImmediateConsumptionStandardStrategy.class : ImmediateConsumptionLayoutStrategy.class, strategy.getClass());
                    assertEquals(type == PrintRequest.LabelType.STANDARD ? quantity * 2 : quantity, count(strategy.generateZpl(), "Café"));
                })
            );
        }));
    }
    private int count(String text, String value) { return text.split(java.util.regex.Pattern.quote(value), -1).length - 1; }
}

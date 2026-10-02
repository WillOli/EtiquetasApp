package br.com.espacovista.service.strategies;

import model.ImmediateConsumptionRequest;
import model.ProductionRequest;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import service.strategies.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SingleLabelQuantityTest {
    @TestFactory Stream<DynamicTest> printsEveryRequestedLabel() {
        return IntStream.of(1, 2, 3, 5, 50).boxed().flatMap(quantity -> Stream.of(
            DynamicTest.dynamicTest("Produção " + quantity, () -> {
                ProductionRequest request = new ProductionRequest(); request.setQuantity(quantity);
                assertQuantity(new ProductionLayoutStrategy(request).generateZpl(), quantity);
            }),
            DynamicTest.dynamicTest("Consumo imediato " + quantity, () -> {
                ImmediateConsumptionRequest request = new ImmediateConsumptionRequest(); request.setQuantity(quantity);
                assertQuantity(new ImmediateConsumptionLayoutStrategy(request).generateZpl(), quantity);
            })
        ));
    }
    private void assertQuantity(String zpl, int expected) {
        assertEquals(expected, zpl.split("\\^XA", -1).length - 1);
        assertEquals(expected, zpl.split("\\^XZ", -1).length - 1);
    }
}

package service;

import model.*;
import org.junit.jupiter.api.*;
import service.strategies.*;
import validation.*;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class ZplSanitizerTest {
    @Test void preservesAccentsAndNeutralizesCommandsAndControls() {
        assertEquals("Pão açúcar café ç", ZplSanitizer.sanitize("  Pão\n açúcar\t cafe\u0301 ç  "));
        assertEquals("FS XZ JA & < > _5E", ZplSanitizer.sanitize("^FS^XZ~JA\\& < > _5E\u0000\u001B"));
    }

    @TestFactory Stream<DynamicTest> everyLayoutKeepsTheSameCommandStructure() {
        return Stream.of("^FS^XZ^XA^FO1,1", "~JA^PQ999", "café\\&\nç\u0000", "^CC!~CT!", "Pão".repeat(40))
            .flatMap(attack -> {
                List<ILabelStrategy> attacked = strategies(attack);
                List<ILabelStrategy> reference = strategies("Produto");
                List<DynamicTest> tests = new ArrayList<>();
                for (int i = 0; i < attacked.size(); i++) {
                    int index = i;
                    tests.add(DynamicTest.dynamicTest(attacked.get(i).getClass().getSimpleName() + " " + attack, () -> {
                        String zpl = attacked.get(index).generateZpl();
                        String baseline = reference.get(index).generateZpl();
                        assertEquals(commands(baseline), commands(zpl));
                        assertFalse(zpl.contains("~"));
                        assertFalse(zpl.contains("\\&"));
                        assertFalse(zpl.contains("\u0000"));
                    }));
                }
                return tests.stream();
            });
    }
    private List<String> commands(String zpl) {
        return java.util.regex.Pattern.compile("\\^[A-Z0-9]{2}").matcher(zpl).results().map(r -> r.group()).toList();
    }
    private List<ILabelStrategy> strategies(String value) {
        ValidadePrintRequest v = new ValidadePrintRequest();
        v.setProductName(value); v.setQuantity(1); v.setDataFabricacao(value); v.setDataAbertura(value); v.setDataValidade(value);
        ImmediateConsumptionRequest i = new ImmediateConsumptionRequest();
        i.setProductName(value); i.setQuantity(1); i.setDataFabricacao(value); i.setValidade(value);
        ProductionRequest p = new ProductionRequest();
        p.setProductName(value); p.setQuantity(1); p.setDataPreparacao(value); p.setDataValidade(value); p.setHorarioPreparo(value); p.setHorarioDescarte(value);
        return List.of(new SimpleLayoutStrategy(value, 1, value, value, value, 1),
            new SimpleStandardStrategy(value, value, value, value, 1L, 1),
            new ValidadeLayoutStrategy(v), new ValidadeStandardStrategy(v),
            new ImmediateConsumptionLayoutStrategy(i), new ImmediateConsumptionStandardStrategy(i), new ProductionLayoutStrategy(p));
    }
    @Test void rejectsOversizeProductAndSector() {
        PrintRequest r = new PrintRequest(); r.setText("a".repeat(81)); r.setSetor("Cozinha");
        assertThrows(RequestValidationException.class, () -> PrintRequestValidator.validate(r));
        r.setText("Pão"); r.setSetor("a".repeat(41));
        assertThrows(RequestValidationException.class, () -> PrintRequestValidator.validate(r));
        r.setText("^~\\\n");
        assertThrows(RequestValidationException.class, () -> PrintRequestValidator.validate(r));
    }
}

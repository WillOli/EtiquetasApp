package service;

import model.LabelStyle;
import model.ImmediateConsumptionRequest;
import model.PrintRequest;
import model.ProductionRequest;
import model.ValidadePrintRequest;
import org.junit.jupiter.api.Test;
import service.strategies.ImmediateConsumptionStandardStrategy;
import service.strategies.ProductionLayoutStrategy;
import service.strategies.SimpleStandardStrategy;
import service.strategies.ValidadeStandardStrategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZplStyleApplicatorTest {
    @Test
    void rightAlignmentUsesLaboratoryHorizontalOffset() {
        String styled = apply("right", "^XA\n^PW400\n^LL200\n"
                + "^FO20,20^A0N,20,20^FB360,1,0,C,0^FDProduto^FS\n^XZ", 5);

        assertTrue(styled.contains("^PW400"), styled);
        assertTrue(styled.contains("^LS40^LT0"), styled);
        assertTrue(styled.contains("^FO0,20^A0N,20,20^FB400,1,0,R,0^FDProduto^FS"), styled);
    }

    @Test
    void centerAlignmentUsesPhysicalCenterOnHorizontalSingleLabel() {
        String styled = apply("center", "^XA\n^PW400\n^LL200\n"
                + "^FO20,20^A0N,20,20^FB360,1,0,L,0^FDProduto^FS\n^XZ");

        assertTrue(styled.contains("^FO0,20^A0N,20,20^FB400,1,0,C,0^FDProduto^FS"), styled);
    }

    @Test
    void leftAlignmentStartsAtLabelEdgeOnHorizontalSingleLabel() {
        String styled = apply("left", "^XA\n^PW400\n^LL200\n"
                + "^FO20,20^A0N,20,20^FB360,1,0,C,0^FDProduto^FS\n^XZ");

        assertTrue(styled.contains("^FO0,20^A0N,20,20^FB400,1,0,L,0^FDProduto^FS"), styled);
    }

    @Test
    void rightAlignmentPreservesIndependentDoubleLabelColumns() {
        String styled = apply("right", "^XA\n^PW420\n^LL200\n"
                + "^FO218,20^A0N,20,20^FB184,1,0,C,0^FDProduto^FS\n^XZ");

        assertTrue(styled.contains("^FO234,20^A0N,20,20^FB152,1,0,R,0^FDProduto^FS"), styled);
    }

    @Test
    void validityDoubleAppliesLeftAlignmentToAllPositionOneFieldsOnly() {
        ValidadePrintRequest request = new ValidadePrintRequest();
        request.setProductName("Bolo de teste");
        request.setDataFabricacao("2026-08-01");
        request.setDataAbertura("2026-08-02");
        request.setDataValidade("2026-08-03");
        request.setQuantity(1);
        request.setLabelType(PrintRequest.LabelType.DOUBLE_25X25);

        LabelStyle style = new LabelStyle();
        style.setFirstLabelAlignment("left");
        style.setSecondLabelAlignment("right");
        String raw = new ValidadeStandardStrategy(request).generateZpl();
        String styled = ZplStyleApplicator.apply(raw, style);

        assertEquals(4, countFields(styled, 24, 'L'), styled);
        assertEquals(4, countFields(styled, 234, 'R'), styled);
    }

    @Test
    void validityDoubleAppliesTheSameAlignmentWithinBothLabelMargins() {
        for (String alignment : new String[] {"left", "center", "right"}) {
            ValidadePrintRequest request = new ValidadePrintRequest();
            request.setProductName("Produto para alinhamento");
            request.setDataFabricacao("2026-08-01");
            request.setDataAbertura("2026-08-02");
            request.setDataValidade("2026-08-03");
            request.setQuantity(1);
            request.setLabelType(PrintRequest.LabelType.DOUBLE_25X25);

            LabelStyle style = new LabelStyle();
            style.setFirstLabelAlignment(alignment);
            style.setSecondLabelAlignment(alignment);
            String styled = ZplStyleApplicator.apply(new ValidadeStandardStrategy(request).generateZpl(), style);
            char code = alignment.charAt(0) == 'l' ? 'L' : alignment.charAt(0) == 'r' ? 'R' : 'C';

            assertEquals(4, countFields(styled, 24, code), "Posição 1: " + alignment + "\n" + styled);
            assertEquals(4, countFields(styled, 234, code), "Posição 2: " + alignment + "\n" + styled);
            assertTrue(styled.contains("^FB152,"), "A caixa conserva 24 dots de margem em cada etiqueta.\n" + styled);
        }
    }

    @Test
    void every25MmDoubleModeUsesOneAlignmentForBothPhysicalLabels() {
        String[] modes = {"simple", "validity", "immediate", "production"};
        int[] fieldsPerLabel = {5, 4, 4, 6};
        for (int modeIndex = 0; modeIndex < modes.length; modeIndex++) {
            for (String alignment : new String[] {"left", "center", "right"}) {
                LabelStyle style = new LabelStyle();
                style.setAlignment(alignment);
                style.setFirstLabelAlignment(alignment);
                style.setSecondLabelAlignment(alignment);
                String styled = ZplStyleApplicator.apply(doubleLabelZpl(modes[modeIndex]), style);
                char code = alignment.equals("left") ? 'L' : alignment.equals("right") ? 'R' : 'C';

                assertEquals(fieldsPerLabel[modeIndex], countFields(styled, 24, code),
                        modes[modeIndex] + " posição 1, " + alignment + "\n" + styled);
                assertEquals(fieldsPerLabel[modeIndex], countFields(styled, 234, code),
                        modes[modeIndex] + " posição 2, " + alignment + "\n" + styled);
            }
        }
    }

    @Test
    void legacy40MmDoubleKeepsBothTextBlocksInsideTheirLabelEdges() {
        String raw = new SimpleStandardStrategy("Produto", "Setor", "2026-08-01", "2026-08-02", 1L, 1).generateZpl();
        LabelStyle style = new LabelStyle();
        style.setAlignment("right");

        String styled = ZplStyleApplicator.apply(raw, style);
        assertEquals(5, countFields(styled, 8, 304, 'R'), styled);
        assertEquals(5, countFields(styled, 338, 304, 'R'), styled);
        assertTrue(styled.contains("^FB304,"), styled);
    }

    @Test
    void legacy40MmDoubleValidityAndImmediateLabelsShareTheSameAlignment() {
        ValidadePrintRequest validity = new ValidadePrintRequest();
        validity.setProductName("Produto"); validity.setDataFabricacao("2026-08-01");
        validity.setDataAbertura("2026-08-01"); validity.setDataValidade("2026-08-02");
        validity.setQuantity(1); validity.setLabelType(PrintRequest.LabelType.STANDARD);
        ImmediateConsumptionRequest immediate = new ImmediateConsumptionRequest();
        immediate.setProductName("Produto"); immediate.setDataFabricacao("2026-08-01");
        immediate.setValidade("2026-08-02"); immediate.setQuantity(1);
        immediate.setLabelType(PrintRequest.LabelType.STANDARD);
        LabelStyle style = new LabelStyle();
        style.setAlignment("left");

        String validityZpl = ZplStyleApplicator.apply(new ValidadeStandardStrategy(validity).generateZpl(), style);
        String immediateZpl = ZplStyleApplicator.apply(new ImmediateConsumptionStandardStrategy(immediate).generateZpl(), style);
        assertEquals(4, countFields(validityZpl, 8, 304, 'L'), validityZpl);
        assertEquals(4, countFields(validityZpl, 338, 304, 'L'), validityZpl);
        assertEquals(4, countFields(immediateZpl, 8, 304, 'L'), immediateZpl);
        assertEquals(4, countFields(immediateZpl, 338, 304, 'L'), immediateZpl);
    }

    private String doubleLabelZpl(String mode) {
        if (mode.equals("simple")) {
            return new SimpleStandardStrategy("Produto", "Setor", "2026-08-01", "2026-08-02", 1L, 1, 25).generateZpl();
        }
        if (mode.equals("validity")) {
            ValidadePrintRequest request = new ValidadePrintRequest();
            request.setProductName("Produto"); request.setDataFabricacao("2026-08-01");
            request.setDataAbertura("2026-08-01"); request.setDataValidade("2026-08-02");
            request.setQuantity(1); request.setLabelType(PrintRequest.LabelType.DOUBLE_25X25);
            return new ValidadeStandardStrategy(request).generateZpl();
        }
        if (mode.equals("immediate")) {
            ImmediateConsumptionRequest request = new ImmediateConsumptionRequest();
            request.setProductName("Produto"); request.setDataFabricacao("2026-08-01");
            request.setValidade("2026-08-02"); request.setQuantity(1);
            request.setLabelType(PrintRequest.LabelType.DOUBLE_25X25);
            return new ImmediateConsumptionStandardStrategy(request).generateZpl();
        }
        ProductionRequest request = new ProductionRequest();
        request.setProductName("Produto"); request.setDataPreparacao("2026-08-01");
        request.setHorarioPreparo("10:00"); request.setHorarioDescarte("12:00");
        request.setDataValidade("2026-08-02"); request.setQuantity(1);
        request.setLabelType(PrintRequest.LabelType.DOUBLE_25X25);
        return new ProductionLayoutStrategy(request).generateZpl();
    }

    private int countFields(String zpl, int x, char alignment) {
        Matcher fields = Pattern.compile("\\^FO" + x + ",\\d+\\^A[^\\^]+\\^FB152,\\d+,0," + alignment + ",0\\^FD")
                .matcher(zpl);
        int count = 0;
        while (fields.find()) count++;
        return count;
    }

    private int countFields(String zpl, int x, int width, char alignment) {
        Matcher fields = Pattern.compile("\\^FO" + x + ",\\d+\\^A[^\\^]+\\^FB" + width + ",\\d+,0," + alignment + ",0\\^FD")
                .matcher(zpl);
        int count = 0;
        while (fields.find()) count++;
        return count;
    }

    @Test
    void rightAlignmentExpandsLegacyWideLabelInsteadOfKeepingOldLargeInset() {
        String styled = apply("right", "^XA\n^PW824\n^LL240\n"
                + "^FO40,20^A0N,20,20^FB744,1,0,C,0^FDProduto^FS\n^XZ");

        assertTrue(styled.contains("^FO40,20^A0N,20,20^FB784,1,0,R,0^FDProduto^FS"), styled);
    }

    @Test
    void rightAlignmentAddsBoundedRightAlignedBlocksToLegacyUnalignedFields() {
        String styled = apply("right", "^XA\n^PW640\n^LL240\n"
                + "^FO330,60^A0N,20,20^FDValidade^FS\n^XZ");

        assertTrue(styled.contains("^FO330,60^A0N,20,20^FB300,1,0,R,0^FDValidade^FS"), styled);
    }

    private String apply(String alignment, String zpl) {
        return apply(alignment, zpl, 0);
    }

    private String apply(String alignment, String zpl, double offsetXmm) {
        LabelStyle style = new LabelStyle();
        style.setAlignment(alignment);
        style.setOffsetXmm(offsetXmm);
        return ZplStyleApplicator.apply(zpl, style);
    }
}

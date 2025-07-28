package service.strategies;

import model.ValidadePrintRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static service.ZplConstants.*;

public class ValidadeStandardStrategy implements ILabelStrategy {
    private final ValidadePrintRequest request;
    private final int actualQuantity;

    public ValidadeStandardStrategy(ValidadePrintRequest request) {
        this.request = request;
        this.actualQuantity = request.getQuantity() * 2;
    }

    @Override
    public String generateZpl() {
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate manufacturingDate = LocalDate.parse(request.getMfgDate());
        String formattedMfgDate = manufacturingDate.format(displayFormatter);
        String prazoText = request.getValidityDays() + " dias";
        String productName = request.getProductName();

        int pageWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM * 2 + GAP_HORIZONTAL_DOTS * 2;
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM;
        int fontSize = 20;
        int textMargin = 15;
        int valueMargin = 110;

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < this.actualQuantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n^CI28\n^PW").append(pageWidthDots).append("\n^LL").append(labelHeightDots).append("\n");
            }

            for (int col = 0; col < 2; col++) {
                int currentX = col * (labelWidthDots + GAP_HORIZONTAL_DOTS);

                // Deslocamento para a etiqueta da esquerda
                int additionalOffsetXLeft = 0;
                if (col == 0) {
                    additionalOffsetXLeft = 20;
                }

                // Deslocamento para a etiqueta da direita
                int additionalOffsetXRight = 0;
                if (col == 1) {
                    additionalOffsetXRight = 20; // Valor negativo para mover para a esquerda
                }

                // --- Linha 1: Produto ---
                int yPosProduto = 30;
                zplBuilder.append("^FO").append(currentX + additionalOffsetXLeft + additionalOffsetXRight + textMargin).append(",").append(yPosProduto).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDProduto:^FS\n");
                zplBuilder.append("^FO").append(currentX + additionalOffsetXLeft + additionalOffsetXRight + valueMargin).append(",").append(yPosProduto).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(productName).append("^FS\n");

                // --- Linha 2: Fabricação ---
                int yPosFabricacao = 80;
                zplBuilder.append("^FO").append(currentX + additionalOffsetXLeft + additionalOffsetXRight + textMargin).append(",").append(yPosFabricacao).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDFab:^FS\n");
                zplBuilder.append("^FO").append(currentX + additionalOffsetXLeft + additionalOffsetXRight + valueMargin).append(",").append(yPosFabricacao).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(formattedMfgDate).append("^FS\n");

                // --- Linha 3: Validade ---
                int yPosValidade = 130;
                zplBuilder.append("^FO").append(currentX + additionalOffsetXLeft + additionalOffsetXRight + textMargin).append(",").append(yPosValidade).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDValidade:^FS\n");
                zplBuilder.append("^FO").append(currentX + additionalOffsetXLeft + additionalOffsetXRight + valueMargin).append(",").append(yPosValidade).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(prazoText).append("^FS\n");
            }

            if (i % 2 == 1 || i == this.actualQuantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }
}
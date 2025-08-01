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
        int fontSize = 22;
        int textMargin = 15;
        int valueMargin = 130;
        int baseLineWidth = labelWidthDots - (textMargin * 2);

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < this.actualQuantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n^CI28\n^PW").append(pageWidthDots).append("\n^LL").append(labelHeightDots).append("\n");
            }

            int col = i % 2;
            int currentX = col * (labelWidthDots + GAP_HORIZONTAL_DOTS);
            int offsetX = 5;

            // ======================= LÓGICA DE COMPENSAÇÃO =======================
            // Variáveis que podem mudar dependendo da etiqueta (esquerda ou direita)
            int currentLineWidth = baseLineWidth;
            int currentValueMargin = valueMargin;

            if (col == 1) { // Se for a etiqueta da DIREITA...
                // ...aumentamos um pouco as dimensões horizontais para compensar o "achatamento".
                // Você pode ajustar estes valores se necessário.
                currentLineWidth = baseLineWidth + 10; // Linha um pouco mais longa
                currentValueMargin = valueMargin + 10; // Valor um pouco mais para a direita
            }
            // =====================================================================

            int yPos = 25;
            int lineSpacing = 50;

            // --- Linha 1: Produto ---
            zplBuilder.append("^FO").append(currentX + offsetX + textMargin).append(",").append(yPos).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDProduto:^FS\n");
            zplBuilder.append("^FO").append(currentX + offsetX + currentValueMargin).append(",").append(yPos).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(productName).append("^FS\n");
            zplBuilder.append("^FO").append(currentX + offsetX + textMargin).append(",").append(yPos + fontSize + 5).append("^GB").append(currentLineWidth).append(",2,2^FS\n");
            yPos += lineSpacing;

            // --- Linha 2: Fabricação ---
            zplBuilder.append("^FO").append(currentX + offsetX + textMargin).append(",").append(yPos).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDFabricacao:^FS\n");
            zplBuilder.append("^FO").append(currentX + offsetX + currentValueMargin).append(",").append(yPos).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(formattedMfgDate).append("^FS\n");
            zplBuilder.append("^FO").append(currentX + offsetX + textMargin).append(",").append(yPos + fontSize + 5).append("^GB").append(currentLineWidth).append(",2,2^FS\n");
            yPos += lineSpacing;

            // --- Linha 3: Validade ---
            zplBuilder.append("^FO").append(currentX + offsetX + textMargin).append(",").append(yPos).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDValidade:^FS\n");
            zplBuilder.append("^FO").append(currentX + offsetX + currentValueMargin).append(",").append(yPos).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(prazoText).append("^FS\n");
            zplBuilder.append("^FO").append(currentX + offsetX + textMargin).append(",").append(yPos + fontSize + 5).append("^GB").append(currentLineWidth).append(",2,2^FS\n");

            if (i % 2 == 1 || i == this.actualQuantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }
}
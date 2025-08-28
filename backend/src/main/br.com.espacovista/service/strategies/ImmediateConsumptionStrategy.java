package service.strategies;

import model.ImmediateConsumptionRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static service.ZplConstants.*;

public class ImmediateConsumptionStrategy extends AbstractTwoColumnStrategy {
    private final ImmediateConsumptionRequest request;

    public ImmediateConsumptionStrategy(ImmediateConsumptionRequest request) {
        super(request.getQuantity());
        this.request = request;
    }

    @Override
    protected String generateLabelContent(int startX, int column) {
        String productName = request.getProductName();
        String internalDateCode = generateInternalDateCode();
        
        int fontSize = 22;
        int textMargin = 15;
        int valueMargin = 130;
        int baseLineWidth = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM - (textMargin * 2);
        int offsetX = 5;

        // Lógica de compensação para a coluna da direita
        int currentLineWidth = baseLineWidth;
        int currentValueMargin = valueMargin;
        if (column == 1) {
            currentLineWidth += 10;
            currentValueMargin += 10;
        }

        int yPos = 25;
        int lineSpacing = 50;

        StringBuilder contentBuilder = new StringBuilder();

        // --- Linha 1: Produto ---
        contentBuilder.append(generateLine("Produto:", productName, startX + offsetX, yPos, fontSize, textMargin, currentValueMargin, currentLineWidth));
        yPos += lineSpacing;

        // --- Linha 2: Consumo Imediato ---
        contentBuilder.append(generateLine("", "CONSUMO IMEDIATO", startX + offsetX, yPos, fontSize + 2, textMargin, textMargin, currentLineWidth));
        yPos += lineSpacing;

        // --- Linha 3: Código interno (invisível ao consumidor) ---
        contentBuilder.append(generateLine("Cod:", internalDateCode, startX + offsetX, yPos, 12, textMargin, currentValueMargin, currentLineWidth));

        return contentBuilder.toString();
    }

    /**
     * Gera código interno de data no formato 0000MMDD
     * Os primeiros 4 zeros são constantes, seguidos do mês e dia atuais
     */
    private String generateInternalDateCode() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        return "0000" + today.format(formatter);
    }

    private String generateLine(String label, String value, int startX, int yPos, int fontSize, int textMargin, int valueMargin, int lineWidth) {
        StringBuilder line = new StringBuilder();
        
        if (!label.isEmpty()) {
            line.append(String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", startX + textMargin, yPos, fontSize, fontSize, label));
        }
        
        line.append(String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", startX + valueMargin, yPos, fontSize, fontSize, value));
        line.append(String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + textMargin, yPos + fontSize + 5, lineWidth));
        
        return line.toString();
    }
}
package service.strategies;
import static service.ZplSanitizer.sanitize;

import model.ValidadePrintRequest;
import static service.ZplConstants.*;

public class ValidadeStandardStrategy extends AbstractTwoColumnStrategy {

    private final ValidadePrintRequest request;

    public ValidadeStandardStrategy(ValidadePrintRequest request) {
        super(request.getQuantity());
        this.request = request;
    }

    @Override
    protected String generateLabelContent(int startX, int column) {
        String formattedMfgDate = formatData(request.getDataFabricacao());
        String formattedOpenDate = formatData(request.getDataAbertura());
        String formattedValDate = formatData(request.getDataValidade());
        String productName = sanitize(request.getProductName());

        int fontSize = 16;
        int textMargin = 8;
        int valueMargin = 115;
        int offsetX = 5;

        // Deslocamento de 12 dots (~1.5mm) aplicado apenas na coluna da direita (column == 1)
        int columnAdjustment = (column == 1) ? 12 : 0;
        int adjustedStartX = startX + offsetX + columnAdjustment;

        int currentValueMargin = valueMargin;
        if (column == 1) {
            currentValueMargin += 10;
        }

        int yPos = 12;
        int lineSpacing = 32;

        StringBuilder contentBuilder = new StringBuilder();

        // --- Linha 1: Produto ---
        contentBuilder.append(generateLine("Produto:", productName, adjustedStartX, yPos, fontSize, textMargin, currentValueMargin));
        yPos += lineSpacing;

        // --- Linha 2: Fabricação ---
        contentBuilder.append(generateLine("Fabricacao:", formattedMfgDate, adjustedStartX, yPos, fontSize, textMargin, currentValueMargin));
        yPos += lineSpacing;

        // --- Linha 3: Abertura ---
        contentBuilder.append(generateLine("Abertura:", formattedOpenDate, adjustedStartX, yPos, fontSize, textMargin, currentValueMargin));
        yPos += lineSpacing;

        // --- Linha 4: Validade (após aberto) ---
        contentBuilder.append(generateLine("Val. Aberto:", formattedValDate, adjustedStartX, yPos, fontSize, textMargin, currentValueMargin));

        return contentBuilder.toString();
    }

    private String generateLine(String label, String value, int startX, int yPos, int fontSize, int textMargin, int valueMargin) {
        return String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", startX + textMargin, yPos, fontSize, fontSize, label) +
                String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", startX + valueMargin, yPos, fontSize, fontSize, sanitize(value));
    }

    private String formatData(String dateStr) {
        if (dateStr == null || !dateStr.contains("-")) return dateStr != null ? dateStr : "";
        String[] p = dateStr.split("-");
        if (p.length == 3) {
            return p[2] + "/" + p[1] + "/" + p[0];
        }
        return dateStr;
    }
}
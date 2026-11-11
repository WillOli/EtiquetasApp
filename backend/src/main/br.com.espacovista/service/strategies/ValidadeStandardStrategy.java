package service.strategies;
import static service.ZplSanitizer.sanitize;

import model.ValidadePrintRequest;
import static service.ZplConstants.*;

public class ValidadeStandardStrategy extends AbstractTwoColumnStrategy {

    private final ValidadePrintRequest request;

    public ValidadeStandardStrategy(ValidadePrintRequest request) {
        super(request.getQuantity(), widthOf(request), 25);
        this.request = request;
    }

    @Override
    protected String generateLabelContent(int startX, int column) {
        if (widthOf(request) == 25) return generateCompactLabel(startX);
        String formattedMfgDate = formatData(request.getDataFabricacao());
        String formattedOpenDate = formatData(request.getDataAbertura());
        String formattedValDate = formatData(request.getDataValidade());
        String productName = sanitize(request.getProductName());

        int contentX = startX + 8;
        int contentWidth = 304;
        int productFontSize = productName.length() > 18
                ? Math.max(12, (int) (contentWidth * 0.75 / ((productName.length() + 9) * 0.62)))
                : 22;

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(generateLine(contentX, 12, contentWidth, productFontSize, "Produto: " + productName));
        contentBuilder.append(generateLine(contentX, 58, contentWidth, 22, "Fab.: " + formattedMfgDate));
        contentBuilder.append(generateLine(contentX, 104, contentWidth, 22, "Abert.: " + formattedOpenDate));
        contentBuilder.append(generateLine(contentX, 150, contentWidth, 22, "Val.: " + formattedValDate));

        return contentBuilder.toString();
    }

    private String generateCompactLabel(int startX) {
        String product = sanitize(request.getProductName());
        // Mantém a mesma área útil e margem da etiqueta dupla simples (25 × 25 mm).
        int x = startX + 24;
        int width = 152;
        String[] lines = {
                product,
                "Fab " + formatData(request.getDataFabricacao()),
                "Abert " + formatData(request.getDataAbertura()),
                "Val " + formatData(request.getDataValidade())
        };
        // Afastar o título do corte superior e manter intervalo entre as quatro linhas.
        int[] y = {20, 66, 112, 158};
        StringBuilder zpl = new StringBuilder();
        int fontScale = request.getStyle() == null ? 100 : request.getStyle().getFontScale();
        int titleMaximumFont = Math.max(10, (int) Math.floor(24 * 100.0 / fontScale));
        int detailMaximumFont = Math.max(10, (int) Math.floor(18 * 100.0 / fontScale));
        zpl.append("^FXVALIDITY_DOUBLE^FS\n");
        for (int i = 0; i < lines.length; i++) {
            int font = i == 0 ? fitTitleFont(lines[i], titleMaximumFont, fontScale, width)
                    : fitFont(lines[i], detailMaximumFont, fontScale, width);
            int lineCount = i == 0 ? fitTitleLineCount(lines[i], font, fontScale, width) : 1;
            zpl.append(String.format("^FO%d,%d^A0N,%d,%d^FB%d,%d,0,L,0^FD%s^FS\n",
                    x, y[i], font, font, width, lineCount, sanitize(lines[i])));
        }
        return zpl.toString();
    }

    private static int widthOf(ValidadePrintRequest request) {
        return request.getLabelType() == null ? 40 : request.getLabelType().getWidthMm();
    }

    private int fitFont(String value, int maximum, int fontScale, int width) {
        if (value == null || value.isBlank()) return maximum;
        int printedFont = (int) (width * 0.93 / (value.length() * 0.58));
        int estimated = (int) Math.round(printedFont * 100.0 / fontScale);
        return Math.max(8, Math.min(maximum, estimated));
    }

    private int fitTitleFont(String value, int maximum, int fontScale, int width) {
        if (value == null || value.isBlank()) return maximum;
        for (int font = maximum; font >= 8; font--) {
            int printedFont = Math.max(8, (int) Math.round(font * fontScale / 100.0));
            int lines = fitTitleLineCount(value, font, fontScale, width);
            if (lines <= 4 && lines * printedFont <= 44) return font;
        }
        return 8;
    }

    private int fitTitleLineCount(String value, int font, int fontScale, int width) {
        if (value == null || value.isBlank()) return 1;
        int printedFont = Math.max(8, (int) Math.round(font * fontScale / 100.0));
        return Math.max(1, (int) Math.ceil(value.length() * printedFont * 0.58 / (width * 0.93)));
    }

    private String generateLine(int x, int y, int width, int fontSize, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,L,0^FD%s^FS\n", x, y, fontSize, fontSize, width, sanitize(text));
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

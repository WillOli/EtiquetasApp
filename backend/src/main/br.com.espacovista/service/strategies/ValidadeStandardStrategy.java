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

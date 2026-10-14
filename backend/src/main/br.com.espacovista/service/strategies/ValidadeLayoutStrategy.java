package service.strategies;
import static service.ZplSanitizer.sanitize;

import model.ValidadePrintRequest;
import static service.ZplConstants.*;

public class ValidadeLayoutStrategy implements ILabelStrategy {
    private final ValidadePrintRequest request;

    public ValidadeLayoutStrategy(ValidadePrintRequest request) {
        this.request = request;
    }

    @Override
    public String generateZpl() {
        String formattedMfgDate = formatData(request.getDataFabricacao());
        String formattedOpenDate = formatData(request.getDataAbertura());
        String formattedValDate = formatData(request.getDataValidade());

        int fontSize = 22;
        int contentStart = 15;
        int contentWidth = 610;
        String productName = sanitize(request.getProductName());
        int productFontSize = productName.length() > 36
                ? Math.max(8, (int) Math.floor(contentWidth * 0.75 / (productName.length() * 0.62)))
                : fontSize;

        StringBuilder zplBuilder = new StringBuilder();
        int quantity = request.getQuantity() > 0 ? request.getQuantity() : 1;

        for (int i = 0; i < quantity; i++) {
            zplBuilder.append("^XA\n^CI28\n^PW").append(LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM).append("\n^LL").append(LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM).append("\n");

            // Mantém o título e cada linha dentro de uma única coluna de largura total.
            appendLine(zplBuilder, contentStart, 18, fontSize, contentWidth,
                    "ETIQUETA DE VALIDADE");
            appendLine(zplBuilder, contentStart, 52, productFontSize, contentWidth,
                    "Produto: " + productName);
            appendLine(zplBuilder, contentStart, 84, fontSize, contentWidth,
                    "Fabricacao: " + sanitize(formattedMfgDate));
            appendLine(zplBuilder, contentStart, 116, fontSize, contentWidth,
                    "Data Abertura: " + sanitize(formattedOpenDate));
            appendLine(zplBuilder, contentStart, 148, fontSize, contentWidth,
                    "Validade (aberto): " + sanitize(formattedValDate));

            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }

    private void appendLine(StringBuilder zpl, int x, int y, int fontSize, int width, String text) {
        zpl.append(String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,L,0^FD%s^FS\n",
                x, y, fontSize, fontSize, width, sanitize(text)));
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

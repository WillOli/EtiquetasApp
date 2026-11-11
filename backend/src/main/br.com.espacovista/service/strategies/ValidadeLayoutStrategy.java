package service.strategies;
import static service.ZplSanitizer.sanitize;

import model.ValidadePrintRequest;
import static service.ZplConstants.*;

public class ValidadeLayoutStrategy implements ILabelStrategy {
    private final ValidadePrintRequest request;
    private final int sizedWidthMm;
    private final int sizedHeightMm;

    public ValidadeLayoutStrategy(ValidadePrintRequest request) {
        this.request = request;
        this.sizedWidthMm = 0;
        this.sizedHeightMm = 0;
    }

    public ValidadeLayoutStrategy(ValidadePrintRequest request, int widthMm, int heightMm) {
        this.request = request;
        this.sizedWidthMm = widthMm;
        this.sizedHeightMm = heightMm;
    }

    @Override
    public String generateZpl() {
        if (sizedWidthMm > 0 && sizedHeightMm > 0) return generateSizedZpl();
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

    private String generateSizedZpl() {
        int pageWidth = sizedWidthMm * DOTS_PER_MM;
        int pageHeight = sizedHeightMm * DOTS_PER_MM;
        int x = Math.max(8, pageWidth / 20);
        int width = pageWidth - 2 * x;
        int inset = Math.max(8, pageHeight / 20);
        int rowHeight = (pageHeight - 2 * inset) / 5;
        String product = sanitize(request.getProductName());
        String[] lines = {
                "ETIQUETA DE VALIDADE",
                "Produto: " + product,
                (sizedWidthMm <= 30 ? "Fab.: " : "Fabricação: ") + formatData(request.getDataFabricacao()),
                (sizedWidthMm <= 30 ? "Abert.: " : "Abertura: ") + formatData(request.getDataAbertura()),
                (sizedWidthMm <= 30 ? "Val.: " : "Validade: ") + formatData(request.getDataValidade())
        };
        StringBuilder zpl = new StringBuilder();
        int quantity = request.getQuantity() > 0 ? request.getQuantity() : 1;
        for (int copy = 0; copy < quantity; copy++) {
            zpl.append("^XA\n^CI28\n^PW").append(pageWidth).append("\n^LL").append(pageHeight).append('\n');
            for (int row = 0; row < lines.length; row++) {
                int maxFont = Math.min(24, Math.max(12, rowHeight - 8));
                int font = fitFont(lines[row], maxFont, width);
                zpl.append(String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,L,0^FD%s^FS\n",
                        x, inset + row * rowHeight, font, font, width, sanitize(lines[row])));
            }
            zpl.append("^XZ\n");
        }
        return zpl.toString();
    }

    private int fitFont(String value, int maximum, int width) {
        if (value == null || value.isBlank()) return maximum;
        int estimated = (int) (width * 0.88 / (value.length() * 0.62));
        return Math.max(10, Math.min(maximum, estimated));
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

package service.strategies;

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

        int fontSize = 22; // Fonte otimizada para caber sem apertar
        int textMargin = 15;
        int valueMargin = 320;

        StringBuilder zplBuilder = new StringBuilder();
        int quantity = request.getQuantity() > 0 ? request.getQuantity() : 1;

        for (int i = 0; i < quantity; i++) {
            zplBuilder.append("^XA\n^CI28\n^PW").append(LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM).append("\n^LL").append(LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM).append("\n");

            // Título menor e mais alto
            zplBuilder.append(String.format("^FO%d,8^A0N,22,22^FDETIQUETA DE VALIDADE^FS\n", textMargin));

            // Linhas distribuídas de forma compacta para não estourar a altura
            int y1 = 42;
            zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FDProduto:^FS\n", textMargin, y1, fontSize, fontSize));
            zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", valueMargin, y1, fontSize, fontSize, request.getProductName() != null ? request.getProductName() : ""));

            int y2 = 78;
            zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FDFabricacao:^FS\n", textMargin, y2, fontSize, fontSize));
            zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", valueMargin, y2, fontSize, fontSize, formattedMfgDate));

            int y3 = 114;
            zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FDData Abertura:^FS\n", textMargin, y3, fontSize, fontSize));
            zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", valueMargin, y3, fontSize, fontSize, formattedOpenDate));

            int y4 = 150;
            zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FDValidade (aberto):^FS\n", textMargin, y4, fontSize, fontSize));
            zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", valueMargin, y4, fontSize, fontSize, formattedValDate));

            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
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
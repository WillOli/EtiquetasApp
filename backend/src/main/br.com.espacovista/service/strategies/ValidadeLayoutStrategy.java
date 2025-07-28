package service.strategies;

import model.ValidadePrintRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static service.ZplConstants.*;

public class ValidadeLayoutStrategy implements ILabelStrategy {
    private final ValidadePrintRequest request;

    public ValidadeLayoutStrategy(ValidadePrintRequest request) {
        this.request = request;
    }

    @Override
    public String generateZpl() {
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate manufacturingDate = LocalDate.parse(request.getMfgDate());
        String formattedMfgDate = manufacturingDate.format(displayFormatter);

        int fontSize = 28;
        int textMargin = 30;
        int valueMargin = 200;
        int lineWidth = 400;
        int lineMargin = 30;

        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < request.getQuantity(); i++) {
            zplBuilder.append("^XA\n^CI28\n^PW").append(LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM).append("\n^LL").append(LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM).append("\n");

            int yPosProduto = 40;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosProduto).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDProduto:^FS\n");
            zplBuilder.append("^FO").append(valueMargin).append(",").append(yPosProduto).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(request.getProductName()).append("^FS\n");
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosProduto + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");

            int yPosFabricacao = 95;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosFabricacao).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDFabricacao:^FS\n");
            zplBuilder.append("^FO").append(valueMargin).append(",").append(yPosFabricacao).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(formattedMfgDate).append("^FS\n");
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosFabricacao + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");

            int yPosValidade = 150;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosValidade).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDValidade:^FS\n");
            zplBuilder.append("^FO").append(valueMargin).append(",").append(yPosValidade).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(request.getValidityDays() + " dias").append("^FS\n");
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosValidade + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");

            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }
}
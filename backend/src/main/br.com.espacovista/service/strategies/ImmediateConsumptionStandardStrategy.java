package service.strategies;
import static service.ZplSanitizer.sanitize;
import model.ImmediateConsumptionRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ImmediateConsumptionStandardStrategy extends AbstractTwoColumnStrategy {
    private final ImmediateConsumptionRequest request;

    public ImmediateConsumptionStandardStrategy(ImmediateConsumptionRequest request) {
        super(request.getQuantity(), widthOf(request), 25);
        this.request = request;
    }

    @Override
    public String generateLabelContent(int startX, int column) {
        if (widthOf(request) == 25) return generateCompactLabel(startX);
        int currentX = startX + 8;
        int contentWidth = 304;

        String productName = sanitize(request.getProductName());

        String dataFabricacao = request.getDataFabricacao() != null && !request.getDataFabricacao().isEmpty()
                ? service.LabelDateFormatter.format(request.getDataFabricacao())
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String validade = service.LabelDateFormatter.format(request.getValidade());

        int fontSizeTitle = 24;
        int fontSizeText = 22;

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(createLine(currentX, 12, contentWidth, fontSizeTitle, "CONSUMO IMEDIATO"));
        contentBuilder.append(createLine(currentX, 58, contentWidth, fontSizeText, "Produto: " + productName));
        contentBuilder.append(createLine(currentX, 104, contentWidth, 21, "Fabricação: " + dataFabricacao));
        contentBuilder.append(createLine(currentX, 150, contentWidth, fontSizeText, "Validade: " + validade));

        return contentBuilder.toString();
    }

    private String generateCompactLabel(int startX) {
        String product = sanitize(request.getProductName());
        String manufacturing = request.getDataFabricacao() != null && !request.getDataFabricacao().isEmpty()
                ? service.LabelDateFormatter.format(request.getDataFabricacao())
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String validity = service.LabelDateFormatter.format(request.getValidade());
        int x = startX + 24;
        int width = 152;
        String[] lines = {
                "CONSUMO IMEDIATO",
                "Produto: " + product,
                "Fab.: " + manufacturing,
                "Val.: " + validity
        };
        int[] y = {20, 66, 112, 158};
        StringBuilder zpl = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            int font = fitFont(lines[i], 15, width);
            zpl.append(createLine(x, y[i], width, font, lines[i]));
        }
        return zpl.toString();
    }

    private String createLine(int x, int y, int width, int fontSize, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,L,0^FD%s^FS\n", x, y, fontSize, fontSize, width, sanitize(text));
    }

    private static int widthOf(ImmediateConsumptionRequest request) {
        return request.getLabelType() == null ? 40 : request.getLabelType().getWidthMm();
    }

    private int fitFont(String value, int maximum, int width) {
        if (value == null || value.isBlank()) return maximum;
        int estimated = (int) (width * 0.88 / (value.length() * 0.62));
        return Math.max(10, Math.min(maximum, estimated));
    }
}

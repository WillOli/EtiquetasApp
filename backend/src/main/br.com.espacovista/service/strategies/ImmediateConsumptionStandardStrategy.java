package service.strategies;
import static service.ZplSanitizer.sanitize;
import model.ImmediateConsumptionRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ImmediateConsumptionStandardStrategy extends AbstractTwoColumnStrategy {
    private final ImmediateConsumptionRequest request;

    public ImmediateConsumptionStandardStrategy(ImmediateConsumptionRequest request) {
        super(request.getQuantity());
        this.request = request;
    }

    @Override
    public String generateLabelContent(int startX, int column) {
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

    private String createLine(int x, int y, int width, int fontSize, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,L,0^FD%s^FS\n", x, y, fontSize, fontSize, width, sanitize(text));
    }
}

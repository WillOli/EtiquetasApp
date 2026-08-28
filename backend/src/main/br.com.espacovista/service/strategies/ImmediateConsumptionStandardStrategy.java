package service.strategies;
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
        // Ajuste fino: garantimos margem segura na esquerda e empurramos um pouco mais a coluna direita para evitar cortes
        int baseOffset = 10;
        int currentX = (column > 0) ? startX + baseOffset +15 : startX + baseOffset;

        String productName = request.getProductName() != null ? request.getProductName() : "";

        String dataFabricacao = request.getDataFabricacao() != null && !request.getDataFabricacao().isEmpty()
                ? request.getDataFabricacao()
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String validade = request.getValidade() != null ? request.getValidade() : "";

        int fontSizeTitle = 22;
        int fontSizeText = 19;

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(createLine(currentX, 15, fontSizeTitle, "CONSUMO IMEDIATO"));
        contentBuilder.append(createLine(currentX, 55, fontSizeText, "Produto: " + productName));
        contentBuilder.append(createLine(currentX, 95, fontSizeText, "Data de Fabricação: " + dataFabricacao));
        contentBuilder.append(createLine(currentX, 135, fontSizeText, "Validade: " + validade));

        return contentBuilder.toString();
    }

    private String createLine(int x, int y, int fontSize, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", x, y, fontSize, fontSize, text);
    }
}
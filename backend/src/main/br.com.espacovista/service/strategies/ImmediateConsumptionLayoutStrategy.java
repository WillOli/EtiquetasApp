package service.strategies;
import model.ImmediateConsumptionRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ImmediateConsumptionLayoutStrategy implements ILabelStrategy {
    private final ImmediateConsumptionRequest request;

    public ImmediateConsumptionLayoutStrategy(ImmediateConsumptionRequest request) {
        this.request = request;
    }

    @Override
    public String generateZpl() {
        String productName = request.getProductName() != null ? request.getProductName() : "";

        String dataFabricacao = request.getDataFabricacao() != null && !request.getDataFabricacao().isEmpty()
                ? request.getDataFabricacao()
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String validade = request.getValidade() != null ? request.getValidade() : "";

        // ➡️ Ajuste este valor para centralizar na etiqueta simples:
        // Se estiver muito para a esquerda, aumente o número (ex: 60 ou 70).
        // Se estiver muito para a direita, diminua o número (ex: 30 ou 20).
        int currentX = 180;

        int fontSizeTitle = 30;
        int fontSizeText = 25;

        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n");
        zpl.append(createLine(currentX, 15, fontSizeTitle, "CONSUMO IMEDIATO"));
        zpl.append(createLine(currentX, 55, fontSizeText, "Produto: " + productName));
        zpl.append(createLine(currentX, 95, fontSizeText, "Data de Fabricação: " + dataFabricacao));
        zpl.append(createLine(currentX, 135, fontSizeText, "Validade: " + validade));
        zpl.append("^XZ\n");

        return zpl.toString();
    }

    private String createLine(int x, int y, int fontSize, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", x, y, fontSize, fontSize, text);
    }
}
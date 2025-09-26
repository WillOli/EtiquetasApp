package service.strategies;

import model.ImmediateConsumptionRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static service.ZplConstants.*;

public class ImmediateConsumptionStrategy extends AbstractTwoColumnStrategy {
    private final ImmediateConsumptionRequest request;

    public ImmediateConsumptionStrategy(ImmediateConsumptionRequest request) {
        super(request.getQuantity());
        this.request = request;
    }

    @Override
    protected String generateLabelContent(int startX, int column) {
        // --- Dados
        String productName = request.getProductName();
        String internalDateCode = generateInternalDateCode();

        // --- PAINEL DE CONTROLE DE LAYOUT
        int fontSizeProduct = 28;
        int fontSizeConsumo = 25;
        int fontSizeCod = 18;

        StringBuilder contentBuilder = new StringBuilder();

        // --- Seção 1: Produto (Rótulo e Nome)
        contentBuilder.append("^FX Seção do Produto\n");
        contentBuilder.append(createLine(startX + 30, 40, fontSizeProduct, productName));

        // --- Seção 2: Consumo Imediato
        contentBuilder.append("^FX Seção de Consumo Imediato\n");
        contentBuilder.append(createLine(startX + 55, 90, fontSizeConsumo, "CONSUMO IMEDIATO"));

        // --- Seção 3: Código
        contentBuilder.append("^FX Seção de Código\n");
        contentBuilder.append(createLine(startX + 215, 160, fontSizeCod, internalDateCode));

        return contentBuilder.toString();
    }

    /**
     * Gera o código ZPL para uma linha de texto.
     */
    private String createLine(int x, int y, int fontSize, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", x, y, fontSize, fontSize, text);
    }

    private String generateInternalDateCode() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        return "00" + today.format(formatter);
    }
}
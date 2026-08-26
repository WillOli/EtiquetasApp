package service.strategies;

import model.ImmediateConsumptionRequest;

public class ImmediateConsumptionStrategy extends AbstractTwoColumnStrategy {
    private final ImmediateConsumptionRequest request;

    public ImmediateConsumptionStrategy(ImmediateConsumptionRequest request) {
        super(request.getQuantity());
        this.request = request;
    }

    @Override
    protected String generateLabelContent(int startX, int column) {
        // --- Afasta mais da borda apenas se for a coluna da direita (column > 0)
        int currentX = (column > 0) ? startX + 15 : startX;

        // --- Dados da Requisição atualizados
        String productName = request.getProductName() != null ? request.getProductName() : "";
        String dataFabricacao = request.getDataFabricacao() != null ? request.getDataFabricacao() : "";
        String validade = request.getValidade() != null ? request.getValidade() : "";

        // --- PAINEL DE CONTROLE DE LAYOUT (Com fontes maiores e bem distribuídas)
        int fontSizeTitle = 22;
        int fontSizeText = 19;

        StringBuilder contentBuilder = new StringBuilder();

        // --- Título da Etiqueta (CONSUMO IMEDIATO)
        contentBuilder.append(createLine(currentX + 10, 15, fontSizeTitle, "CONSUMO IMEDIATO"));

        // --- Campos essenciais distribuídos verticalmente
        contentBuilder.append(createLine(currentX + 10, 55, fontSizeText, "Produto: " + productName));
        contentBuilder.append(createLine(currentX + 10, 95, fontSizeText, "Data de Fabricação: " + dataFabricacao));
        contentBuilder.append(createLine(currentX + 10, 135, fontSizeText, "Validade: " + validade));

        return contentBuilder.toString();
    }

    /**
     * Gera o código ZPL para uma linha de texto.
     */
    private String createLine(int x, int y, int fontSize, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", x, y, fontSize, fontSize, text);
    }
}
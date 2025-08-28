package service.strategies;

import model.ImmediateConsumptionRequest; // Certifique-se que o import para o seu modelo está correto
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
        // --- Dados ---
        String productName = request.getProductName();
        String internalDateCode = generateInternalDateCode();

        // Juntamos o rótulo e o valor em uma única string para cada linha
        String productLineText = "Produto: " + productName;
        String codeLineText = "Cod: " + internalDateCode;

        // --- PAINEL DE CONTROLE DE LAYOUT ---
        // Ajuste estes valores para refinar o layout
        int offsetX = 15; // Margem geral nas laterais da etiqueta
        int labelWidth = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM - (offsetX * 2); // Largura útil para o texto
        int mainFontSize = 22; // Tamanho da fonte para Produto e Consumo
        int codeFontSize = 20; // Tamanho da fonte para o código

        int yPos = 30; // Posição Y (vertical) da primeira linha
        int lineSpacing = 50; // Espaço vertical entre as linhas
        // --- FIM DO PAINEL ---

        StringBuilder contentBuilder = new StringBuilder();

        // --- Linha 1: Produto (Centralizado) ---
        contentBuilder.append(createCenteredLine(productLineText, startX + offsetX, yPos, labelWidth, mainFontSize));
        contentBuilder.append(createDividerLine(startX + offsetX, yPos + mainFontSize + 5, labelWidth));
        yPos += lineSpacing;

        // --- Linha 2: Consumo Imediato (Centralizado) ---
        contentBuilder.append(createCenteredLine("CONSUMO IMEDIATO", startX + offsetX, yPos, labelWidth, mainFontSize));
        contentBuilder.append(createDividerLine(startX + offsetX, yPos + mainFontSize + 5, labelWidth));
        yPos += lineSpacing;

        // --- Linha 3: Código (Centralizado) ---
        contentBuilder.append(createCenteredLine(codeLineText, startX + offsetX, yPos, labelWidth, codeFontSize));
        contentBuilder.append(createDividerLine(startX + offsetX, yPos + codeFontSize + 5, labelWidth));

        return contentBuilder.toString();
    }

    /**
     * Gera o código ZPL para uma linha de texto centralizada.
     */
    private String createCenteredLine(String text, int x, int y, int width, int fontSize) {
        return String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,C,0^FD%s^FS", x, y, fontSize, fontSize, width, text);
    }

    /**
     * Gera o código ZPL para uma linha divisória.
     */
    private String createDividerLine(int x, int y, int width) {
        return String.format("^FO%d,%d^GB%d,2,2^FS\n", x, y, width);
    }

    /**
     * Gera código interno de data no formato 0000MMDD.
     */
    private String generateInternalDateCode() {
        LocalDate today = LocalDate.now();
        // A data da foto é 28 de agosto, então o código está correto.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        return "0000" + today.format(formatter);
    }
}
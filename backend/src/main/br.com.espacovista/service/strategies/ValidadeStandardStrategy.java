package service.strategies;

import model.ValidadePrintRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static service.ZplConstants.*;

/**
 * Estratégia para imprimir uma etiqueta de validade em um layout de duas colunas.
 * Herda a lógica de layout de AbstractTwoColumnStrategy.
 */
public class ValidadeStandardStrategy extends AbstractTwoColumnStrategy {

    private final ValidadePrintRequest request;

    public ValidadeStandardStrategy(ValidadePrintRequest request) {
        // Passa a quantidade para o construtor da classe pai.
        super(request.getQuantity());
        this.request = request;
    }

    /**
     * Implementa o método abstrato para gerar o conteúdo específico da etiqueta de validade.
     */
    @Override
    protected String generateLabelContent(int startX, int column) {
        // Prepara os dados a serem impressos
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate manufacturingDate = LocalDate.parse(request.getMfgDate());
        String formattedMfgDate = manufacturingDate.format(displayFormatter);
        String prazoText = request.getValidityDays() + " dias";
        String productName = request.getProductName();

        // Define as constantes de layout
        int fontSize = 22;
        int textMargin = 15;
        int valueMargin = 130;
        int baseLineWidth = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM - (textMargin * 2);
        int offsetX = 5;

        // Lógica de compensação para a coluna da direita
        int currentLineWidth = baseLineWidth;
        int currentValueMargin = valueMargin;
        if (column == 1) {
            currentLineWidth += 10;
            currentValueMargin += 10;
        }

        int yPos = 25;
        int lineSpacing = 50;

        StringBuilder contentBuilder = new StringBuilder();

        // --- Linha 1: Produto ---
        contentBuilder.append(generateLine("Produto:", productName, startX + offsetX, yPos, fontSize, textMargin, currentValueMargin, currentLineWidth));
        yPos += lineSpacing;

        // --- Linha 2: Fabricação ---
        contentBuilder.append(generateLine("Fabricacao:", formattedMfgDate, startX + offsetX, yPos, fontSize, textMargin, currentValueMargin, currentLineWidth));
        yPos += lineSpacing;

        // --- Linha 3: Validade ---
        contentBuilder.append(generateLine("Validade:", prazoText, startX + offsetX, yPos, fontSize, textMargin, currentValueMargin, currentLineWidth));

        return contentBuilder.toString();
    }

    /**
     * Método auxiliar para gerar uma linha completa (Rótulo, Valor e Linha divisória).
     */
    private String generateLine(String label, String value, int startX, int yPos, int fontSize, int textMargin, int valueMargin, int lineWidth) {
        return String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", startX + textMargin, yPos, fontSize, fontSize, label) +
                String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", startX + valueMargin, yPos, fontSize, fontSize, value) +
                String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + textMargin, yPos + fontSize + 5, lineWidth);
    }
}

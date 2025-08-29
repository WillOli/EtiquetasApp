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
        // --- Dados
        String productName = request.getProductName();
        String internalDateCode = generateInternalDateCode();

        // --- PAINEL DE CONTROLE DE LAYOUT
        int borderSize = 3;
        int margin = 10;
        int textOffset = 15;
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM;
        int contentWidth = labelWidthDots - (margin * 2);

        StringBuilder contentBuilder = new StringBuilder();

        // --- Seção 1: Moldura da Etiqueta
        contentBuilder.append("^FX Etiqueta com Borda\n");
        contentBuilder.append(String.format("^FO%d,%d^GB%d,%d,%d^FS\n",
                startX + margin, margin, contentWidth, labelHeightDots - (margin * 2), borderSize));

        // --- Seção 2: Linha 1 - Produto (Ajustado)
        contentBuilder.append("^FX Seção do Produto\n");
        contentBuilder.append(createCenteredLine(productName, startX + textOffset, 40, contentWidth - textOffset, 35));

        // --- Seção 3: Linha 2 - Consumo Imediato (Ajustado)
        contentBuilder.append("^FX Seção de Consumo Imediato\n");
        contentBuilder.append(createCenteredLine("CONSUMO IMEDIATO", startX + textOffset, 90, contentWidth - textOffset, 20));

        // --- Seção 4: Linha divisória
        contentBuilder.append("^FX Linha divisória\n");
        contentBuilder.append(String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + textOffset, 120, contentWidth - textOffset));

        // --- Seção 5: Código no canto inferior direito
        contentBuilder.append("^FX Seção de Código (canto inferior direito)\n");
        contentBuilder.append(String.format("^FO%d,%d^A0N,20,20^FD%s^FS\n", startX + 220, 195, internalDateCode));

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
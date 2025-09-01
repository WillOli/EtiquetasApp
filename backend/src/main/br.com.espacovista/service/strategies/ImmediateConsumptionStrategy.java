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
        int borderSize = 3;
        int margin = 10;
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM; // 320 pontos para 80mm
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM; // 100 pontos para 25mm (aprox)
        int contentWidth = labelWidthDots - (margin * 2);

        int fontSizeProduct = 28;
        int fontSizeConsumo = 25;
        int fontSizeCod = 25;
        int textLineHeight = fontSizeProduct + 5;
        int centralLineHeight = fontSizeConsumo + 5;
        int codeLineHeight = fontSizeCod + 5;

        StringBuilder contentBuilder = new StringBuilder();

        // --- Seção 1: Moldura da Etiqueta
        contentBuilder.append("^FX Etiqueta com Borda\n");
        contentBuilder.append(String.format("^FO%d,%d^GB%d,%d,%d^FS\n",
                startX + margin, margin, contentWidth, labelHeightDots - (margin * 2), borderSize));
        contentBuilder.append(String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + 15, margin + textLineHeight, contentWidth - 15));

        // --- Seção 2: Produto (Rótulo e Nome)
        contentBuilder.append("^FX Seção do Produto\n");
        int yPos = margin + 15;
        contentBuilder.append(createLine(startX + 15, yPos, fontSizeProduct, "Produto:"));
        contentBuilder.append(createLine(startX + 125, yPos, fontSizeProduct, productName));

        // --- Seção 3: Consumo Imediato (Centralizado)
        contentBuilder.append("^FX Seção de Consumo Imediato\n");
        yPos += textLineHeight;
        contentBuilder.append(String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + 15, yPos, contentWidth - 15));
        yPos += 15;
        contentBuilder.append(createCenteredLine("CONSUMO IMEDIATO", startX + 15, yPos, contentWidth - 15, fontSizeConsumo));
        yPos += centralLineHeight;
        contentBuilder.append(String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + 15, yPos, contentWidth - 15));

        // --- Seção 4: Código (Rótulo e Código)
        contentBuilder.append("^FX Seção de Código\n");
        yPos += 15;
        contentBuilder.append(createLine(startX + 15, yPos, fontSizeCod, "Cod:"));

        int codeRightOffset = 100;
        int codeXPos = startX + contentWidth - codeRightOffset;
        contentBuilder.append(createLine(codeXPos, yPos, fontSizeCod, internalDateCode));
        yPos += codeLineHeight;
        contentBuilder.append(String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + 15, yPos, contentWidth - 15));

        return contentBuilder.toString();
    }

    /**
     * Gera o código ZPL para uma linha de texto centralizada em um bloco.
     */
    private String createCenteredLine(String text, int x, int y, int width, int fontSize) {
        return String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,C,0^FD%s^FS", x, y, fontSize, fontSize, width, text);
    }

    /**
     * Gera o código ZPL para uma linha de texto sem centralização.
     */
    private String createLine(int x, int y, int fontSize, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", x, y, fontSize, fontSize, text);
    }

    private String generateInternalDateCode() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        return "0000" + today.format(formatter);
    }
}

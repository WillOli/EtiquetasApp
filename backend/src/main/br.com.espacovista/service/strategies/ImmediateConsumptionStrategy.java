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
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM; // 320 pontos para 80mm
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM; // 100 pontos para 25mm (aprox)
        int contentWidth = labelWidthDots - (margin * 2); // 320 - 20 = 300

        // Fontes ajustadas conforme a sua solicitação.
        int fontSizeProduct = 28; // Reduzido um pouco para não vazar
        int fontSizeConsumo = 25; // Reduzido para ser menor
        int fontSizeCod = 25;

        StringBuilder contentBuilder = new StringBuilder();

        // --- Seção 1: Moldura da Etiqueta
        contentBuilder.append("^FX Etiqueta com Borda\n");
        contentBuilder.append(String.format("^FO%d,%d^GB%d,%d,%d^FS\n",
                startX + margin, margin, contentWidth, labelHeightDots - (margin * 2), borderSize));

        // --- Seção 2: Produto (Rótulo e Nome)
        contentBuilder.append("^FX Seção do Produto\n");
        int yPos = 35; // Posição Y inicial
        contentBuilder.append(createLine(startX + 15, yPos, fontSizeProduct, "Produto:"));
        // Ajuste no X para o nome do produto começar um pouco mais para a direita
        contentBuilder.append(createLine(startX + 125, yPos, fontSizeProduct, productName));
        // Linha divisória abaixo do nome do produto
        yPos += fontSizeProduct + 5;
        contentBuilder.append(String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + 15, yPos, contentWidth - 15));

        // --- Seção 3: Consumo Imediato (Centralizado)
        contentBuilder.append("^FX Seção de Consumo Imediato\n");
        yPos += 15;
        contentBuilder.append(createCenteredLine("CONSUMO IMEDIATO", startX + 15, yPos, contentWidth - 15, fontSizeConsumo));
        // Linha divisória abaixo do "CONSUMO IMEDIATO"
        yPos += fontSizeConsumo + 5;
        contentBuilder.append(String.format("^FO%d,%d^GB%d,2,2^FS\n", startX + 15, yPos, contentWidth - 15));

        // --- Seção 4: Código (Rótulo e Código no canto inferior direito)
        contentBuilder.append("^FX Seção de Código\n");
        yPos += 15;
        contentBuilder.append(createLine(startX + 15, yPos, fontSizeCod, "Cod:"));

        // Posiciona o código. Calculei um novo X para ficar bem alinhado à direita, mas com um pequeno espaço.
        // O 220 foi um chute inicial, mas precisamos de um cálculo mais preciso:
        // (startX + contentWidth) -> limite direito do conteúdo.
        // - (largura_aproximada_do_codigo) -> para recuar um pouco
        // Para o texto "00000829" com fonte de 25, aproximadamente 10 caracteres * 15 (pixel por caractere) = 150px
        int codeRightOffset = 100; // Ajuste este valor para mover o código para a esquerda/direita
        int codeXPos = startX + contentWidth - codeRightOffset;
        contentBuilder.append(createLine(codeXPos, yPos, fontSizeCod, internalDateCode));

        // Última linha divisória abaixo do código
        yPos += fontSizeCod + 5;
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
        return String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS", x, y, fontSize, fontSize, text);
    }

    private String generateInternalDateCode() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        return "0000" + today.format(formatter);
    }
}
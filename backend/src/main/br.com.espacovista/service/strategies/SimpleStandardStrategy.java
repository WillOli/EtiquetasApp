package service.strategies;

import static service.ZplConstants.*;

/**
 * Estratégia para imprimir uma etiqueta de texto simples em um layout de duas colunas.
 * Herda a lógica de layout de AbstractTwoColumnStrategy.
 */
public class SimpleStandardStrategy extends AbstractTwoColumnStrategy {

    private final String text;

    public SimpleStandardStrategy(String text, int quantity) {
        // Passa a quantidade para o construtor da classe pai.
        super(quantity);
        this.text = text;
    }

    /**
     * Implementa o método abstrato para gerar o conteúdo específico desta etiqueta.
     */
    @Override
    protected String generateLabelContent(int startX, int column) {
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM;
        int fontHeight = FONT_HEIGHT_STANDARD - 10;

        // Lógica de posicionamento que era específica desta classe.
        int globalLeftShift = 10;
        int posX;
        if (column == 0) {
            posX = 20 - globalLeftShift; // Posição para a coluna da esquerda
        } else {
            posX = (startX - 15) - globalLeftShift; // Posição para a coluna da direita
        }

        // Adiciona uma pequena margem vertical
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM;
        int posY = (labelHeightDots - fontHeight) / 2 + 10;

        // Usa um StringBuilder para construir o fragmento ZPL.
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("^FO").append(posX).append(",").append(posY)
                .append("^A0N,").append(fontHeight).append(",").append(fontHeight)
                .append("^FB").append(labelWidthDots).append(",3,0,C,0")
                .append("^FD").append(this.text).append("^FS\n");

        return contentBuilder.toString();
    }
}

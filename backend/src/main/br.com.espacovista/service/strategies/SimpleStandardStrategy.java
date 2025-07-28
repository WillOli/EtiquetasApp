package service.strategies;

import static service.ZplConstants.*;

public class SimpleStandardStrategy implements ILabelStrategy {
    private final String text;
    private final int quantity;

    public SimpleStandardStrategy(String text, int quantity) {
        this.text = text;
        this.quantity = quantity * 2;
    }

    @Override
    public String generateZpl() {
        int pageWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM * 2 + GAP_HORIZONTAL_DOTS * 2;
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM;
        int fontHeight = FONT_HEIGHT_STANDARD - 10;

        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < this.quantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n^CI28\n^PW").append(pageWidthDots).append("\n^LL").append(labelHeightDots).append("\n");
            }
            for (int col = 0; col < 2; col++) {
                int currentX = col * (labelWidthDots + GAP_HORIZONTAL_DOTS);

                int additionalOffsetXLeft = 0;
                if (col == 0) {
                    additionalOffsetXLeft = 20;
                }

                int additionalOffsetXRight = 0;
                if (col == 1) {
                    additionalOffsetXRight = -15;
                }

                int posX = additionalOffsetXLeft + additionalOffsetXRight + currentX;

                // ===================================================================
                // =====        INÍCIO DA CORREÇÃO DE CENTRALIZAÇÃO VERTICAL     =====
                // ===================================================================
                // Calcula a posição Y (vertical) para centralizar o texto
                int posY = (labelHeightDots - fontHeight) / 2;

                // Adiciona um pequeno ajuste fino para descer o texto um pouco,
                // pois a centralização matemática exata às vezes parece muito alta.
                // Você pode alterar este valor se achar necessário.
                posY += 10;
                // ===================================================================

                zplBuilder.append("^FO").append(posX).append(",").append(posY) // Usa a nova variável posY
                        .append("^A0N,").append(fontHeight).append(",").append(fontHeight)
                        .append("^FB").append(labelWidthDots).append(",3,0,C,0") // O 'C' aqui já centraliza horizontalmente
                        .append("^FD").append(this.text).append("^FS\n");
            }
            if (i % 2 == 1 || i == this.quantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }
}
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

            int col = i % 2;
            int currentX = col * (labelWidthDots + GAP_HORIZONTAL_DOTS);

            // ======================= LÓGICA DE POSICIONAMENTO CORRIGIDA =======================
            // O valor abaixo controla o quanto a impressão é movida para a esquerda.
            // Para mover AINDA MAIS para a esquerda, aumente este número.
            // Para mover para a direita (caso tenha passado do ponto), diminua.
            int globalLeftShift = 10; // Ex: deslocamento de 15 pontos para a esquerda.

            int posX;
            if (col == 0) {
                // A posição original da etiqueta ESQUERDA era 20.
                // A nova posição é a original MENOS o deslocamento.
                posX = 20 - globalLeftShift;
            } else { // col == 1
                // A posição original da etiqueta DIREITA era (currentX - 15).
                // A nova posição é a original MENOS o deslocamento.
                posX = (currentX - 15) - globalLeftShift;
            }
            // =================================================================================

            int posY = (labelHeightDots - fontHeight) / 2;
            posY += 10;

            zplBuilder.append("^FO").append(posX).append(",").append(posY)
                    .append("^A0N,").append(fontHeight).append(",").append(fontHeight)
                    .append("^FB").append(labelWidthDots).append(",3,0,C,0")
                    .append("^FD").append(this.text).append("^FS\n");

            if (i % 2 == 1 || i == this.quantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }
}
package service.strategies;

import static service.ZplConstants.*;

public class SimpleStandardStrategy implements ILabelStrategy {
    private final String text;
    private final int quantity;

    public SimpleStandardStrategy(String text, int quantity) {
        this.text = text;
        this.quantity = quantity * 2; // Etiqueta dupla sempre imprime o dobro
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
                int posX = 20 + col * (labelWidthDots + GAP_HORIZONTAL_DOTS);
                zplBuilder.append("^FO").append(posX).append(",40")
                        .append("^A0N,").append(fontHeight).append(",").append(fontHeight)
                        .append("^FB").append(labelWidthDots - 40).append(",3,0,C,0")
                        .append("^FD").append(this.text).append("^FS\n");
            }
            if (i % 2 == 1 || i == this.quantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }
}
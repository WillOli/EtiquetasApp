package service.strategies;

import static service.ZplConstants.*;

public class SimpleLayoutStrategy implements ILabelStrategy {
    private final String text;
    private final int quantity;

    public SimpleLayoutStrategy(String text, int quantity) {
        this.text = text;
        this.quantity = quantity;
    }

    @Override
    public String generateZpl() {
        int labelWidthDots = LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int fontHeight = FONT_HEIGHT_SIXTY_TWO_MM;
        int maxLines = (int) Math.floor((double) labelHeightDots / fontHeight);

        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < this.quantity; i++) {
            zplBuilder.append("^XA\n^CI28\n^PW").append(labelWidthDots).append("\n^LL").append(labelHeightDots).append("\n");
            int verticalPosition = 80; // Posição centralizada
            zplBuilder.append("^FO0,").append(verticalPosition)
                    .append("^A0N,").append(fontHeight).append(",").append(fontHeight)
                    .append("^FB").append(labelWidthDots).append(",").append(maxLines).append(",0,C,0")
                    .append("^FD").append(this.text).append("^FS\n");
            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }
}
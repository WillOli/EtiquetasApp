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
        // Usando as constantes para definir a área total da etiqueta
        int labelWidthDots = LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM; // ~480 a 500 dots
        int labelHeightDots = LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM; // ~240 a 300 dots

        // Ajuste de fonte: se o nome for curto, usamos 80. Se for longo, 60.
        int fontHeightNome = (this.text.length() > 12) ? 60 : 80;

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < this.quantity; i++) {
            zplBuilder.append("^XA\n");
            zplBuilder.append("^CI28\n"); // Suporte para acentos
            zplBuilder.append("^PW").append(labelWidthDots).append("\n");
            zplBuilder.append("^LL").append(labelHeightDots).append("\n");

            // --- 1. NOME DIGITADO (Variável) ---
            // FO0,80 -> Posição vertical um pouco acima do meio
            zplBuilder.append("^FO0,80")
                    .append("^A0N,").append(fontHeightNome).append(",").append(fontHeightNome)
                    .append("^FB").append(labelWidthDots).append(",1,0,C,0") // 1 linha, Centralizado
                    .append("^FD").append(this.text.toUpperCase()).append("^FS\n");

            // --- 2. LINHA DIVISORA (Estético) ---
            // Desenha uma linha de 300 dots de largura no centro
            zplBuilder.append("^FO150,165^GB300,2,2^FS\n");

            // --- 3. GRUPO VISTA (Fixo) ---
            // FO0,185 -> Posição vertical na parte inferior
            zplBuilder.append("^FO0,185")
                    .append("^A0N,35,35") // Fonte menor para o rodapé
                    .append("^FB").append(labelWidthDots).append(",1,0,C,0")
                    .append("^FDGRUPO VISTA^FS\n");

            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }
}
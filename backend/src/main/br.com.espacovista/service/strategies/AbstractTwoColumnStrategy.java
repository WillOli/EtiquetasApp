package service.strategies;
import static service.ZplConstants.*;

/**
 * Classe base abstrata para estratégias de impressão que usam um layout de duas colunas.
 * Encapsula a lógica comum de iteração, cálculo de posição e comandos de página ZPL.
 */
public abstract class AbstractTwoColumnStrategy implements ILabelStrategy {

    private final int quantity;

    /**
     * Construtor para a estratégia de duas colunas.
     * @param quantity A quantidade de etiquetas solicitada pelo usuário (será duplicada internamente).
     */
    public AbstractTwoColumnStrategy(int quantity) {
        // A quantidade real de etiquetas a serem impressas é sempre o dobro da solicitada.
        this.quantity = quantity * 2;
    }


    /**
     * Método final que gera o ZPL completo.
     * Este método não pode ser sobrescrito pelas classes filhas.
     * @return O código ZPL completo para todas as etiquetas.
     */
    @Override
    public final String generateZpl() {
        StringBuilder zplBuilder = new StringBuilder();
        int pageWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM * 2 + GAP_HORIZONTAL_DOTS * 2;
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM;

        for (int i = 0; i < this.quantity; i++) {
            // Inicia uma nova página a cada duas etiquetas (na primeira e na terceira, etc.)
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n^CI28\n^PW").append(pageWidthDots).append("\n^LL").append(labelHeightDots).append("\n");
            }

            int col = i % 2;
            int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM;
            int currentX = col * (labelWidthDots + GAP_HORIZONTAL_DOTS);

            // Chama o método abstrato para obter o conteúdo específico da etiqueta.
            // A classe filha decide o que será impresso.
            zplBuilder.append(generateLabelContent(currentX, col));

            // Finaliza a página a cada duas etiquetas ou se for a última.
            if (i % 2 == 1 || i == this.quantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }

    /**
     * Método abstrato que deve ser implementado pelas classes filhas.
     * Sua responsabilidade é gerar o fragmento de código ZPL para o conteúdo de uma única etiqueta.
     *
     * @param startX A coordenada X inicial para esta etiqueta na página.
     * @param column O índice da coluna (0 para esquerda, 1 para direita).
     * @return Uma String contendo os comandos ZPL (^FO, ^A0, ^FD, etc.) para o conteúdo da etiqueta.
     */
    protected abstract String generateLabelContent(int startX, int column);
}


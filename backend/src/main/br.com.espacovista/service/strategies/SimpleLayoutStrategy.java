package service.strategies;
import static service.ZplSanitizer.sanitize;

public class SimpleLayoutStrategy implements ILabelStrategy {
    private final String text;
    private final int quantity;
    private final String setor;
    private final String dataFabricacao;
    private final String dataValidade;
    private final long numeroImpressao;

    public SimpleLayoutStrategy(String text, int quantity, String setor, String dataFabricacao, String dataValidade, long numeroImpressao) {
        this.text = text;
        this.quantity = quantity;
        this.setor = (setor != null) ? setor : "";
        this.dataFabricacao = service.LabelDateFormatter.format(dataFabricacao);
        this.dataValidade = service.LabelDateFormatter.format(dataValidade);
        this.numeroImpressao = numeroImpressao;
    }

    @Override
    public String generateZpl() {
        int labelWidthDots = 824;
        int labelHeightDots = 240;
        int contentWidth = labelWidthDots - 80;
        int fontHeightNome = this.text.length() > 30
                ? Math.max(12, (int) (contentWidth * 0.8 / (this.text.length() * 0.62)))
                : 36;

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < this.quantity; i++) {
            zplBuilder.append("^XA\n");
            zplBuilder.append("^CI28\n");
            zplBuilder.append("^PW").append(labelWidthDots).append("\n");
            zplBuilder.append("^LL").append(labelHeightDots).append("\n");

            long regAtual = this.numeroImpressao + i;
            String regFormatado = String.format("%05d", regAtual);
            int contentStart = 40;
            zplBuilder.append("^FO").append(contentStart).append(",18")
                    .append("^A0N,").append(fontHeightNome).append(",").append(fontHeightNome)
                    .append("^FB").append(contentWidth).append(",1,0,C,0^FD")
                    .append(sanitize(this.text.toUpperCase(java.util.Locale.ROOT))).append("^FS\n");
            zplBuilder.append("^FO").append(contentStart).append(",64")
                    .append("^A0N,26,26^FB").append(contentWidth).append(",1,0,C,0^FDSETOR: ")
                    .append(sanitize(this.setor.toUpperCase(java.util.Locale.ROOT))).append("^FS\n");
            zplBuilder.append("^FO").append(contentStart).append(",105")
                    .append("^A0N,25,25^FB").append(contentWidth).append(",1,0,C,0^FDFAB.: ")
                    .append(sanitize(this.dataFabricacao)).append("^FS\n");
            zplBuilder.append("^FO").append(contentStart).append(",145")
                    .append("^A0N,25,25^FB").append(contentWidth).append(",1,0,C,0^FDVAL.: ")
                    .append(sanitize(this.dataValidade)).append("^FS\n");
            zplBuilder.append("^FO").append(contentStart).append(",185")
                    .append("^A0N,24,24^FB").append(contentWidth).append(",1,0,C,0^FDREG.: ")
                    .append(regFormatado).append("^FS\n");
            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }
}

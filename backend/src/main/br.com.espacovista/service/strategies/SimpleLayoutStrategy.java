package service.strategies;

public class SimpleLayoutStrategy implements ILabelStrategy {
    private final String text;
    private final int quantity;
    private final String setor;
    private final String dataFabricacao;
    private final String dataValidade;
    private final long numeroImpressao;

    public SimpleLayoutStrategy(String text, int quantity) {
        this(text, quantity, "", "", "", 0L);
    }

    public SimpleLayoutStrategy(String text, int quantity, String setor, String dataFabricacao, String dataValidade, long numeroImpressao) {
        this.text = text;
        this.quantity = quantity;
        this.setor = (setor != null) ? setor : "";
        this.dataFabricacao = (dataFabricacao != null) ? dataFabricacao : "";
        this.dataValidade = (dataValidade != null) ? dataValidade : "";
        this.numeroImpressao = numeroImpressao;
    }

    @Override
    public String generateZpl() {
        int labelWidthDots = 824;
        int labelHeightDots = 240;
        int fontHeightNome = (this.text.length() > 16) ? 24 : 28;

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < this.quantity; i += 2) {
            zplBuilder.append("^XA\n");
            zplBuilder.append("^CI28\n");
            zplBuilder.append("^PW").append(labelWidthDots).append("\n");
            zplBuilder.append("^LL").append(labelHeightDots).append("\n");

            for (int col = 0; col < 2; col++) {
                int itemAtual = i + col;
                if (itemAtual >= this.quantity) break;

                int offsetX = (col == 0) ? 25 : 405;
                long regAtual = this.numeroImpressao + itemAtual;
                String regFormatado = String.format("%05d", regAtual);

                zplBuilder.append("^FO").append(offsetX).append(",25")
                        .append("^A0N,").append(fontHeightNome).append(",").append(fontHeightNome)
                        .append("^FB360,1,0,C,0^FD").append(this.text.toUpperCase()).append("^FS\n");
                zplBuilder.append("^FO").append(offsetX).append(",65")
                        .append("^A0N,22,22^FB360,1,0,C,0^FDSETOR: ").append(this.setor.toUpperCase()).append("^FS\n");
                zplBuilder.append("^FO").append(offsetX).append(",100")
                        .append("^A0N,20,20^FB360,1,0,C,0^FDFAB.: ").append(this.dataFabricacao).append("^FS\n");
                zplBuilder.append("^FO").append(offsetX).append(",130")
                        .append("^A0N,20,20^FB360,1,0,C,0^FDVAL.: ").append(this.dataValidade).append("^FS\n");
                zplBuilder.append("^FO").append(offsetX).append(",160")
                        .append("^A0N,20,20^FB360,1,0,C,0^FDREG.: ").append(regFormatado).append("^FS\n");
            }
            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }
}
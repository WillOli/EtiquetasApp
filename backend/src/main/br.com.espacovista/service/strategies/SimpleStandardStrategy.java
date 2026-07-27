package service.strategies;

import static service.ZplConstants.*;

/**
 * Estratégia para imprimir a Etiqueta Dupla (Padrão / Standard).
 * Adaptada para utilizar EXATAMENTE as mesmas dimensões, fontes e coordenadas
 * da SimpleLayoutStrategy (Etiqueta Simples), garantindo alinhamento idêntico.
 */
public class SimpleStandardStrategy extends AbstractTwoColumnStrategy {

    private final String text;
    private final String setor;
    private final String dataFabricacao;
    private final String dataValidade;
    private final String registro;

    // Construtor completo
    public SimpleStandardStrategy(String text, String setor, String dataFabricacao, String dataValidade, String registro, int quantity) {
        super(quantity);
        this.text = (text != null) ? text : "";
        this.setor = (setor != null) ? setor : "";
        this.dataFabricacao = (dataFabricacao != null) ? dataFabricacao : "";
        this.dataValidade = (dataValidade != null) ? dataValidade : "";
        this.registro = (registro != null) ? registro : "";
    }

    // Construtores de compatibilidade
    public SimpleStandardStrategy(String text, String setor, String dataFabricacao, String dataValidade, int quantity) {
        this(text, setor, dataFabricacao, dataValidade, "", quantity);
    }

    public SimpleStandardStrategy(String text, int quantity) {
        this(text, "CONFEITARIA", "", "", "", quantity);
    }

    /**
     * Gera o conteúdo ZPL adotando as mesmas dimensões e coordenadas da Etiqueta Simples.
     */
    @Override
    protected String generateLabelContent(int startX, int column) {
        // Ajuste das colunas idêntico
        int offsetX = (column == 0) ? 5 : 340;
        int fontHeightNome = (this.text.length() > 16) ? 24 : 28;

        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder.append("^FO").append(offsetX).append(",25")
                .append("^A0N,").append(fontHeightNome).append(",").append(fontHeightNome)
                .append("^FB340,1,0,C,0^FD").append(this.text.toUpperCase()).append("^FS\n");

        if (!this.setor.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(",65")
                    .append("^A0N,22,22^FB340,1,0,C,0^FDSETOR: ").append(this.setor.toUpperCase()).append("^FS\n");
        }

        if (!this.dataFabricacao.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(",100")
                    .append("^A0N,20,20^FB340,1,0,C,0^FDFAB.: ").append(this.dataFabricacao).append("^FS\n");
        }

        if (!this.dataValidade.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(",130")
                    .append("^A0N,20,20^FB340,1,0,C,0^FDVAL.: ").append(this.dataValidade).append("^FS\n");
        }

        if (!this.registro.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(",160")
                    .append("^A0N,20,20^FB340,1,0,C,0^FDREG.: ").append(this.registro).append("^FS\n");
        }

        return contentBuilder.toString();
    }
}
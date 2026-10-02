package model;

public class PrintRequest {

    private String text;
    private int quantity;
    private LabelType labelType;

    private String setor;
    private String dataFabricacao;
    private String dataValidade;
    private String registro;

    public enum LabelType {
        STANDARD,
        SIXTY_TWO_MM
    }

    public PrintRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
    }

    public void setLabelType(String labelTypeStr) {

        if (labelTypeStr == null ||
                labelTypeStr.trim().isEmpty()) {

            this.labelType = null;
            return;
        }

        try {
            this.labelType =
                    LabelType.valueOf(
                            labelTypeStr
                                    .trim()
                                    .toUpperCase(java.util.Locale.ROOT)
                    );

        } catch (IllegalArgumentException e) {
            this.labelType = null;
        }
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getDataFabricacao() {
        return dataFabricacao;
    }

    public void setDataFabricacao(String dataFabricacao) {
        this.dataFabricacao = dataFabricacao;
    }

    public String getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(String dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }
}
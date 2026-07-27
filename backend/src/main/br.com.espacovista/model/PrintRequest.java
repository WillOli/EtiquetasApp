package model;

public class PrintRequest {

    private String text;
    private int quantity;
    private LabelType labelType;

    private String setor;
    private String dataFabricacao;
    private String dataValidade;
    private String registro; // ✅ Adicionado para suportar o número sequencial

    public enum LabelType {
        STANDARD,
        SIXTY_TWO_MM
    }

    public PrintRequest() {

    }

    // --- GETTERS ---
    public String getText() {
        return text;
    }

    public int getQuantity() {
        return quantity;
    }

    public LabelType getLabelType() {
        return (labelType == null) ? LabelType.STANDARD : labelType;
    }

    // --- SETTERS ---
    public void setText(String text) {
        this.text = text;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * ✅ Mantido: A lógica de conversão segura para o Enum.
     */
    public void setLabelType(String labelTypeStr) {
        LabelType tempType;
        try {
            if (labelTypeStr == null || labelTypeStr.trim().isEmpty()) {
                tempType = LabelType.STANDARD;
            } else {
                tempType = LabelType.valueOf(labelTypeStr.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("[AVISO] Valor de labelType inválido recebido: '" + labelTypeStr + "'. Usando padrão.");
            tempType = LabelType.STANDARD;
        }
        this.labelType = tempType;
    }

    // --- GETTERS E SETTERS DOS CAMPOS COMPLEMENTARES ---
    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }

    public String getDataFabricacao() { return dataFabricacao; }
    public void setDataFabricacao(String dataFabricacao) { this.dataFabricacao = dataFabricacao; }

    public String getDataValidade() { return dataValidade; }
    public void setDataValidade(String dataValidade) { this.dataValidade = dataValidade; }

    public String getRegistro() { return registro; }
    public void setRegistro(String registro) { this.registro = registro; }
}
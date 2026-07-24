package model;

public class PrintRequest {

    private String text;
    private int quantity;
    private LabelType labelType;

    private String setor;
    private String dataFabricacao;
    private String dataValidade;

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
     * ✅ CORREÇÃO: A lógica de conversão está DENTRO do setter.
     * Este método recebe a String do JSON e a converte para o Enum antes de atribuir.
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
        // Atribui o Enum convertido, não a String original
        this.labelType = tempType;
    }

    // --- GETTERS E SETTERS DOS NOVOS CAMPOS ---
    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }
    public String getDataFabricacao() { return dataFabricacao; }
    public void setDataFabricacao(String dataFabricacao) { this.dataFabricacao = dataFabricacao; }
    public String getDataValidade() { return dataValidade; }
    public void setDataValidade(String dataValidade) { this.dataValidade = dataValidade; }
}
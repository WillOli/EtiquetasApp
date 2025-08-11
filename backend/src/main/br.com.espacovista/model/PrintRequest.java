package model;

public class PrintRequest {

    private String text;
    private int quantity;
    private LabelType labelType;

    public enum LabelType {
        STANDARD,
        SIXTY_TWO_MM
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
}
package model;

public class PrintRequest {
    private final String text;
    private final int quantity;
    private final LabelType labelType;

    public enum LabelType {
        STANDARD,
        SIXTY_TWO_MM
    }

    // --- CONSTRUTOR CORRIGIDO ---
    // O nome do parâmetro agora é 'labelType' para combinar perfeitamente com o JSON do frontend.
    public PrintRequest(String text, int quantity, String labelType) {
        this.text = text;
        this.quantity = quantity;

        LabelType tempType;
        try {
            // Garante que labelType não seja nulo antes de toUpperCase()
            if (labelType == null || labelType.trim().isEmpty()) {
                tempType = LabelType.STANDARD; // Define um padrão se o campo estiver ausente ou vazio
            } else {
                tempType = LabelType.valueOf(labelType.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("[AVISO] Valor de labelType inválido recebido: '" + labelType + "'. Usando padrão.");
            tempType = LabelType.STANDARD; // Define um padrão em caso de valor inválido (ex: "MINI")
        }
        this.labelType = tempType;
    }

    public String getText() {
        return text;
    }

    public int getQuantity() {
        return quantity;
    }

    public LabelType getLabelType() {
        return labelType;
    }
}
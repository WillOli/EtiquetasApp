package model;

public class PrintRequest {
    private final String text;
    private final int quantity;
    private final LabelType labelType; // Novo campo

    public enum LabelType { // Novo enum
        STANDARD,
        SIXTY_TWO_MM
    }

    public PrintRequest(String text, int quantity, String labelTypeString) { // Construtor modificado
        this.text = text;
        this.quantity = quantity;

        LabelType type; // Declare a variável sem inicializá-la
        try {
            type = LabelType.valueOf(labelTypeString.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            // Se ocorrer um erro (ex: string inválida), use o tipo padrão
            type = LabelType.STANDARD;
        }
        this.labelType = type; // Atribua o valor final à variável final
    }

    public String getText() {
        return text;
    }

    public int getQuantity() {
        return quantity;
    }

    public LabelType getLabelType() { // Novo getter
        return labelType;
    }
}
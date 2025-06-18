package model;

public class PrintRequest {
    private final String text;
    private final int quantity;

    public PrintRequest(String text, int quantity) {
        this.text = text;
        this.quantity = quantity;
    }

    public String getText() {
        return text;
    }

    public int getQuantity() {
        return quantity;
    }
}
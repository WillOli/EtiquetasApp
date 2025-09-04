package model;

import model.PrintRequest.LabelType;

public class ImmediateConsumptionRequest {
    private String productName;
    private int quantity;
    private LabelType labelType;

    public ImmediateConsumptionRequest() {
    }

    // --- GETTERS ---
    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public LabelType getLabelType() {
        return (labelType == null) ? LabelType.STANDARD : labelType;
    }

    // --- SETTERS ---
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
    }
}
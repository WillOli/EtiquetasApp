package model;

// Importa o enum do outro modelo para reutilização.
import model.PrintRequest.LabelType;

public class ValidadePrintRequest {
    private String productName;
    private String mfgDate;
    private int validityDays;
    private int quantity;
    // O tipo do campo deve ser LabelType
    private LabelType labelType;

    // --- GETTERS ---
    public String getProductName() {
        return productName;
    }

    public String getMfgDate() {
        return mfgDate;
    }

    public int getValidityDays() {
        return validityDays;
    }

    public int getQuantity() {
        return quantity;
    }

    // O getter deve retornar o tipo LabelType
    public LabelType getLabelType() {
        return labelType;
    }

    // --- SETTERS ---
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setMfgDate(String mfgDate) {
        this.mfgDate = mfgDate;
    }

    public void setValidityDays(int validityDays) {
        this.validityDays = validityDays;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
    }
}
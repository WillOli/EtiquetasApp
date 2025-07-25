package model;

public class ValidadePrintRequest {
    private String productName;
    private String mfgDate;
    private int validityDays;
    private int quantity;
    private String labelType;

    // Getters
    public String getProductName() { return productName; }
    public String getMfgDate() { return mfgDate; }
    public int getValidityDays() { return validityDays; }
    public int getQuantity() { return quantity; }
    public String getLabelType() { return labelType; }
}
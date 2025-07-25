package model;

public class ValidadePrintRequest {
    private String productName;
    private String mfgDate;
    private String expDate;
    private int quantity;
    private String labelType;

    // Getters
    public String getProductName() { return productName; }
    public String getMfgDate() { return mfgDate; }
    public String getExpDate() { return expDate; }
    public int getQuantity() { return quantity; }
    public String getLabelType() { return labelType; }
}
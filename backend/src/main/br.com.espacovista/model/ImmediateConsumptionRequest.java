package model;

public class ImmediateConsumptionRequest {
    private String productName;
    private String dataFabricacao;
    private String validade;
    private int quantity;
    private PrintRequest.LabelType labelType; // <-- Deve usar o Enum do PrintRequest

    // Getters e Setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDataFabricacao() { return dataFabricacao; }
    public void setDataFabricacao(String dataFabricacao) { this.dataFabricacao = dataFabricacao; }

    public String getValidade() { return validade; }
    public void setValidade(String validade) { this.validade = validade; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public PrintRequest.LabelType getLabelType() { return labelType; }
    public void setLabelType(PrintRequest.LabelType labelType) { this.labelType = labelType; }
}
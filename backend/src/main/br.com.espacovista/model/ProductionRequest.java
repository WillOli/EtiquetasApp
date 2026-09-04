package model;

public class ProductionRequest {
    private String productName;
    private String dataPreparacao; // Mudado de LocalDate para String
    private String horarioPreparo; // Mudado de LocalTime para String
    private String horarioDescarte; // Mudado de LocalTime para String
    private String dataValidade;     // Mudado de LocalDate para String
    private int quantity;
    private PrintRequest.LabelType labelType;

    // Getters e Setters (ajuste para aceitar e retornar String)
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDataPreparacao() { return dataPreparacao; }
    public void setDataPreparacao(String dataPreparacao) { this.dataPreparacao = dataPreparacao; }

    public String getHorarioPreparo() { return horarioPreparo; }
    public void setHorarioPreparo(String horarioPreparo) { this.horarioPreparo = horarioPreparo; }

    public String getHorarioDescarte() { return horarioDescarte; }
    public void setHorarioDescarte(String horarioDescarte) { this.horarioDescarte = horarioDescarte; }

    public String getDataValidade() { return dataValidade; }
    public void setDataValidade(String dataValidade) { this.dataValidade = dataValidade; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public PrintRequest.LabelType getLabelType() { return labelType; }
    public void setLabelType(PrintRequest.LabelType labelType) { this.labelType = labelType; }
}
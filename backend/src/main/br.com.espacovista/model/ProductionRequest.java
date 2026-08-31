package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class ProductionRequest {
    private String productName;
    private LocalDate dataPreparacao;
    private LocalTime horarioPreparo;
    private LocalTime horarioDescarte;
    private LocalDate dataValidade;
    private int quantity;
    private PrintRequest.LabelType labelType;

    // Getters e Setters atualizados para LocalDate e LocalTime
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public LocalDate getDataPreparacao() { return dataPreparacao; }
    public void setDataPreparacao(LocalDate dataPreparacao) { this.dataPreparacao = dataPreparacao; }

    public LocalTime getHorarioPreparo() { return horarioPreparo; }
    public void setHorarioPreparo(LocalTime horarioPreparo) { this.horarioPreparo = horarioPreparo; }

    public LocalTime getHorarioDescarte() { return horarioDescarte; }
    public void setHorarioDescarte(LocalTime horarioDescarte) { this.horarioDescarte = horarioDescarte; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public PrintRequest.LabelType getLabelType() { return labelType; }
    public void setLabelType(PrintRequest.LabelType labelType) { this.labelType = labelType; }
}
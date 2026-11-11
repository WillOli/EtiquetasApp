package service;

import model.ImmediateConsumptionRequest;
import model.PrintRequest;
import model.ProductionRequest;
import model.ValidadePrintRequest;

/** Stable application boundary for HTTP controllers or another Java application. */
public interface LabelPrintingUseCase {
    void printLabels(PrintRequest request);
    void printValidadeLabel(ValidadePrintRequest request);
    void printImmediateConsumptionLabel(ImmediateConsumptionRequest request);
    void printProductionLabel(ProductionRequest request);
}

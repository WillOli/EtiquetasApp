package service;

import model.PrintRequest;
import model.ProductionRequest;
import model.ValidadePrintRequest;
import model.ImmediateConsumptionRequest;
import service.strategies.*;

public class PrinterStrategyFactory {

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta simples.
     */
    public static ILabelStrategy getStrategy(PrintRequest request) {
        int requestedQuantity = request.getQuantity();

        int physicalLabelCount =
                request.getLabelType().isDoubleLabel()
                        ? requestedQuantity * 2
                        : requestedQuantity;

        long proximoRegistro =
                SequenceManager.getNextSequenceAndIncrement(
                        physicalLabelCount
                );

        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new SimpleLayoutStrategy(
                    request.getText(),
                    requestedQuantity,
                    request.getSetor(),
                    request.getDataFabricacao(),
                    request.getDataValidade(),
                    proximoRegistro
            );
        } else if (request.getLabelType() == PrintRequest.LabelType.STANDARD) {
            return new SimpleStandardStrategy(
                    request.getText(),
                    request.getSetor(),
                    request.getDataFabricacao(),
                    request.getDataValidade(),
                    proximoRegistro,
                    requestedQuantity
            );
        } else if (request.getLabelType().isDoubleLabel()) {
            return new SimpleStandardStrategy(
                    request.getText(), request.getSetor(), request.getDataFabricacao(),
                    request.getDataValidade(), proximoRegistro, requestedQuantity,
                    request.getLabelType().getWidthMm()
            );
        } else {
            return new SimpleLayoutStrategy(
                    request.getText(), requestedQuantity, request.getSetor(),
                    request.getDataFabricacao(), request.getDataValidade(), proximoRegistro,
                    request.getLabelType().getWidthMm(), request.getLabelType().getHeightMm()
            );
        }
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de validade.
     */
    public static ILabelStrategy getStrategy(ValidadePrintRequest request) {
        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new ValidadeLayoutStrategy(request);
        } else if (request.getLabelType() == PrintRequest.LabelType.STANDARD) {
            return new ValidadeStandardStrategy(request);
        } else if (request.getLabelType().isDoubleLabel()) {
            return new ValidadeStandardStrategy(request);
        } else {
            return new ValidadeLayoutStrategy(request, request.getLabelType().getWidthMm(), request.getLabelType().getHeightMm());
        }
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de consumo imediato.
     */
    public static ILabelStrategy getStrategy(ImmediateConsumptionRequest request) {
        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new ImmediateConsumptionLayoutStrategy(request);
        } else if (request.getLabelType() == PrintRequest.LabelType.STANDARD) {
            return new ImmediateConsumptionStandardStrategy(request);
        } else if (request.getLabelType().isDoubleLabel()) {
            return new ImmediateConsumptionStandardStrategy(request);
        } else {
            return new ImmediateConsumptionLayoutStrategy(request, request.getLabelType().getWidthMm(), request.getLabelType().getHeightMm());
        }
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de produção.
     */
    public static ILabelStrategy getStrategy(ProductionRequest request) {
        return new ProductionLayoutStrategy(request);
    }
}

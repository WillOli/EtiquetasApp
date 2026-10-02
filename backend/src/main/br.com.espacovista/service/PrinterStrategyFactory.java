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
                request.getLabelType() == PrintRequest.LabelType.STANDARD
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
        } else {
            return new SimpleStandardStrategy(
                    request.getText(),
                    request.getSetor(),
                    request.getDataFabricacao(),
                    request.getDataValidade(),
                    proximoRegistro,
                    requestedQuantity
            );
        }
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de validade.
     */
    public static ILabelStrategy getStrategy(ValidadePrintRequest request) {
        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new ValidadeLayoutStrategy(request);
        } else {
            return new ValidadeStandardStrategy(request);
        }
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de consumo imediato.
     */
    public static ILabelStrategy getStrategy(ImmediateConsumptionRequest request) {
        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new ImmediateConsumptionLayoutStrategy(request);
        } else {
            return new ImmediateConsumptionStandardStrategy(request);
        }
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de produção.
     */
    public static ILabelStrategy getStrategy(ProductionRequest request) {
        return new ProductionLayoutStrategy(request);
    }
}
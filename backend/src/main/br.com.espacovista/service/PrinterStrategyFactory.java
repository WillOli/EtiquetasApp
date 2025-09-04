package service;

import model.PrintRequest;
import model.ValidadePrintRequest;
import model.ImmediateConsumptionRequest;
import service.strategies.*;

public class PrinterStrategyFactory {

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta simples.
     */
    public static ILabelStrategy getStrategy(PrintRequest request) {
        // A lógica aqui permanece a mesma.
        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new SimpleLayoutStrategy(request.getText(), request.getQuantity());
        } else {
            return new SimpleStandardStrategy(request.getText(), request.getQuantity());
        }
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de validade.
     */
    public static ILabelStrategy getStrategy(ValidadePrintRequest request) {
        // --- CÓDIGO SIMPLIFICADO ---
        // A conversão manual foi removida. Agora acessamos o tipo diretamente.
        // A biblioteca Gson lida com a conversão do JSON ("STANDARD") para o enum (LabelType.STANDARD) automaticamente.
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
        return new ImmediateConsumptionStrategy(request);
    }
}

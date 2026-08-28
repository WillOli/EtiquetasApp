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
        // 1. Geramos o número sequencial antes do if/else para que AMBOS os tipos de etiqueta tenham registro
        long proximoRegistro = SequenceManager.getNextSequenceAndIncrement(request.getQuantity());

        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new SimpleLayoutStrategy(
                    request.getText(),
                    request.getQuantity(),
                    request.getSetor(),
                    request.getDataFabricacao(),
                    request.getDataValidade(),
                    proximoRegistro
            );
        } else {
            // ✅ CORREÇÃO: Agora formatamos o registro e repassamos TODOS os dados para a Etiqueta Dupla!
            String regFormatado = String.format("%05d", proximoRegistro);
            return new SimpleStandardStrategy(
                    request.getText(),
                    request.getSetor(),
                    request.getDataFabricacao(),
                    request.getDataValidade(),
                    regFormatado,
                    request.getQuantity()
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
}
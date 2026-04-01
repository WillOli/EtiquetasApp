package service;

import model.PrintRequest;
import model.ValidadePrintRequest;
import model.ImmediateConsumptionRequest;
import service.strategies.*;

public class PrinterStrategyFactory {

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta simples.
     * Ajustado para passar o setor (cargo) para a estratégia de layout.
     */
    public static ILabelStrategy getStrategy(PrintRequest request) {
        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            // O ERRO ESTAVA AQUI: Faltava o request.getSector() no meio
            return new SimpleLayoutStrategy(
                    request.getText(),
                    request.getSector(), // <-- ADICIONE ESTA LINHA
                    request.getQuantity()
            );
        } else {
            // Se a Standard também foi atualizada, adicione o setor aqui também.
            // Caso contrário, mantenha como está.
            return new SimpleStandardStrategy(request.getText(), request.getQuantity());
        }
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de validade.
     */
    public static ILabelStrategy getStrategy(ValidadePrintRequest request) {
        // A biblioteca Gson lida com a conversão do JSON para o enum automaticamente.
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
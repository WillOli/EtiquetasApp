package service;

import model.PrintRequest;
import model.ValidadePrintRequest;
import service.strategies.*;

public class PrinterStrategyFactory {

    public static ILabelStrategy getStrategy(PrintRequest request) {
        if (request.getLabelType() == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new SimpleLayoutStrategy(request.getText(), request.getQuantity());
        } else {
            return new SimpleStandardStrategy(request.getText(), request.getQuantity());
        }
    }

    public static ILabelStrategy getStrategy(ValidadePrintRequest request) {
        PrintRequest.LabelType labelType = PrintRequest.LabelType.valueOf(request.getLabelType().toUpperCase());
        if (labelType == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return new ValidadeLayoutStrategy(request);
        } else {
            return new ValidadeStandardStrategy(request);
        }
    }
}
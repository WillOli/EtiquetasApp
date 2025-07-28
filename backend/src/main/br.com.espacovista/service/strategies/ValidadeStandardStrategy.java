package service.strategies;

import model.ValidadePrintRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static service.ZplConstants.*;

public class ValidadeStandardStrategy implements ILabelStrategy {
    private final ValidadePrintRequest request;
    private final int quantity;

    public ValidadeStandardStrategy(ValidadePrintRequest request) {
        this.request = request;
        this.quantity = request.getQuantity() * 2; // Dupla
    }

    @Override
    public String generateZpl() {
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate manufacturingDate = LocalDate.parse(request.getMfgDate());
        String formattedMfgDate = manufacturingDate.format(displayFormatter);
        String labelText = String.format("%s\\&Fab: %s\\&Prazo: %d dias",
                request.getProductName(), formattedMfgDate, request.getValidityDays());

        // Reutiliza a lógica da Etiqueta Simples Padrão
        return new SimpleStandardStrategy(labelText, request.getQuantity()).generateZpl();
    }
}
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import model.PrintRequest;
import model.ValidadePrintRequest;
import service.PrinterService;

public class PrintController {
    private final PrinterService printerService;
    private final Gson gson = new Gson();

    public PrintController(PrinterService printerService) {
        this.printerService = printerService;
    }

    public void handlePrintRequest(Context ctx) {
        try {
            PrintRequest printRequest = gson.fromJson(ctx.body(), PrintRequest.class);
            if (printRequest == null || printRequest.getText() == null || printRequest.getText().trim().isEmpty()) {
                ctx.status(400).result("Erro: Texto da etiqueta não pode ser vazio.");
                return;
            }
            // MUDANÇA: Passa o objeto inteiro
            printerService.printLabels(printRequest);
            ctx.status(200).result("Impressão enviada com sucesso!");
        } catch (Exception e) {
            ctx.status(500).result("Erro interno no servidor: " + e.getMessage());
        }
    }

    public void handleValidadePrintRequest(Context ctx) {
        try {
            ValidadePrintRequest request = gson.fromJson(ctx.body(), ValidadePrintRequest.class);
            // ... (validações permanecem as mesmas)

            // MUDANÇA: Passa o objeto inteiro
            printerService.printValidadeLabel(request);
            ctx.status(200).result("Etiqueta de validade enviada com sucesso!");
        } catch (Exception e) {
            ctx.status(500).result("Erro interno no servidor: " + e.getMessage());
        }
    }
}
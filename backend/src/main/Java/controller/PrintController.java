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

    // Método para etiqueta simples (sem alterações)
    public void handlePrintRequest(Context ctx) {
        try {
            PrintRequest printRequest = gson.fromJson(ctx.body(), PrintRequest.class);
            if (printRequest == null || printRequest.getText() == null || printRequest.getText().trim().isEmpty()) {
                ctx.status(400).result("Erro na requisição: Texto da etiqueta não pode ser vazio.");
                return;
            }
            printerService.printLabels(printRequest.getText(), printRequest.getQuantity(), printRequest.getLabelType());
            ctx.status(200).result("Impressão enviada com sucesso!");
        } catch (JsonSyntaxException e) {
            ctx.status(400).result("Erro na requisição: JSON mal formatado.");
        } catch (Exception e) {
            ctx.status(500).result("Erro interno no servidor: " + e.getMessage());
        }
    }

    // ALTERAÇÃO: NOVO MÉTODO para etiqueta de validade
    public void handleValidadePrintRequest(Context ctx) {
        try {
            ValidadePrintRequest request = gson.fromJson(ctx.body(), ValidadePrintRequest.class);

            if (request == null || request.getProductName() == null || request.getProductName().trim().isEmpty()) {
                ctx.status(400).result("Nome do produto não pode ser vazio.");
                return;
            }
            if (request.getMfgDate() == null || request.getExpDate() == null) {
                ctx.status(400).result("Datas de fabricação e validade são obrigatórias.");
                return;
            }

            printerService.printValidadeLabel(request);

            ctx.status(200).result("Etiqueta de validade enviada com sucesso!");
        } catch (JsonSyntaxException e) {
            ctx.status(400).result("Erro na requisição: JSON mal formatado.");
        } catch (PrinterService.PrinterServiceException e) {
            ctx.status(500).result("Erro na impressora: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Erro interno no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
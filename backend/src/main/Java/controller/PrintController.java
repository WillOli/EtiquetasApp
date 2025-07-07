package controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import model.PrintRequest;
import service.PrinterService;

public class PrintController {

    private final PrinterService printerService; // Não é mais estático
    private final Gson gson = new Gson();

    // Construtor que recebe a dependência de fora (injeção de dependência)
    public PrintController(PrinterService printerService) {
        this.printerService = printerService;
    }

    // Método de instância, não mais estático
    public void handlePrintRequest(Context ctx) {
        try {
            String requestBody = ctx.body();
            PrintRequest printRequest = gson.fromJson(requestBody, PrintRequest.class);

            if (printRequest == null || printRequest.getText() == null || printRequest.getText().trim().isEmpty()) {
                ctx.status(400).result("Erro na requisição: Texto da etiqueta não pode ser vazio.");
                return;
            }
            if (printRequest.getQuantity() <= 0) {
                ctx.status(400).result("Erro na requisição: A quantidade deve ser maior que zero.");
                return;
            }
            if (printRequest.getLabelType() == null) {
                System.err.println("[AVISO] Tipo de etiqueta não especificado, usando padrão.");
            }

            printerService.printLabels(
                    printRequest.getText(),
                    printRequest.getQuantity(),
                    printRequest.getLabelType()
            );

            ctx.status(200).result("Impressão enviada com sucesso!");

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
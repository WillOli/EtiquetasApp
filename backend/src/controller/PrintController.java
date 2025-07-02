package controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import model.PrintRequest;
import service.PrinterService;

public class PrintController {

    private static final PrinterService printerService = new PrinterService();
    private static final Gson gson = new Gson();

    public static void handlePrintRequest(Context ctx) {
        try {
            String requestBody = ctx.body();
            PrintRequest printRequest = gson.fromJson(requestBody, PrintRequest.class);

            // --- VALIDAÇÃO MELHORADA ---
            if (printRequest == null) {
                ctx.status(400).result("Erro na requisição: Pedido vazio ou mal formatado.");
                return;
            }
            if (printRequest.getText() == null || printRequest.getText().trim().isEmpty()) {
                ctx.status(400).result("Erro na requisição: O texto da etiqueta não pode ser vazio.");
                return;
            }
            if (printRequest.getQuantity() <= 0) {
                ctx.status(400).result("Erro na requisição: A quantidade deve ser maior que zero.");
                return;
            }
            if (printRequest.getLabelType() == null) {
                // O construtor do PrintRequest já define um padrão, mas esta é uma segurança extra.
                System.err.println("[AVISO] Tipo de etiqueta não especificado, usando padrão.");
            }
            // --- FIM DA VALIDAÇÃO ---


            printerService.printLabels(
                    printRequest.getText(),
                    printRequest.getQuantity(),
                    printRequest.getLabelType()
            );

            ctx.status(200).result("Impressão enviada com sucesso!");

        } catch (JsonSyntaxException e) {
            ctx.status(400).result("Erro na requisição: JSON mal formatado. Corpo recebido: " + ctx.body());
        } catch (PrinterService.PrinterServiceException e) {
            // Captura uma exceção específica do nosso serviço de impressão
            System.err.println("Erro de serviço de impressão: " + e.getMessage());
            ctx.status(500).result("Erro na impressora: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro interno inesperado: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).result("Erro interno no servidor: Ocorreu uma falha inesperada.");
        }
    }
}
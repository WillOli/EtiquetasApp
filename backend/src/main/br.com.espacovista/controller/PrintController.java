package controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import model.ImmediateConsumptionRequest;
import model.PrintRequest;
import model.ValidadePrintRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.PrinterService;

public class PrintController {
    private final PrinterService printerService;
    private final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(PrintController.class);

    public PrintController(PrinterService printerService) {
        this.printerService = printerService;
    }

    public void handlePrintRequest(Context ctx) {
        try {
            PrintRequest printRequest = gson.fromJson(ctx.body(), PrintRequest.class);
            logger.info("Recebida requisição para /print: {}", ctx.body());

            if (printRequest == null || printRequest.getText() == null || printRequest.getText().trim().isEmpty()) {
                logger.warn("Requisição /print inválida: texto da etiqueta vazio.");
                ctx.status(400).result("Erro: Texto da etiqueta não pode ser vazio.");
                return;
            }

            printerService.printLabels(printRequest);
            ctx.status(200).result("Impressão enviada com sucesso!");

        } catch (JsonSyntaxException e) {
            logger.warn("Erro de sintaxe no JSON recebido em /print.", e);
            ctx.status(400).result("Erro: Formato do JSON inválido.");
        } catch (Exception e) {
            // O logger registra a exceção completa no log para que você possa depurar.
            logger.error("Erro interno no servidor ao processar /print.", e);

            // --- ALTERAÇÃO DE SEGURANÇA ---
            // Retorna uma mensagem genérica para o cliente, sem expor detalhes internos.
            ctx.status(500).result("Ocorreu um erro inesperado no servidor.");
        }
    }

    public void handleValidadePrintRequest(Context ctx) {
        try {
            ValidadePrintRequest request = gson.fromJson(ctx.body(), ValidadePrintRequest.class);
            logger.info("Recebida requisição para /print-validade: {}", ctx.body());

            // TODO: Adicionar validações para os campos de ValidadePrintRequest
            if (request == null || request.getProductName() == null || request.getProductName().trim().isEmpty()) {
                logger.warn("Requisição /print-validade inválida: nome do produto vazio.");
                ctx.status(400).result("Erro: Nome do produto não pode ser vazio.");
                return;
            }

            printerService.printValidadeLabel(request);
            ctx.status(200).result("Etiqueta de validade enviada com sucesso!");

        } catch (JsonSyntaxException e) {
            logger.warn("Erro de sintaxe no JSON recebido em /print-validade.", e);
            ctx.status(400).result("Erro: Formato do JSON inválido.");
        } catch (Exception e) {
            // O logger registra a exceção completa no log.
            logger.error("Erro interno no servidor ao processar /print-validade.", e);

            // --- ALTERAÇÃO DE SEGURANÇA ---
            // Retorna uma mensagem genérica para o cliente.
            ctx.status(500).result("Ocorreu um erro inesperado no servidor.");
        }
    }

    public void handleImmediateConsumptionRequest(Context ctx) {
        try {
            ImmediateConsumptionRequest request = gson.fromJson(ctx.body(), ImmediateConsumptionRequest.class);
            logger.info("Recebida requisição para /print-consumo-imediato: {}", ctx.body());

            if (request == null || request.getProductName() == null || request.getProductName().trim().isEmpty()) {
                logger.warn("Requisição /print-consumo-imediato inválida: nome do produto vazio.");
                ctx.status(400).result("Erro: Nome do produto não pode ser vazio.");
                return;
            }

            printerService.printImmediateConsumptionLabel(request);
            ctx.status(200).result("Etiqueta de consumo imediato enviada com sucesso!");

        } catch (JsonSyntaxException e) {
            logger.warn("Erro de sintaxe no JSON recebido em /print-consumo-imediato.", e);
            ctx.status(400).result("Erro: Formato do JSON inválido.");
        } catch (Exception e) {
            logger.error("Erro interno no servidor ao processar /print-consumo-imediato.", e);
            ctx.status(500).result("Ocorreu um erro inesperado no servidor.");
        }
    }
}

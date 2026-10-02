package controller;

import com.google.gson.*;
import io.javalin.http.Context;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.PrinterService;
import service.SequenceException;
import validation.PrintRequestValidator;
import validation.RequestValidationException;
import java.util.function.Consumer;

public class PrintController {
    private static final Logger logger = LoggerFactory.getLogger(PrintController.class);
    private final PrinterService printerService;
    private final Gson gson = new GsonBuilder().setStrictness(Strictness.STRICT).create();

    public PrintController(PrinterService printerService) { this.printerService = printerService; }

    public void handlePrintRequest(Context ctx) {
        handle(ctx, PrintRequest.class, PrintRequestValidator::validate, printerService::printLabels);
    }
    public void handleValidadePrintRequest(Context ctx) {
        handle(ctx, ValidadePrintRequest.class, PrintRequestValidator::validate, printerService::printValidadeLabel);
    }
    public void handleImmediateConsumptionRequest(Context ctx) {
        handle(ctx, ImmediateConsumptionRequest.class, PrintRequestValidator::validate, printerService::printImmediateConsumptionLabel);
    }
    public void handleProductionRequest(Context ctx) {
        handle(ctx, ProductionRequest.class, PrintRequestValidator::validate, printerService::printProductionLabel);
    }

    private <T> void handle(Context ctx, Class<T> type, Consumer<T> validate, Consumer<T> print) {
        ctx.contentType("text/plain; charset=utf-8");
        try {
            T request = gson.fromJson(ctx.body(), type);
            validate.accept(request);
            print.accept(request);
            ctx.status(200).result("Pedido enviado à fila da impressora. Confira a saída das etiquetas.");
        } catch (io.javalin.http.HttpResponseException e) {
            throw e;
        } catch (JsonParseException e) {
            logger.warn("JSON inválido em {}.", ctx.path());
            ctx.status(400).result("Formato do JSON inválido.");
        } catch (RequestValidationException e) {
            logger.warn("Pedido inválido em {}: {}", ctx.path(), e.getMessage());
            ctx.status(400).result(e.getMessage());
        } catch (PrinterService.PrinterServiceException e) {
            logger.error("Impressora indisponível em {}.", ctx.path(), e);
            ctx.status(503).result(e.getMessage());
        } catch (SequenceException e) {
            logger.error("Falha na sequência em {}.", ctx.path(), e);
            ctx.status(503).result("Impressão bloqueada: não foi possível garantir o registro. Consulte o responsável pela sequência.");
        } catch (Exception e) {
            logger.error("Erro interno em {}.", ctx.path(), e);
            ctx.status(500).result("Ocorreu um erro inesperado no servidor.");
        }
    }
}

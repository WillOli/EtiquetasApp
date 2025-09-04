package service;

import model.ImmediateConsumptionRequest;
import model.PrintRequest;
import model.ValidadePrintRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.strategies.ILabelStrategy;

import javax.print.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class PrinterService {

    // Cria uma instância do logger para esta classe
    private static final Logger logger = LoggerFactory.getLogger(PrinterService.class);

    public static class PrinterServiceException extends RuntimeException {
        public PrinterServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void printLabels(PrintRequest request) {
        ILabelStrategy strategy = PrinterStrategyFactory.getStrategy(request);
        String zpl = strategy.generateZpl();
        sendZplToPrinter(zpl, request.getQuantity(), request.getLabelType().name());
    }

    public void printValidadeLabel(ValidadePrintRequest request) {
        ILabelStrategy strategy = PrinterStrategyFactory.getStrategy(request);
        String zpl = strategy.generateZpl();
        sendZplToPrinter(zpl, request.getQuantity(), request.getLabelType().name());
    }

    public void printImmediateConsumptionLabel(ImmediateConsumptionRequest request) {
        ILabelStrategy strategy = PrinterStrategyFactory.getStrategy(request);
        String zpl = strategy.generateZpl();
        sendZplToPrinter(zpl, request.getQuantity(), request.getLabelType().name());
    }

    private void sendZplToPrinter(String zpl, int requestedQuantity, String labelType) {
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultService == null) {
            // Log de erro grave
            logger.error("Nenhuma impressora padrão encontrada no sistema.");
            throw new PrinterServiceException("Nenhuma impressora padrão foi encontrada no servidor.", null);
        }
        try {
            InputStream is = new ByteArrayInputStream(zpl.getBytes("UTF-8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();

            // Log de informação com parâmetros
            logger.info("Enviando impressão. Tipo: {}, Impressora: {}, Quantidade: {}", labelType, defaultService.getName(), requestedQuantity);

            job.print(doc, null);
            logger.info("Impressão enviada com sucesso para a fila da impressora.");
        } catch (Exception e) {
            // Log de erro com a exceção completa para diagnóstico
            logger.error("Falha ao enviar ZPL para a impressora.", e);
            throw new PrinterServiceException("Erro ao comunicar com a impressora.", e);
        }
    }
}

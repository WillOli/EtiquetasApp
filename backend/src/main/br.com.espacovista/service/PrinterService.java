package service;

import model.PrintRequest;
import model.ValidadePrintRequest;
import service.strategies.ILabelStrategy;
import javax.print.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;

public class PrinterService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static class PrinterServiceException extends RuntimeException {
        public PrinterServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Ponto de entrada para impressão de Etiqueta Simples.
     */
    public void printLabels(PrintRequest request) {
        ILabelStrategy strategy = PrinterStrategyFactory.getStrategy(request);
        String zpl = strategy.generateZpl();
        sendZplToPrinter(zpl, request.getQuantity(), request.getLabelType().toString());
    }

    /**
     * Ponto de entrada para impressão de Etiqueta de Validade.
     */
    public void printValidadeLabel(ValidadePrintRequest request) {
        ILabelStrategy strategy = PrinterStrategyFactory.getStrategy(request);
        String zpl = strategy.generateZpl();
        sendZplToPrinter(zpl, request.getQuantity(), request.getLabelType());
    }

    /**
     * Método centralizado e final para enviar o ZPL para a impressora.
     */
    private void sendZplToPrinter(String zpl, int requestedQuantity, String labelType) {
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultService == null) {
            log("[ERRO GRAVE] Nenhuma impressora padrão encontrada no sistema.");
            throw new PrinterServiceException("Nenhuma impressora padrão foi encontrada no servidor.", null);
        }
        try {
            InputStream is = new ByteArrayInputStream(zpl.getBytes("UTF-8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();

            log(String.format("[IMPRESSÃO] Tipo: %s, Impressora: %s, Quantidade: %d", labelType, defaultService.getName(), requestedQuantity));

            job.print(doc, null);
            log("[STATUS] Impressão enviada com sucesso.");
        } catch (Exception e) {
            log("[ERRO] Falha ao enviar para impressora: " + e.getMessage());
            throw new PrinterServiceException("Erro ao comunicar com a impressora.", e);
        }
    }

    private void log(String message) {
        String timestampedMessage = String.format("[%s] %s", FORMATTER.format(LocalDateTime.now()), message);
        System.out.println(timestampedMessage);
        try (FileWriter writer = new FileWriter("logs/impressao.log", true)) {
            writer.write(timestampedMessage + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Falha ao escrever no arquivo de log: " + e.getMessage());
        }
    }
}
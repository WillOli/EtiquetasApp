package service;

import config.AppConfig;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.strategies.ILabelStrategy;
import validation.PrintRequestValidator;

import javax.print.*;
import javax.print.attribute.standard.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Supplier;

public class PrinterService {
    private static final Logger logger = LoggerFactory.getLogger(PrinterService.class);
    private final Supplier<String> printerName;
    private final Supplier<PrintService[]> printers;
    private final Sender sender;

    @FunctionalInterface interface Sender { void send(PrintService printer, String zpl) throws Exception; }

    public PrinterService() {
        this(AppConfig::getPrinterName, () -> PrintServiceLookup.lookupPrintServices(null, null), PrinterService::send);
    }

    PrinterService(Supplier<String> printerName, Supplier<PrintService[]> printers, Sender sender) {
        this.printerName = printerName;
        this.printers = printers;
        this.sender = sender;
    }

    public static class PrinterServiceException extends RuntimeException {
        public PrinterServiceException(String message, Throwable cause) { super(message, cause); }
    }

    public void printLabels(PrintRequest request) {
        PrintRequestValidator.validate(request);
        print(() -> PrinterStrategyFactory.getStrategy(request), request.getQuantity(), request.getLabelType().name());
    }
    public void printValidadeLabel(ValidadePrintRequest request) {
        PrintRequestValidator.validate(request);
        print(() -> PrinterStrategyFactory.getStrategy(request), request.getQuantity(), "VALIDADE");
    }
    public void printImmediateConsumptionLabel(ImmediateConsumptionRequest request) {
        PrintRequestValidator.validate(request);
        print(() -> PrinterStrategyFactory.getStrategy(request), request.getQuantity(), "CONSUMO_IMEDIATO");
    }
    public void printProductionLabel(ProductionRequest request) {
        PrintRequestValidator.validate(request);
        print(() -> PrinterStrategyFactory.getStrategy(request), request.getQuantity(), "PRODUCAO");
    }

    private void print(Supplier<ILabelStrategy> strategy, int quantity, String type) {
        // Confirma a impressora antes de consumir registros. Falha posterior pode deixar lacunas, nunca reutilizar registros.
        PrintService printer = selectPrinter();
        String zpl = strategy.get().generateZpl();
        logger.debug("ZPL gerado: {}", zpl);
        try {
            sender.send(printer, zpl);
            logger.info("Pedido enviado à fila. Tipo: {}, Impressora: {}, Quantidade solicitada: {}",
                    type, printer.getName(), quantity);
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            logger.error("Falha ao enviar ZPL à impressora {}.", printer.getName(), e);
            throw new PrinterServiceException("Não foi possível enviar o pedido à fila da impressora. Verifique a conexão e a fila.", e);
        }
    }

    PrintService selectPrinter() {
        String configured = printerName.get();
        if (configured == null || configured.isBlank())
            throw new PrinterServiceException("Configure printer.name com o nome exato da Zebra.", null);
        PrintService selected = null;
        for (PrintService candidate : printers.get()) {
            PrinterName internal = candidate.getAttribute(PrinterName.class);
            if (configured.equals(candidate.getName()) || (internal != null && configured.equals(internal.getValue()))) {
                if (selected != null) throw new PrinterServiceException("Mais de uma impressora corresponde ao nome configurado.", null);
                selected = candidate;
            }
        }
        if (selected == null) throw new PrinterServiceException("A impressora configurada não foi encontrada. Verifique a configuração e a conexão.", null);
        if (PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(selected.getAttribute(PrinterIsAcceptingJobs.class))
                || PrinterState.STOPPED.equals(selected.getAttribute(PrinterState.class)))
            throw new PrinterServiceException("A fila da impressora está pausada ou indisponível.", null);
        return selected;
    }

    private static void send(PrintService printer, String zpl) throws Exception {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac")) {
            String receipt = new CupsZplPrinter().print(cupsQueueName(printer), zpl);
            logger.debug("Recibo CUPS: {}", receipt);
        } else {
            try (var data = new java.io.ByteArrayInputStream(zpl.getBytes(StandardCharsets.UTF_8))) {
                Doc doc = new SimpleDoc(data, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
                printer.createPrintJob().print(doc, null);
            }
        }
    }

    static String cupsQueueName(PrintService printer) {
        PrinterName name = printer.getAttribute(PrinterName.class);
        if (name == null || name.getValue().isBlank())
            throw new PrinterServiceException("O nome interno da fila CUPS não está disponível.", null);
        return name.getValue();
    }
}

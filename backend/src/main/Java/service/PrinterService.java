package service;

import model.PrintRequest;
import model.ValidadePrintRequest;

import javax.print.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class PrinterService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int LABEL_WIDTH_MM_STANDARD = 40;
    private static final int LABEL_HEIGHT_MM_STANDARD = 25;
    private static final int FONT_HEIGHT_STANDARD = 36;
    private static final int DOTS_PER_MM = 8;
    private static final int LABEL_WIDTH_MM_SIXTY_TWO_MM = 80;
    private static final int LABEL_HEIGHT_MM_SIXTY_TWO_MM = 25;
    private static final int FONT_HEIGHT_SIXTY_TWO_MM = 36;
    private static final int FONT_WIDTH_SIXTY_TWO_MM = 36;
    private static final int GAP_HORIZONTAL_DOTS = 10;

    public static class PrinterServiceException extends RuntimeException {
        public PrinterServiceException(String message) {
            super(message);
        }
    }

    private String getCurrentLogFilename() {
        DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String monthYear = LocalDateTime.now().format(fileFormatter);
        return "logs_" + monthYear + ".txt";
    }

    /**
     * Lógica principal para impressão da Etiqueta Simples.
     */
    public void printLabels(String labelText, int quantity, PrintRequest.LabelType type) throws PrinterServiceException {
        log("### DEBUG: TIPO DE ETIQUETA RECEBIDO = " + type.toString() + " ###");
        if (labelText == null || labelText.trim().isEmpty()) {
            log("[ERRO] Texto da etiqueta está vazio.");
            return;
        }
        int actualQuantity = (type == PrintRequest.LabelType.STANDARD) ? quantity * 2 : quantity;
        String zpl = generateZpl(labelText, actualQuantity, type);

        sendZplToPrinter(zpl, quantity, actualQuantity, type.toString());
    }

    /**
     * Lógica principal para impressão da Etiqueta de Validade.
     */
    public void printValidadeLabel(ValidadePrintRequest request) throws PrinterServiceException {
        try {
            PrintRequest.LabelType labelType = PrintRequest.LabelType.valueOf(request.getLabelType().toUpperCase());
            int quantity = request.getQuantity();
            int actualQuantity = (labelType == PrintRequest.LabelType.STANDARD) ? quantity * 2 : quantity;
            String zpl;

            // Pega os dados da requisição
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate manufacturingDate = LocalDate.parse(request.getMfgDate());
            String formattedMfgDate = manufacturingDate.format(displayFormatter);
            int validityDays = request.getValidityDays();
            String productName = request.getProductName();

            // Decide qual ZPL gerar com base no tipo de etiqueta
            if (labelType == PrintRequest.LabelType.SIXTY_TWO_MM) {
                // Se for a etiqueta grande (Padrão), usa o novo método de layout para validade
                zpl = generateValidadeLayoutZPL(productName, formattedMfgDate, validityDays, actualQuantity);
            } else {
                // Se for a etiqueta pequena (Dupla), mantém o texto condensado
                String labelText = String.format("%s\\&Fab: %s\\&Prazo: %d dias", productName, formattedMfgDate, validityDays);
                zpl = generateStandardDualLabelZPL(labelText, actualQuantity);
            }

            sendZplToPrinter(zpl, quantity, actualQuantity, labelType.toString());

        } catch (Exception e) {
            log("[ERRO] Falha ao processar etiqueta de validade: " + e.getMessage());
            throw new PrinterServiceException("Falha ao processar etiqueta de validade: " + e.getMessage());
        }
    }

    /**
     * Novo método centralizado para enviar o ZPL final para a impressora.
     */
    private void sendZplToPrinter(String zpl, int requestedQuantity, int actualQuantity, String labelType) {
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultService == null) {
            log("[ERRO GRAVE] Nenhuma impressora padrão encontrada no sistema.");
            throw new PrinterServiceException("Nenhuma impressora padrão foi encontrada no servidor.");
        }
        try {
            InputStream is = new ByteArrayInputStream(zpl.getBytes("UTF-8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();
            String logMessage = String.format(
                    "[IMPRESSÃO] %s\nTipo de Etiqueta: %s\nImpressora: %s\nQuantidade solicitada: %d\nQuantidade real impressa: %d\nZPL: %s",
                    FORMATTER.format(LocalDateTime.now()), labelType, defaultService.getName(),
                    requestedQuantity, actualQuantity, formatZplForLogging(zpl));
            log(logMessage);
            job.print(doc, null);
            log("[STATUS] Impressão enviada com sucesso.");
        } catch (Exception e) {
            log("[ERRO] Erro ao enviar ZPL para impressora: " + e.getMessage());
            throw new PrinterServiceException("Erro ao comunicar com a impressora: " + e.getMessage());
        }
    }

    private String generateZpl(String labelText, int actualQuantity, PrintRequest.LabelType type) {
        if (type == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return generateLayoutLabelZPL(labelText, actualQuantity);
        }
        return generateStandardDualLabelZPL(labelText, actualQuantity);
    }

    public String generateStandardDualLabelZPL(String labelText, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM;
        int pageWidthDots = labelWidthDots * 2 + GAP_HORIZONTAL_DOTS * 2;
        int fontHeight = FONT_HEIGHT_STANDARD;
        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < actualQuantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n^CI28\n^PW").append(pageWidthDots).append("\n^LL").append(labelHeightDots).append("\n");
            }
            for (int col = 0; col < 2; col++) {
                int posX = 20 + col * (labelWidthDots + GAP_HORIZONTAL_DOTS);
                zplBuilder.append("^FO").append(posX).append(",40")
                        .append("^A0N,").append(fontHeight-10).append(",").append(fontHeight-10)
                        .append("^FB").append(labelWidthDots-40).append(",3,0,C,0")
                        .append("^FD").append(labelText).append("^FS\n");
            }
            if (i % 2 == 1 || i == actualQuantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }

    /**
     * Gera o ZPL para a "Etiqueta Simples" na etiqueta "Padrão (80x25mm)"
     */
    public String generateLayoutLabelZPL(String userText, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int fontSize = 28;
        int textMargin = 30;
        int valueMargin = 200;
        int lineWidth = 400;
        int lineMargin = 30;
        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < actualQuantity; i++) {
            zplBuilder.append("^XA\n");
            zplBuilder.append("^CI28\n");
            zplBuilder.append("^PW").append(labelWidthDots).append("\n");
            zplBuilder.append("^LL").append(labelHeightDots).append("\n");
            int yPosProduto = 40;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosProduto).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDProduto:^FS\n");
            zplBuilder.append("^FO").append(valueMargin).append(",").append(yPosProduto).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(userText).append("^FS\n");
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosProduto + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");
            int yPosFabricacao = 95;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosFabricacao).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDFabricacao:^FS\n");
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosFabricacao + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");
            int yPosValidade = 150;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosValidade).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDValidade:^FS\n");
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosValidade + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");
            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }

    /**
     * Gera o ZPL para a "Etiqueta de Validade" na etiqueta "Padrão (80x25mm)"
     */
    public String generateValidadeLayoutZPL(String productName, String fabDate, int prazo, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int fontSize = 28;
        int textMargin = 30;
        int valueMargin = 200;
        int lineWidth = 400;
        int lineMargin = 30;

        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < actualQuantity; i++) {
            zplBuilder.append("^XA\n^CI28\n^PW").append(labelWidthDots).append("\n^LL").append(labelHeightDots).append("\n");

            // --- Linha 1: Produto ---
            int yPosProduto = 40;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosProduto).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDProduto:^FS\n");
            zplBuilder.append("^FO").append(valueMargin).append(",").append(yPosProduto).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(productName).append("^FS\n");
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosProduto + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");

            // --- Linha 2: Fabricação ---
            int yPosFabricacao = 95;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosFabricacao).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDFabricacao:^FS\n");
            zplBuilder.append("^FO").append(valueMargin).append(",").append(yPosFabricacao).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(fabDate).append("^FS\n");
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosFabricacao + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");

            // --- Linha 3: Validade (agora mostra o Prazo) ---
            int yPosValidade = 150;
            zplBuilder.append("^FO").append(textMargin).append(",").append(yPosValidade).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FDValidade:^FS\n"); // O texto fixo é "Validade:"
            zplBuilder.append("^FO").append(valueMargin).append(",").append(yPosValidade).append("^A0N,").append(fontSize).append(",").append(fontSize).append("^FD").append(prazo + " dias").append("^FS\n"); // O valor é o prazo
            zplBuilder.append("^FO").append(lineMargin).append(",").append(yPosValidade + fontSize).append("^GB").append(lineWidth).append(",2,2^FS\n");

            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }

    void log(String message) {
        String timestamped = "[" + FORMATTER.format(LocalDateTime.now()) + "] " + message;
        System.out.println(timestamped);
        String logFilename = getCurrentLogFilename();
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            try (FileWriter writer = new FileWriter(logDir.resolve(logFilename).toString(), true)) {
                writer.write(timestamped + "\n\n");
            }
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao escrever no log: " + e.getMessage());
        }
    }

    private String formatZplForLogging(String rawZpl) {
        if (rawZpl == null || rawZpl.trim().isEmpty()) {
            return "[ZPL Vazio]";
        }
        return rawZpl.trim().replace("\n", " ").replaceAll("\\s+", " ");
    }
}
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

    public void printLabels(String labelText, int quantity, PrintRequest.LabelType type) throws PrinterServiceException {
        if (labelText == null || labelText.trim().isEmpty()) {
            log("[ERRO] Texto da etiqueta está vazio.");
            return;
        }

        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

        if (defaultService == null) {
            log("[ERRO GRAVE] Nenhuma impressora padrão encontrada no sistema.");
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            log("Impressoras disponíveis: " + Arrays.toString(services));
            throw new PrinterServiceException("Nenhuma impressora padrão foi encontrada no servidor.");
        }

        int actualQuantity = (type == PrintRequest.LabelType.STANDARD) ? quantity * 2 : quantity;
        String zpl = generateZpl(labelText, actualQuantity, type);

        try {
            InputStream is = new ByteArrayInputStream(zpl.getBytes("UTF-8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();

            String logMessage = String.format(
                    "[IMPRESSÃO] %s\nTipo de Etiqueta: %s\nImpressora: %s\nQuantidade solicitada: %d\nQuantidade real impressa: %d\nZPL: %s",
                    FORMATTER.format(LocalDateTime.now()), type.toString(), defaultService.getName(),
                    quantity, actualQuantity, formatZplForLogging(zpl));
            log(logMessage);

            job.print(doc, null);
            log("[STATUS] Impressão enviada com sucesso.");

        } catch (PrintException e) {
            log("[ERRO] Erro específico da impressora: " + e.getMessage());
            throw new PrinterServiceException("Erro ao comunicar com a impressora: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log("[ERRO] Codificação UTF-8 não suportada: " + e.getMessage());
            throw new PrinterServiceException("Erro de codificação inesperado.");
        }
    }

    public void printValidadeLabel(ValidadePrintRequest request) throws PrinterServiceException {
        try {
            // Formato para exibir a data na etiqueta (dia/mês/ano)
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // 1. Pega a data de fabricação e a formata
            LocalDate manufacturingDate = LocalDate.parse(request.getMfgDate());
            String formattedMfgDate = manufacturingDate.format(displayFormatter);

            // 2. Pega o número de dias de validade diretamente da requisição
            int validityDays = request.getValidityDays();

            // 3. Monta o texto final da etiqueta com "Prazo: XX dias"
            // Usamos '\\&' para quebra de linha explícita no ZPL
            String labelText = String.format(
                    "%s\\&Fab: %s\\&Prazo: %d dias",
                    request.getProductName(),
                    formattedMfgDate,
                    validityDays
            );

            // 4. Converte o tipo de etiqueta e chama o método de impressão principal
            PrintRequest.LabelType labelType = PrintRequest.LabelType.valueOf(request.getLabelType().toUpperCase());
            printLabels(labelText, request.getQuantity(), labelType);

        } catch (IllegalArgumentException e) {
            log("[ERRO] Tipo de etiqueta inválido recebido: " + request.getLabelType());
            throw new PrinterServiceException("Tipo de etiqueta inválido: " + request.getLabelType());
        } catch (DateTimeParseException e) {
            log("[ERRO] Formato de data de fabricação inválido: " + request.getMfgDate());
            throw new PrinterServiceException("Formato de data de fabricação inválido. Use yyyy-MM-dd.");
        } catch (Exception e) {
            log("[ERRO] Falha ao processar etiqueta de validade: " + e.getMessage());
            throw new PrinterServiceException("Falha ao processar etiqueta de validade: " + e.getMessage());
        }
    }

    private String generateZpl(String labelText, int actualQuantity, PrintRequest.LabelType type) {
        if (type == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return generate80x25LabelZPL(labelText, actualQuantity);
        }
        return generateStandardDualLabelZPL(labelText, actualQuantity);
    }

    public String generateStandardDualLabelZPL(String labelText, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM;
        int pageWidthDots = labelWidthDots * 2 + GAP_HORIZONTAL_DOTS * 2;
        int fontHeight = FONT_HEIGHT_STANDARD;
        int charsPerLine = (int) Math.floor((double) labelWidthDots / fontHeight);
        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < actualQuantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n");
                zplBuilder.append("^CI28\n");
                zplBuilder.append("^PW").append(pageWidthDots).append("\n");
                zplBuilder.append("^LL").append(labelHeightDots).append("\n");
            }
            for (int col = 0; col < 2; col++) {
                int currentLabelWidthDots = labelWidthDots;
                int currentLabelHeightDots = labelHeightDots;
                String textToPrint = labelText;
                int textDisplayLength = Math.min(textToPrint.length(), charsPerLine);
                int textPixelWidth = textDisplayLength * fontHeight;
                int marginLeft = (currentLabelWidthDots - textPixelWidth) / 2;
                if (marginLeft < 0) marginLeft = 0;
                int marginTop = (currentLabelHeightDots - fontHeight) / 2;
                int posX = marginLeft + col * (labelWidthDots + GAP_HORIZONTAL_DOTS);
                int posY = marginTop;
                zplBuilder.append("^FO").append(posX).append(",").append(posY)
                        .append("^A0N,").append(fontHeight).append(",").append(fontHeight)
                        .append("^FD").append(textToPrint).append("^FS\n");
            }
            if (i % 2 == 1 || i == actualQuantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }

    public String generate80x25LabelZPL(String labelText, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int fontHeight = FONT_HEIGHT_SIXTY_TWO_MM;
        int fontWidth = FONT_WIDTH_SIXTY_TWO_MM;
        int fieldBlockWidth = labelWidthDots;
        int maxLines = (int) Math.floor((double) labelHeightDots / fontHeight);
        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < actualQuantity; i++) {
            zplBuilder.append("^XA\n");
            zplBuilder.append("^CI28\n");
            zplBuilder.append("^PW").append(labelWidthDots).append("\n");
            zplBuilder.append("^LL").append(labelHeightDots).append("\n");
            int verticalPosition = 40;
            zplBuilder.append("^FO0,").append(verticalPosition)
                    .append("^A0N,").append(fontHeight).append(",").append(fontWidth)
                    .append("^FB").append(fieldBlockWidth).append(",").append(maxLines).append(",0,C,0")
                    .append("^FD").append(labelText).append("^FS\n");
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
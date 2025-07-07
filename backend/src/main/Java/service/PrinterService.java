package service;

import model.PrintRequest;

import javax.print.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class PrinterService {
    // As suas constantes de dimensão e fonte permanecem INALTERADAS.
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_CHARACTERS = 100;
    private static final int MAX_QUANTITY = 100;
    private static final int LABEL_WIDTH_MM_STANDARD = 40;
    private static final int LABEL_HEIGHT_MM_STANDARD = 25;
    private static final int FONT_HEIGHT_STANDARD = 36;
    private static final int DOTS_PER_MM = 8;
    private static final int LABEL_WIDTH_MM_SIXTY_TWO_MM = 62;
    private static final int LABEL_HEIGHT_MM_SIXTY_TWO_MM = 62;
    private static final int FONT_HEIGHT_SIXTY_TWO_MM = 36;
    private static final int FONT_WIDTH_SIXTY_TWO_MM = 36;
    private static final int GAP_HORIZONTAL_DOTS = 10;

    public static class PrinterServiceException extends RuntimeException {
        public PrinterServiceException(String message) {
            super(message);
        }
    }

    /**
     * Gera dinamicamente o nome do ficheiro de log para o mês e ano atuais.
     * @return Uma string como "logs_2025-07.txt".
     */
    private String getCurrentLogFilename() {
        DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String monthYear = LocalDateTime.now().format(fileFormatter);
        return "logs_" + monthYear + ".txt";
    }

    // O método printLabels permanece com a sua lógica original.
    public void printLabels(String labelText, int quantity, PrintRequest.LabelType type) {
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

        String zpl = generateZpl(labelText, quantity * 2, type); // A sua lógica original de quantidade é mantida.

        try {
            InputStream is = new ByteArrayInputStream(zpl.getBytes("UTF-8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();

            String logMessage = String.format(
                    "[IMPRESSÃO] %s\nTipo de Etiqueta: %s\nImpressora: %s\nQuantidade solicitada: %d\nQuantidade real: %d\nZPL: %s",
                    FORMATTER.format(LocalDateTime.now()), type.toString(), defaultService.getName(),
                    quantity, quantity * 2, formatZplForLogging(zpl));
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

    private String generateZpl(String labelText, int actualQuantity, PrintRequest.LabelType type) {
        if (type == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return generate62mmLabelZPL(labelText, actualQuantity);
        }
        return generateStandardLabelZPL(labelText, actualQuantity);
    }

    public String generateStandardLabelZPL(String labelText, int actualQuantity) {
        // ... (este método permanece exatamente como estava no seu código)
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

    public String generate62mmLabelZPL(String labelText, int actualQuantity) {
        // ... (este método permanece exatamente como estava no seu código, com os seus ajustes de posicionamento)
        int labelWidthDots = LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int pageWidthDots = labelWidthDots * 2 + GAP_HORIZONTAL_DOTS * 2;
        int fontHeight = FONT_HEIGHT_SIXTY_TWO_MM;
        int fontWidth = FONT_WIDTH_SIXTY_TWO_MM;
        int fieldBlockWidth = labelWidthDots - -250;
        if (fieldBlockWidth <= 0) fieldBlockWidth = labelWidthDots;
        int maxLines = (int) Math.floor((double) labelHeightDots / fontHeight);
        StringBuilder zplBuilder = new StringBuilder();
        for (int i = 0; i < actualQuantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n");
                zplBuilder.append("^CI28\n");
                zplBuilder.append("^PW").append(pageWidthDots).append("\n");
                zplBuilder.append("^LL").append(labelHeightDots).append("\n");
            }
            for (int col = 0; col < 2; col++) {
                int initialYOffset = 95;
                int marginLeftBlock = 30;
                int posX = marginLeftBlock + col * (labelWidthDots + GAP_HORIZONTAL_DOTS);
                int posY = initialYOffset;
                zplBuilder.append("^FO").append(posX).append(",").append(posY).append("^FB").append(fieldBlockWidth)
                        .append(",").append(maxLines).append(",0,C,0\n")
                        .append("^A0N,").append(fontHeight).append(",").append(fontWidth)
                        .append("^FD").append(labelText).append("^FS\n");
            }
            if (i % 2 == 1 || i == actualQuantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
    }

    /**
     * Regista uma mensagem no console e no ficheiro de log do mês corrente.
     * Cria o diretório de logs se ele não existir.
     * @param message A mensagem a ser registada.
     */
    void log(String message) {
        String timestamped = "[" + FORMATTER.format(LocalDateTime.now()) + "] " + message;
        System.out.println(timestamped);

        // --- ESTA É A NOVA LÓGICA ---
        String logFilename = getCurrentLogFilename();
        try {
            // Garante que o diretório 'logs' existe
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            // Escreve no ficheiro do mês corrente, dentro da pasta 'logs'
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

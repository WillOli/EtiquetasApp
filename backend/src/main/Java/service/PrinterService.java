package service;


import config.AppConfig;
import model.PrintRequest;

import javax.print.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class PrinterService {
    // ... (mantenha todas as suas constantes aqui, como LOG_FILE, FORMATTER, etc.)
    private static final String LOG_FILE = AppConfig.getLogFilename();
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

    // --- NOVA CLASSE DE EXCEÇÃO --
    public static class PrinterServiceException extends RuntimeException {
        public PrinterServiceException(String message) {
            super(message);
        }
    }

    public void printLabels(String labelText, int quantity, PrintRequest.LabelType type) {
        if (labelText == null || labelText.trim().isEmpty()) {
            log("[ERRO] Texto da etiqueta está vazio.");
            // Não lançamos exceção aqui porque o controller já deve ter validado.
            return;
        }

        // ... (mantenha as validações de MAX_CHARACTERS e MAX_QUANTITY) ...

        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

        // --- VALIDAÇÃO DE IMPRESSORA MELHORADA ---
        if (defaultService == null) {
            log("[ERRO GRAVE] Nenhuma impressora padrão encontrada no sistema.");
            // Lista as impressoras disponíveis para ajudar na depuração
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            log("Impressoras disponíveis: " + Arrays.toString(services));
            throw new PrinterServiceException("Nenhuma impressora padrão foi encontrada no servidor.");
        }

        String zpl = generateZpl(labelText, quantity * 2, type); // Multiplica a quantidade aqui

        try {
            InputStream is = new ByteArrayInputStream(zpl.getBytes("UTF-8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();

            String logMessage = String.format(
                    "[IMPRESSÃO] %s\nTipo de Etiqueta: %s\nImpressora: %s\nQuantidade solicitada: %d\nQuantidade real: %d\nZPL:\n%s",
                    FORMATTER.format(LocalDateTime.now()), type.toString(), defaultService.getName(),
                    quantity, quantity * 2, formatZplLogging(zpl)); // <-- Formatando legenda dos logs
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

    // Método unificado para gerar ZPL
    private String generateZpl(String labelText, int actualQuantity, PrintRequest.LabelType type) {
        if (type == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return generate62mmLabelZPL(labelText, actualQuantity);
        }
        return generateStandardLabelZPL(labelText, actualQuantity);
    }

    // ... (cole aqui seus métodos generateStandardLabelZPL, generate62mmLabelZPL e log sem alteração) ...
    public String generateStandardLabelZPL(String labelText, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM;
        int pageWidthDots = labelWidthDots * 2 + GAP_HORIZONTAL_DOTS * 2;
        int fontHeight = FONT_HEIGHT_STANDARD;
        int charsPerLine = (int) Math.floor((double) labelWidthDots / fontHeight);

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < actualQuantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n");
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
                zplBuilder.append("^PW").append(pageWidthDots).append("\n");
                zplBuilder.append("^LL").append(labelHeightDots).append("\n");
            }

            for (int col = 0; col < 2; col++) {
                int initialYOffset = 10;
                int marginLeftBlock = 10;
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

    void log(String message) {
        String timestamped = "[" + FORMATTER.format(LocalDateTime.now()) + "] " + message;
        System.out.println(timestamped);

        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(timestamped + "\n\n");
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao escrever no log: " + e.getMessage());
        }
    }

    /**
     * Formata uma string ZPL com múltiplos linhas para uma única linha compacta,
     * ideal para ser exibida em logs.
     * @param rawZpl A string ZPL original
     * @return Uma versão formatada e mais legível do ZPL.
     */
    private String formatZplLogging(String rawZpl) {
        if (rawZpl == null || rawZpl.trim().isEmpty()) {
            return "[ZPL Vazio]";
        }
        // Substitui todas as quebras de linha por um espaço e remove espaços múltiplos.
        return rawZpl.trim().replace("\n", " ").replaceAll("\\s+", " ");
    }
}
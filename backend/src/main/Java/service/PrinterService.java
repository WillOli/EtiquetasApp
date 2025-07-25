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
    private static final int LABEL_WIDTH_MM_SIXTY_TWO_MM = 80; // Ajustado para refletir o tamanho real da etiqueta
    private static final int LABEL_HEIGHT_MM_SIXTY_TWO_MM = 25; // Ajustado para refletir o tamanho real da etiqueta
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

        // A lógica de quantidade (duplicar ou não) agora é controlada pelo tipo de etiqueta.
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
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate mfg = LocalDate.parse(request.getMfgDate());
            LocalDate exp = LocalDate.parse(request.getExpDate());

            // Usamos '\\&' para quebra de linha explícita no ZPL
            String labelText = String.format(
                    "%s\\&Fab: %s\\&Val: %s",
                    request.getProductName(),
                    mfg.format(dtf),
                    exp.format(dtf)
            );

            PrintRequest.LabelType labelType = PrintRequest.LabelType.valueOf(request.getLabelType().toUpperCase());
            printLabels(labelText, request.getQuantity(), labelType);

        } catch (IllegalArgumentException e) {
            log("[ERRO] Tipo de etiqueta inválido recebido: " + request.getLabelType());
            throw new PrinterServiceException("Tipo de etiqueta inválido: " + request.getLabelType());
        } catch (Exception e) {
            log("[ERRO] Falha ao processar etiqueta de validade: " + e.getMessage());
            throw new PrinterServiceException("Falha ao processar etiqueta de validade: " + e.getMessage());
        }
    }

    private String generateZpl(String labelText, int actualQuantity, PrintRequest.LabelType type) {
        if (type == PrintRequest.LabelType.SIXTY_TWO_MM) {
            return generate80x25LabelZPL(labelText, actualQuantity);
        }
        return generate62mmLabelZPL(labelText, actualQuantity);
    }

    public String generate62mmLabelZPL(String labelText, int actualQuantity) {
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

    // ===== MÉTODO MODIFICADO =====
    public String generate80x25LabelZPL(String labelText, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int labelHeightDots = LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM;
        int fontHeight = FONT_HEIGHT_SIXTY_TWO_MM;
        int fontWidth = FONT_WIDTH_SIXTY_TWO_MM;

        // O bloco de texto (^FB) ocupará a largura total da etiqueta para permitir a centralização.
        int fieldBlockWidth = labelWidthDots;
        // Calcula o número máximo de linhas que cabem na etiqueta.
        int maxLines = (int) Math.floor((double) labelHeightDots / fontHeight);

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < actualQuantity; i++) {
            zplBuilder.append("^XA\n"); // Início de cada etiqueta individual
            zplBuilder.append("^CI28\n"); // Suporte a UTF-8
            zplBuilder.append("^PW").append(labelWidthDots).append("\n"); // Largura da página/etiqueta
            zplBuilder.append("^LL").append(labelHeightDots).append("\n"); // Altura da etiqueta

            // Posição Y (vertical). Ajuste este valor se precisar mover o texto para cima ou para baixo.
            int verticalPosition = 40;

            // Montagem dos comandos ZPL para centralização
            zplBuilder.append("^FO0,").append(verticalPosition) // Posição do campo (X=0, Y=verticalPosition)
                    .append("^A0N,").append(fontHeight).append(",").append(fontWidth) // Define a fonte
                    .append("^FB").append(fieldBlockWidth).append(",").append(maxLines).append(",0,C,0") // Define o Bloco de Texto: largura total, max linhas, sem espaço extra, Centralizado (C)
                    .append("^FD").append(labelText).append("^FS\n"); // O texto a ser impresso

            zplBuilder.append("^XZ\n"); // Fim de cada etiqueta individual
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

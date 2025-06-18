package service;

import javax.print.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrinterService {

    private static final String LOG_FILE = "logs.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_CHARACTERS = 40;
    private static final int MAX_QUANTITY = 100;

    public void printLabels(String labelText, int quantity) {
        if (labelText == null || labelText.trim().isEmpty()) {
            log("[ERRO] Texto da etiqueta está vazio.");
            return;
        }

        if (labelText.length() > MAX_CHARACTERS) {
            log("[ERRO] Texto excede o limite de " + MAX_CHARACTERS + " caracteres.");
            return;
        }
        if (quantity <= 0) {
            log("[ERRO] Quantidade inválida. Deve ser maior que zero.");
            return;
        }

        if (quantity > MAX_QUANTITY) {
            log("[ERRO] Quantidade acima do limite permitido (" + MAX_QUANTITY + ").");
            return;
        }

        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

        if (defaultService == null) {
            log("[ERRO] Nenhuma impressora padrão encontrada.");
            return;
        }

        int dotsPerMM = 8; // 203 dpi
        int labelWidthDots = 40 * dotsPerMM;  // 320 dots (40mm)
        int labelHeightDots = 25 * dotsPerMM; // 200 dots (25mm)
        int pageWidthDots = labelWidthDots * 2 + 20; // 660 dots (320 * 2 + 20 dots de lacuna horizontal)
        int fontHeight = 24;  // Ajustado para caber em 200 dots, centralizado
        int charsPerLine = 12; // Estimativa de caracteres que cabem em 320 dots
        int textWidthEstimate = Math.min(labelText.length(), charsPerLine) * 25; // 25 dots por caractere como média

        // Centralização: Margens ajustadas para o meio de 320x200 dots (sem margens externas)
        int marginLeft = (labelWidthDots - textWidthEstimate) / 2;
        if (marginLeft < 0) marginLeft = 0; // Sem margem mínima, pois margens são 0
        int marginTop = (labelHeightDots - fontHeight) / 2; // Centraliza verticalmente

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < quantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n");
                zplBuilder.append("^PW").append(pageWidthDots).append("\n");
                zplBuilder.append("^LL").append(labelHeightDots).append("\n");
            }

            // Adiciona texto em ambas as colunas para cada etiqueta
            for (int col = 0; col < 2; col++) {
                int posX = marginLeft + col * (labelWidthDots + 10); // Adiciona 10 dots de lacuna/2 por lado
                int posY = marginTop;

                // Ajuste para texto longo: quebre ou reduza se necessário
                String displayText = labelText;
                if (labelText.length() > charsPerLine) {
                    displayText = labelText.substring(0, charsPerLine) + "...";
                    textWidthEstimate = charsPerLine * 25;
                    marginLeft = (labelWidthDots - textWidthEstimate) / 2;
                    if (marginLeft < 0) marginLeft = 0;
                }

                zplBuilder.append("^FO").append(posX).append(",").append(posY)
                        .append("^A0N,").append(fontHeight).append(",").append(fontHeight)
                        .append("^FD").append(displayText).append("^FS\n");
            }

            if (i % 2 == 1 || i == quantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }

        String zpl = zplBuilder.toString();

        try {
            InputStream is = new ByteArrayInputStream(zpl.getBytes("UTF8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();

            String logMessage = String.format(
                    "[IMPRESSÃO] %s\nImpressora: %s\nQuantidade: %d\nZPL:\n%s",
                    FORMATTER.format(LocalDateTime.now()),
                    defaultService.getName(),
                    quantity,
                    zpl
            );
            log(logMessage);

            job.print(doc, null);

            log("[STATUS] Impressão enviada com sucesso.");

        } catch (PrintException e) {
            log("[ERRO] Erro específico da impressora: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log("[ERRO] Codificação não suportada: " + e.getMessage());
        } catch (Exception e) {
            log("[ERRO] Falha inesperada na impressão: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void log(String message) {
        String timestamped = "[" + FORMATTER.format(LocalDateTime.now()) + "] " + message;
        System.out.println(timestamped);

        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(timestamped + "\n\n");
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao escrever no log: " + e.getMessage());
        }
    }
}
import javax.print.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrinterService {

    private static final String LOG_FILE = "logs.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void printLabels(String labelText, int quantity) {
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

        if (defaultService == null) {
            log("[ERRO] Nenhuma impressora padrão encontrada."); // já existente
            return;
        }

        int totalWidth = 40;
        int topMarginLines = 2;
        int leftMarginSpace = 4;
        int lineSpacing = 2;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < quantity; i++) {
            sb.append("\n".repeat(topMarginLines));
            String centered = centerText(labelText, totalWidth);
            sb.append(" ".repeat(leftMarginSpace)).append(centered).append("\n");
            sb.append("\n".repeat(lineSpacing));
        }

        String fullText = sb.toString();

        try {
            InputStream is = new ByteArrayInputStream(fullText.getBytes("UTF8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();

            String logMessage = String.format(
                    "[IMPRESSÃO] %s\nImpressora: %s\nQuantidade: %d\nTexto:\n%s",
                    FORMATTER.format(LocalDateTime.now()),
                    defaultService.getName(),
                    quantity,
                    fullText
            );
            log(logMessage);

            job.print(doc, null);

            log("[STATUS] Impressão enviada com sucesso."); // já existente

        } catch (PrintException e) {
            log("[ERRO] Erro específico da impressora: " + e.getMessage()); // << NOVO
        } catch (UnsupportedEncodingException e) {
            log("[ERRO] Codificação não suportada: " + e.getMessage()); // << NOVO
        } catch (Exception e) {
            log("[ERRO] Falha inesperada na impressão: " + e.getMessage()); // << NOVO
            e.printStackTrace();
        }
    }

    private String centerText(String text, int totalWidth) {
        int padding = Math.max((totalWidth - text.length()) / 2, 0);
        return " ".repeat(padding) + text;
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

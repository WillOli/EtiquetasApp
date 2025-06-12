import javax.print.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class PrinterService {

    public void printLabels(String labelText, int quantity) {
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

        if (defaultService == null) {
            System.out.println("Nenhuma impressora padrão encontrada.");
            return;
        }

        // Gera o conteúdo a ser impresso (pode adaptar a formatação aqui)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < quantity; i++) {
            sb.append(centerText(labelText)).append("\n\n"); // Duplo espaçamento entre etiquetas
        }

        String fullText = sb.toString();
        try {
            // Converte para InputStream
            InputStream is = new ByteArrayInputStream(fullText.getBytes("UTF8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);

            DocPrintJob job = defaultService.createPrintJob();
            job.print(doc, null);

            System.out.println("Impressão enviada para " + defaultService.getName());

        } catch (Exception e) {
            System.err.println("Erro ao imprimir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Centraliza o texto em até 40 caracteres (ajuste conforme necessário)
    private String centerText(String text) {
        int totalWidth = 40;
        int padding = Math.max((totalWidth - text.length()) / 2, 0);
        return " ".repeat(padding) + text;
    }
}


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

        // Configurações manuais
        int totalWidth = 40; // Largura da etiqueta (em caracteres)
        int topMarginLines = 2; // Linhas vazias no topo da etiqueta
        int leftMarginSpace = 4; // Espaços à esquerda
        int lineSpacing = 2; // Linhas entre cada etiqueta

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < quantity; i++) {
            sb.append("\n".repeat(topMarginLines)); // Margem superior
            String centered = centerText(labelText, totalWidth);
            sb.append(" ".repeat(leftMarginSpace)).append(centered).append("\n");
            sb.append("\n".repeat(lineSpacing)); // Espaço entre etiquetas
        }

        String fullText = sb.toString();

        try {
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

    private String centerText(String text, int totalWidth) {
        int padding = Math.max((totalWidth - text.length()) / 2, 0);
        return " ".repeat(padding) + text;
    }
}

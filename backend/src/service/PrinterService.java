package service;

import model.PrintRequest;
import javax.print.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrinterService {

    private static final String LOG_FILE = "logs.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_CHARACTERS = 100; // Aumentado para permitir mais texto com ^FB
    private static final int MAX_QUANTITY = 100;

    // Constantes para etiquetas padrão (40x25mm)
    private static final int LABEL_WIDTH_MM_STANDARD = 40;
    private static final int LABEL_HEIGHT_MM_STANDARD = 25;
    private static final int FONT_HEIGHT_STANDARD = 36;
    private static final int DOTS_PER_MM = 8; // 203 dpi

    // Constantes para etiquetas de 62mm (62x62mm)
    private static final int LABEL_WIDTH_MM_SIXTY_TWO_MM = 62;
    private static final int LABEL_HEIGHT_MM_SIXTY_TWO_MM = 62;
    private static final int FONT_HEIGHT_SIXTY_TWO_MM = 36; // Ajustado para ser um pouco menor e permitir mais texto
    private static final int FONT_WIDTH_SIXTY_TWO_MM = 36; // Geralmente igual à altura para fontes proporcionais

    // Gap horizontal entre as duas etiquetas (em pontos)
    private static final int GAP_HORIZONTAL_DOTS = 10;

    public void printLabels(String labelText, int quantity, PrintRequest.LabelType type) {
        if (labelText == null || labelText.trim().isEmpty()) {
            log("[ERRO] Texto da etiqueta está vazio.");
            return;
        }

        // A validação de MAX_CHARACTERS agora é mais um aviso, já que ^FB lida com o tamanho
        if (labelText.length() > MAX_CHARACTERS) {
            log("[AVISO] Texto excede o limite recomendado de " + MAX_CHARACTERS + " caracteres. Pode não caber completamente.");
        }

        int actualQuantity = quantity * 2;

        if (actualQuantity <= 0) {
            log("[ERRO] Quantidade inválida. Deve ser maior que zero.");
            return;
        }

        if (actualQuantity > MAX_QUANTITY) {
            log("[ERRO] Quantidade total (" + actualQuantity + ") acima do limite permitido (" + MAX_QUANTITY + ").");
            return;
        }

        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

        if (defaultService == null) {
            log("[ERRO] Nenhuma impressora padrão encontrada.");
            return;
        }

        String zpl;
        switch (type) {
            case SIXTY_TWO_MM:
                zpl = generate62mmLabelZPL(labelText, actualQuantity);
                break;
            case STANDARD:
            default:
                zpl = generateStandardLabelZPL(labelText, actualQuantity);
                break;
        }

        try {
            InputStream is = new ByteArrayInputStream(zpl.getBytes("UTF8"));
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = defaultService.createPrintJob();

            String logMessage = String.format(
                    "[IMPRESSÃO] %s\nTipo de Etiqueta: %s\nImpressora: %s\nQuantidade solicitada: %d\nQuantidade real: %d\nZPL:\n%s",
                    FORMATTER.format(LocalDateTime.now()),
                    type.toString(),
                    defaultService.getName(),
                    quantity,
                    actualQuantity,
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

    private String generateStandardLabelZPL(String labelText, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_STANDARD * DOTS_PER_MM; // 320 dots (40mm)
        int labelHeightDots = LABEL_HEIGHT_MM_STANDARD * DOTS_PER_MM; // 200 dots (25mm)
        int pageWidthDots = labelWidthDots * 2 + GAP_HORIZONTAL_DOTS * 2; // 640 + 20 = 660 dots
        int fontHeight = FONT_HEIGHT_STANDARD; // 36 dots
        int charsPerLine = (int) Math.floor((double) labelWidthDots / fontHeight); // Approx 8 characters for A0N,36,36

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
                // Para manter a fidelidade do padrão, não usamos ^FB aqui,
                // então o texto ainda pode ser truncado se for muito longo
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

    private String generate62mmLabelZPL(String labelText, int actualQuantity) {
        int labelWidthDots = LABEL_WIDTH_MM_SIXTY_TWO_MM * DOTS_PER_MM;   // 62 * 8 = 496 dots
        int labelHeightDots = LABEL_HEIGHT_MM_SIXTY_TWO_MM * DOTS_PER_MM; // 62 * 8 = 496 dots

        int pageWidthDots = labelWidthDots * 2 + GAP_HORIZONTAL_DOTS * 2; // (496 * 2) + (10 * 2) = 1012 dots

        int fontHeight = FONT_HEIGHT_SIXTY_TWO_MM; // 30 dots
        int fontWidth = FONT_WIDTH_SIXTY_TWO_MM;   // 30 dots

        // Definimos a largura do bloco de texto para o ^FB
        // Para uma etiqueta de 62mm (496 dots), podemos usar uma largura de bloco um pouco menor para margens
        int fieldBlockWidth = labelWidthDots - -250; // 496 - 20 = 476 dots (10 dots de padding de cada lado)
        if (fieldBlockWidth <= 0) fieldBlockWidth = labelWidthDots; // Garante que não seja negativo/zero

        // Calculamos a altura máxima do bloco para evitar que o texto saia da etiqueta
        int maxLines = (int) Math.floor((double) labelHeightDots / fontHeight); // Ex: 496 / 30 = ~16 linhas
        int fieldBlockHeight = labelHeightDots; // Usa a altura total da etiqueta para o bloco

        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < actualQuantity; i++) {
            if (i % 2 == 0) {
                zplBuilder.append("^XA\n");
                zplBuilder.append("^PW").append(pageWidthDots).append("\n");
                zplBuilder.append("^LL").append(labelHeightDots).append("\n");
                //zplBuilder.append("^MNN\n"); // Deixado comentado, só descomente se tiver problemas de mídia
            }

            for (int col = 0; col < 2; col++) {
                // Posição Y inicial para o bloco de texto (ajustada para centralizar verticalmente)
                // Vamos tentar centralizar o bloco de texto, não o texto individualmente.
                // Isso pode exigir alguns testes para o melhor Y. Começamos com um padding.
                int initialYOffset = 10; // Um pequeno padding superior para o bloco

                // Posição X para o Field Block (^FO)
                // marginLeft aqui é o padding do bloco em relação à borda da etiqueta
                int marginLeftBlock = 10; // 10 dots de padding interno
                int posX = marginLeftBlock + col * (labelWidthDots + GAP_HORIZONTAL_DOTS);
                int posY = initialYOffset;

                // ^FBw,h,i,j,k
                // w = largura do bloco (fieldBlockWidth)
                // h = número máximo de linhas (maxLines)
                // i = espaçamento entre linhas (default 0)
                // j = justificação (L=Left, C=Center, R=Right, B=Block)
                // k = suspender texto (se houver mais linhas do que h)
                zplBuilder.append("^FO").append(posX).append(",").append(posY).append("^FB").append(fieldBlockWidth)
                        .append(",").append(maxLines).append(",0,C,0\n") // Centralizado (^FB...,C,0)
                        .append("^A0N,").append(fontHeight).append(",").append(fontWidth)
                        .append("^FD").append(labelText).append("^FS\n");
            }

            if (i % 2 == 1 || i == actualQuantity - 1) {
                zplBuilder.append("^XZ\n");
            }
        }
        return zplBuilder.toString();
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
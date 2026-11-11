package service.strategies;
import static service.ZplSanitizer.sanitize;

public class SimpleLayoutStrategy implements ILabelStrategy {
    private final String text;
    private final int quantity;
    private final String setor;
    private final String dataFabricacao;
    private final String dataValidade;
    private final long numeroImpressao;
    private final int sizedWidthMm;
    private final int sizedHeightMm;

    public SimpleLayoutStrategy(String text, int quantity, String setor, String dataFabricacao, String dataValidade, long numeroImpressao) {
        this.text = text;
        this.quantity = quantity;
        this.setor = (setor != null) ? setor : "";
        this.dataFabricacao = service.LabelDateFormatter.format(dataFabricacao);
        this.dataValidade = service.LabelDateFormatter.format(dataValidade);
        this.numeroImpressao = numeroImpressao;
        this.sizedWidthMm = 0;
        this.sizedHeightMm = 0;
    }

    public SimpleLayoutStrategy(String text, int quantity, String setor, String dataFabricacao,
                                String dataValidade, long numeroImpressao, int widthMm, int heightMm) {
        this.text = text;
        this.quantity = quantity;
        this.setor = setor != null ? setor : "";
        this.dataFabricacao = service.LabelDateFormatter.format(dataFabricacao);
        this.dataValidade = service.LabelDateFormatter.format(dataValidade);
        this.numeroImpressao = numeroImpressao;
        this.sizedWidthMm = widthMm;
        this.sizedHeightMm = heightMm;
    }

    @Override
    public String generateZpl() {
        if (sizedWidthMm > 0 && sizedHeightMm > 0) return generateSizedZpl();
        int labelWidthDots = service.ZplConstants.LABEL_WIDTH_MM_SIXTY_TWO_MM * service.ZplConstants.DOTS_PER_MM;
        int labelHeightDots = service.ZplConstants.LABEL_HEIGHT_MM_SIXTY_TWO_MM * service.ZplConstants.DOTS_PER_MM;
        int horizontalInset = 24;
        int verticalInset = 8;
        int contentWidth = labelWidthDots - horizontalInset * 2;
        int contentHeight = labelHeightDots - verticalInset * 2;
        int rowHeight = contentHeight / 5;
        String[] fields = {
                sanitize(this.text.toUpperCase(java.util.Locale.ROOT)),
                "SETOR: " + sanitize(this.setor.toUpperCase(java.util.Locale.ROOT)),
                "FAB.: " + sanitize(this.dataFabricacao),
                "VAL.: " + sanitize(this.dataValidade),
                "REG.: "
        };
        StringBuilder zplBuilder = new StringBuilder();

        for (int i = 0; i < this.quantity; i++) {
            zplBuilder.append("^XA\n");
            zplBuilder.append("^CI28\n");
            zplBuilder.append("^PW").append(labelWidthDots).append("\n");
            zplBuilder.append("^LL").append(labelHeightDots).append("\n");
            zplBuilder.append("^LH0,0^LS0^LT0\n");

            fields[4] = "REG.: " + String.format("%05d", this.numeroImpressao + i);
            for (int row = 0; row < fields.length; row++) {
                int maximumFont = row == 0 ? 30 : 22;
                int font = fitFont(fields[row], maximumFont, contentWidth);
                int y = verticalInset + row * rowHeight;
                zplBuilder.append(String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,C,0^FD%s^FS\n",
                        horizontalInset, y, font, font, contentWidth, fields[row]));
            }
            zplBuilder.append("^XZ\n");
        }
        return zplBuilder.toString();
    }

    private String generateSizedZpl() {
        int pageWidth = sizedWidthMm * service.ZplConstants.DOTS_PER_MM;
        int pageHeight = sizedHeightMm * service.ZplConstants.DOTS_PER_MM;
        int contentX = Math.max(8, pageWidth / 20);
        int contentWidth = pageWidth - contentX * 2;
        int verticalInset = Math.max(8, pageHeight / 20);
        int rowHeight = (pageHeight - verticalInset * 2) / 5;
        String[] fields = {
                sanitize(text.toUpperCase(java.util.Locale.ROOT)),
                "SETOR: " + sanitize(setor.toUpperCase(java.util.Locale.ROOT)),
                "FAB.: " + sanitize(dataFabricacao),
                "VAL.: " + sanitize(dataValidade),
                "REG.: "
        };
        StringBuilder zpl = new StringBuilder();
        for (int i = 0; i < quantity; i++) {
            zpl.append("^XA\n^CI28\n^PW").append(pageWidth).append("\n^LL").append(pageHeight).append('\n');
            fields[4] = "REG.: " + String.format("%05d", numeroImpressao + i);
            for (int row = 0; row < fields.length; row++) {
                int maxFont = Math.min(28, Math.max(12, rowHeight - 8));
                int font = fitFont(fields[row], maxFont, contentWidth);
                int y = verticalInset + row * rowHeight;
                zpl.append(String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,C,0^FD%s^FS\n",
                        contentX, y, font, font, contentWidth, fields[row]));
            }
            zpl.append("^XZ\n");
        }
        return zpl.toString();
    }

    private int fitFont(String value, int maximum, int width) {
        if (value == null || value.isBlank()) return maximum;
        int estimated = (int) (width * 0.88 / (value.length() * 0.62));
        return Math.max(10, Math.min(maximum, estimated));
    }
}

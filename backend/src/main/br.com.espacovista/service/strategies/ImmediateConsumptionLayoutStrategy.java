package service.strategies;
import static service.ZplSanitizer.sanitize;
import model.ImmediateConsumptionRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ImmediateConsumptionLayoutStrategy implements ILabelStrategy {
    private final ImmediateConsumptionRequest request;
    private final int sizedWidthMm;
    private final int sizedHeightMm;

    public ImmediateConsumptionLayoutStrategy(ImmediateConsumptionRequest request) {
        this.request = request;
        this.sizedWidthMm = 0;
        this.sizedHeightMm = 0;
    }

    public ImmediateConsumptionLayoutStrategy(ImmediateConsumptionRequest request, int widthMm, int heightMm) {
        this.request = request;
        this.sizedWidthMm = widthMm;
        this.sizedHeightMm = heightMm;
    }

    @Override
    public String generateZpl() {
        if (sizedWidthMm > 0 && sizedHeightMm > 0) return generateSizedZpl();
        String productName = sanitize(request.getProductName());

        String dataFabricacao = request.getDataFabricacao() != null && !request.getDataFabricacao().isEmpty()
                ? service.LabelDateFormatter.format(request.getDataFabricacao())
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String validade = service.LabelDateFormatter.format(request.getValidade());

        int fontSizeTitle = 30;
        int fontSizeText = 25;
        int labelWidth = 80 * service.ZplConstants.DOTS_PER_MM;
        int labelHeight = 25 * service.ZplConstants.DOTS_PER_MM;
        int horizontalInset = 24;
        int contentWidth = labelWidth - horizontalInset * 2;

        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n^CI28\n^PW").append(labelWidth).append("\n^LL").append(labelHeight)
                .append("\n^LH0,0^LS0^LT0\n");
        zpl.append(createLine(horizontalInset, 15, fontSizeTitle, contentWidth, "CONSUMO IMEDIATO"));
        String productLine = "Produto: " + productName;
        String fabricationLine = "Data de Fabricação: " + dataFabricacao;
        String validityLine = "Validade: " + validade;
        zpl.append(createLine(horizontalInset, 55, fitFont(productLine, fontSizeText, contentWidth), contentWidth, productLine));
        zpl.append(createLine(horizontalInset, 95, fitFont(fabricationLine, fontSizeText, contentWidth), contentWidth, fabricationLine));
        zpl.append(createLine(horizontalInset, 135, fitFont(validityLine, fontSizeText, contentWidth), contentWidth, validityLine));
        zpl.append("^XZ\n");

        return zpl.toString().repeat(request.getQuantity());
    }

    private String generateSizedZpl() {
        String productName = sanitize(request.getProductName());
        String dataFabricacao = request.getDataFabricacao() != null && !request.getDataFabricacao().isEmpty()
                ? service.LabelDateFormatter.format(request.getDataFabricacao())
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String validade = service.LabelDateFormatter.format(request.getValidade());
        int pageWidth = sizedWidthMm * service.ZplConstants.DOTS_PER_MM;
        int pageHeight = sizedHeightMm * service.ZplConstants.DOTS_PER_MM;
        int x = Math.max(8, pageWidth / 20);
        int width = pageWidth - 2 * x;
        int inset = Math.max(8, pageHeight / 20);
        int rowHeight = (pageHeight - 2 * inset) / 4;
        String[] lines = {
                "CONSUMO IMEDIATO",
                "Produto: " + productName,
                (sizedWidthMm <= 30 ? "Fab.: " : "Fabricação: ") + dataFabricacao,
                "Validade: " + validade
        };
        StringBuilder zpl = new StringBuilder();
        for (int copy = 0; copy < request.getQuantity(); copy++) {
            zpl.append("^XA\n^CI28\n^PW").append(pageWidth).append("\n^LL").append(pageHeight).append('\n');
            for (int row = 0; row < lines.length; row++) {
                int maxFont = Math.min(26, Math.max(12, rowHeight - 8));
                int font = fitFont(lines[row], maxFont, width);
                int y = inset + row * rowHeight;
                zpl.append(String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,C,0^FD%s^FS\n",
                        x, y, font, font, width, sanitize(lines[row])));
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

    private String createLine(int x, int y, int fontSize, int width, String text) {
        return String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,C,0^FD%s^FS\n",
                x, y, fontSize, fontSize, width, sanitize(text));
    }
}

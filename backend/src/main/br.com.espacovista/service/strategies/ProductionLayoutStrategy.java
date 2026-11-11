package service.strategies;
import static service.ZplSanitizer.sanitize;

import model.ProductionRequest;

public class ProductionLayoutStrategy implements ILabelStrategy {

    private final ProductionRequest request;

    public ProductionLayoutStrategy(ProductionRequest request) {
        this.request = request;
    }

    @Override
    public String generateZpl() {
        if (request.getLabelType() != null && request.getLabelType().isNewStockType()) {
            return generateForNewStock();
        }
        StringBuilder zpl = new StringBuilder();

        // Converte "2026-08-31" para "31/08/2026" diretamente via String
        String dataPrep = formatData(request.getDataPreparacao());
        String dataVal = formatData(request.getDataValidade());

        // Formata os horários se vierem no formato HH:mm (ex: "16:44" -> "16h 44min")
        String horaPrep = formatHora(request.getHorarioPreparo());
        String horaDesc = formatHora(request.getHorarioDescarte());

        zpl.append("^XA\n^CI28\n");
        zpl.append("^PW640\n");
        zpl.append("^LL240\n");

        int leftInset = 20;
        int pageContentWidth = 600;
        String[] lines = {
                "Produto: " + sanitize(request.getProductName()),
                "Data Prep: " + sanitize(dataPrep),
                "Validade: " + sanitize(dataVal),
                "Hora Prep: " + sanitize(horaPrep),
                "Descarte: " + sanitize(horaDesc)
        };

        appendCenteredField(zpl, leftInset, 10, pageContentWidth, 24, "ETIQUETA DE PRODUCAO");
        for (int row = 0; row < lines.length; row++) {
            appendCenteredField(zpl, leftInset, 48 + row * 38, pageContentWidth, 22, lines[row]);
        }

        zpl.append("^XZ\n");

        return zpl.toString().repeat(request.getQuantity());
    }

    private String generateForNewStock() {
        var type = request.getLabelType();
        int labelWidth = type.getWidthMm() * service.ZplConstants.DOTS_PER_MM;
        int labelHeight = type.getHeightMm() * service.ZplConstants.DOTS_PER_MM;
        int columns = type.isDoubleLabel() ? 2 : 1;
        int gap = type.isDoubleLabel() ? service.ZplConstants.GAP_HORIZONTAL_DOTS : 0;
        int pageWidth = columns * labelWidth + (type.isDoubleLabel() ? gap * 2 : 0);
        String prepDate = formatData(request.getDataPreparacao());
        String valDate = formatData(request.getDataValidade());
        String prepTime = formatHora(request.getHorarioPreparo());
        String discardTime = formatHora(request.getHorarioDescarte());
        String prepLabel = type.getWidthMm() <= 30 ? "Prep.: " : "Data Prep: ";
        String valLabel = type.getWidthMm() <= 30 ? "Val.: " : "Validade: ";
        String prepTimeLabel = type.getWidthMm() <= 30 ? "H. Prep.: " : "Hora Prep: ";
        String discardLabel = type.getWidthMm() <= 30 ? "Desc.: " : "Descarte: ";
        String[] lines = {
                "ETIQUETA DE PRODUÇÃO",
                "Produto: " + request.getProductName(),
                prepLabel + prepDate,
                valLabel + valDate,
                prepTimeLabel + prepTime,
                discardLabel + discardTime
        };
        StringBuilder zpl = new StringBuilder();
        for (int copy = 0; copy < request.getQuantity(); copy++) {
            zpl.append("^XA\n^CI28\n^PW").append(pageWidth).append("\n^LL").append(labelHeight).append('\n');
            if (type == model.PrintRequest.LabelType.DOUBLE_25X25) zpl.append("^LH0,0^LS0^LT0\n");
            for (int column = 0; column < columns; column++) {
                int horizontalInset = type == model.PrintRequest.LabelType.DOUBLE_25X25
                        ? 24 : Math.max(6, labelWidth / 20);
                int x = column * (labelWidth + gap) + horizontalInset;
                int width = labelWidth - 2 * horizontalInset;
                int inset = type == model.PrintRequest.LabelType.DOUBLE_25X25
                        ? 18 : Math.max(8, labelHeight / 20);
                int rowHeight = (labelHeight - 2 * inset) / lines.length;
                for (int row = 0; row < lines.length; row++) {
                    int maximum = Math.min(24, Math.max(12, rowHeight - 6));
                    int font = fitFont(lines[row], maximum, width);
                    zpl.append(String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,C,0^FD%s^FS\n",
                            x, inset + row * rowHeight, font, font, width, sanitize(lines[row])));
                }
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

    private void appendCenteredField(StringBuilder zpl, int x, int y, int width, int maximumFont, String value) {
        int font = fitFont(value, maximumFont, width);
        zpl.append(String.format("^FO%d,%d^A0N,%d,%d^FB%d,1,0,C,0^FD%s^FS\n",
                x, y, font, font, width, value));
    }

    private String formatData(String dateStr) {
        if (dateStr == null || !dateStr.contains("-")) return dateStr != null ? dateStr : "";
        String[] p = dateStr.split("-");
        if (p.length == 3) {
            return p[2] + "/" + p[1] + "/" + p[0]; // YYYY-MM-DD virou DD/MM/YYYY
        }
        return dateStr;
    }

    private String formatHora(String horaStr) {
        if (horaStr == null || horaStr.isEmpty()) return "";
        // Se vier como "16:44:00" ou "16:44", transforma em "16h 44min"
        String[] p = horaStr.split(":");
        if (p.length >= 2) {
            return p[0] + "h " + p[1] + "min";
        }
        return horaStr;
    }
}

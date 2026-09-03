package service.strategies;

import model.ProductionRequest;

public class ProductionLayoutStrategy implements ILabelStrategy {

    private final ProductionRequest request;

    public ProductionLayoutStrategy(ProductionRequest request) {
        this.request = request;
    }

    @Override
    public String generateZpl() {
        StringBuilder zpl = new StringBuilder();

        // Converte "2026-08-31" para "31/08/2026" diretamente via String
        String dataPrep = formatData(request.getDataPreparacao());
        String dataVal = formatData(request.getDataValidade());

        // Formata os horários se vierem no formato HH:mm (ex: "16:44" -> "16h 44min")
        String horaPrep = formatHora(request.getHorarioPreparo());
        String horaDesc = formatHora(request.getHorarioDescarte());

        zpl.append("^XA\n");
        zpl.append("^PW640\n");
        zpl.append("^LL240\n");

        int startX = 20;
        int col2X = 330;

        zpl.append(String.format("^FO%d,15^A0N,24,24^FDETIQUETA DE PRODUCAO^FS\n", startX));
        zpl.append(String.format("^FO%d,60^A0N,24,24^FDProduto: %s^FS\n", startX, request.getProductName() != null ? request.getProductName() : ""));
        zpl.append(String.format("^FO%d,110^A0N,22,22^FDData Prep: %s^FS\n", startX, dataPrep));
        zpl.append(String.format("^FO%d,110^A0N,22,22^FDValidade: %s^FS\n", col2X, dataVal));
        zpl.append(String.format("^FO%d,155^A0N,22,22^FDHora Prep: %s^FS\n", startX, horaPrep));
        zpl.append(String.format("^FO%d,155^A0N,22,22^FDDescarte: %s^FS\n", col2X, horaDesc));

        zpl.append("^XZ\n");

        return zpl.toString();
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
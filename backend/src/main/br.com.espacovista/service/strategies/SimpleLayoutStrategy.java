package service.strategies;

public class SimpleLayoutStrategy implements ILabelStrategy {
    private final String text;
    private final String sector; // [cite: 19]
    private final int quantity;

    public SimpleLayoutStrategy(String text, String sector, int quantity) {
        this.text = text;
        this.sector = sector; // [cite: 20]
        this.quantity = quantity;
    }

    @Override
    public String generateZpl() {
        StringBuilder zpl = new StringBuilder();
        // [cite: 24, 25]
        String infoSetor = (this.sector != null && !this.sector.isEmpty()) ? this.sector.toUpperCase() : "GERAL";

        for (int i = 0; i < quantity; i++) {
            zpl.append("^XA\n^CI28\n^PW480\n^LL240\n");
            zpl.append("^FO345,65^GFA,1800,1800,15,000000..."); // Sua logo [cite: 22]
            zpl.append("^FO30,85^A0N,70,70^FB300,1,0,L,0^FD").append(this.text.toUpperCase()).append("^FS\n"); // Nome [cite: 23]
            zpl.append("^FO30,155^A0N,30,30^FB300,1,0,L,0^FDVISTA - ").append(infoSetor).append("^FS\n"); // Setor [cite: 25]
            zpl.append("^XZ\n");
        }
        return zpl.toString();
    }
}
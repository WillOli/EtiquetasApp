package service.strategies;


import model.ProductionRequest;
import java.time.format.DateTimeFormatter;

public class ProductionLayoutStrategy implements ILabelStrategy {

    private final ProductionRequest request;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProductionLayoutStrategy(ProductionRequest request) {
        this.request = request;
    }

    /**
     * Retorna a estratégia correta para uma requisição de etiqueta de produção.
     */
    public static ILabelStrategy getStrategy(ProductionRequest request) {
        // Como o foco atual é a etiqueta 80x30, roteamos para o layout que acabamos de criar.
        // Se no futuro houver necessidade de uma versão dupla, basta adicionar o else com a StandardStrategy.
        return new ProductionLayoutStrategy(request);
    }

    @Override
    public String generateZpl() {
        StringBuilder zpl = new StringBuilder();

        String dataPrep = request.getDataPreparacao() != null ? request.getDataPreparacao().format(DATE_FORMATTER) : "";
        String horaPrep = request.getHorarioPreparo() != null ?
                String.format("%02dh %02dmin", request.getHorarioPreparo().getHour(), request.getHorarioPreparo().getMinute()) : "";
        String horaDesc = request.getHorarioDescarte() != null ?
                String.format("%02dh %02dmin", request.getHorarioDescarte().getHour(), request.getHorarioDescarte().getMinute()) : "";
        String dataVal = request.getDataValidade() != null ? request.getDataValidade().format(DATE_FORMATTER) : "";

        zpl.append("^XA\n");

        // Configuração para Etiqueta 80x30mm (aprox. 640 dots de largura por 240 dots de altura)
        zpl.append("^PW640\n");
        zpl.append("^LL240\n");

        int startX = 20; // Margem esquerda padrão
        int col2X = 330; // Margem para a segunda coluna (Validade e Descarte)

        // Título Limpo (Sem tarja preta)
        zpl.append(String.format("^FO%d,15^A0N,24,24^FDETIQUETA DE PRODUCAO^FS\n", startX));

        // Nome do Produto (Linha inteira)
        zpl.append(String.format("^FO%d,60^A0N,24,24^FDProduto: %s^FS\n", startX, request.getProductName() != null ? request.getProductName() : ""));

        // Linha de Datas (Lado a lado para aproveitar os 80mm de largura)
        zpl.append(String.format("^FO%d,110^A0N,22,22^FDData Prep: %s^FS\n", startX, dataPrep));
        zpl.append(String.format("^FO%d,110^A0N,22,22^FDValidade: %s^FS\n", col2X, dataVal));

        // Linha de Horários (Lado a lado)
        zpl.append(String.format("^FO%d,155^A0N,22,22^FDHora Prep: %s^FS\n", startX, horaPrep));
        zpl.append(String.format("^FO%d,155^A0N,22,22^FDDescarte: %s^FS\n", col2X, horaDesc));

        zpl.append("^XZ\n");

        return zpl.toString();
    }
}
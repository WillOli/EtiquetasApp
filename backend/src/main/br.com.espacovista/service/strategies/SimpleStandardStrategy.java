package service.strategies;
import static service.ZplSanitizer.sanitize;

import static service.ZplConstants.*;

/**
 * Estratégia para imprimir a Etiqueta Dupla (Padrão / Standard).
 * Adaptada para herdar de AbstractTwoColumnStrategy e gerenciar o incremento
 * automático e sequencial do número de registro para cada coluna/etiqueta impressa.
 */
public class SimpleStandardStrategy extends AbstractTwoColumnStrategy {

    private final String text;
    private final String setor;
    private final String dataFabricacao;
    private final String dataValidade;
    private final int labelWidthMm;

    // ✅ Alterado para um contador mutável, permitindo avançar a cada coluna impressa
    private long currentRegistro;
    private final long initialRegistro;

    // Construtor principal recebendo o número base (long) para cálculo sequencial
    public SimpleStandardStrategy(String text, String setor, String dataFabricacao, String dataValidade, long registroInicial, int quantity) {
        this(text, setor, dataFabricacao, dataValidade, registroInicial, quantity, 40);
    }

    public SimpleStandardStrategy(String text, String setor, String dataFabricacao, String dataValidade,
                                  long registroInicial, int quantity, int labelWidthMm) {
        super(quantity, labelWidthMm, 25);
        this.text = (text != null) ? text : "";
        this.setor = (setor != null) ? setor : "";
        this.dataFabricacao = service.LabelDateFormatter.format(dataFabricacao);
        this.dataValidade = service.LabelDateFormatter.format(dataValidade);
        this.labelWidthMm = labelWidthMm;
        this.initialRegistro = registroInicial;
        this.currentRegistro = registroInicial;
    }

    /**
     * Gera o conteúdo ZPL adotando as mesmas dimensões e coordenadas da Etiqueta Simples.
     * A cada chamada (coluna 0 ou coluna 1), o registro é formatado e incrementado.
     */
    @Override
    protected void beforeGeneration() { currentRegistro = initialRegistro; }

    @Override
    protected String generateLabelContent(int startX, int column) {
        // Etiqueta dupla de 25 × 25 mm: reserva 3 mm em cada lateral.
        int offsetX = startX + (this.labelWidthMm == 25 ? 24 : 8);
        int labelWidthDots = this.labelWidthMm * service.ZplConstants.DOTS_PER_MM;
        int contentWidth = labelWidthDots - (this.labelWidthMm == 25 ? 48 : 16);
        boolean compact = contentWidth < 240;
        int fontHeightNome = compact
                ? fitFont(this.text.toUpperCase(java.util.Locale.ROOT), 24, contentWidth)
                : this.text.length() > 14
                    ? Math.max(12, (int) (contentWidth * 0.78 / (this.text.length() * 0.62)))
                    : 34;
        int yTitle = compact ? 18 : 20;
        int ySector = compact ? 54 : 60;
        int yMfg = compact ? 90 : 96;
        int yValidity = compact ? 126 : 128;
        int yRegistration = compact ? 162 : 160;
        int smallFont = compact ? 14 : 22;

        StringBuilder contentBuilder = new StringBuilder();

        // 1. NOME DO PRODUTO
        contentBuilder.append("^FO").append(offsetX).append(",").append(yTitle)
                .append("^A0N,").append(fontHeightNome).append(",").append(fontHeightNome)
                .append("^FB").append(contentWidth).append(",1,0,C,0^FD").append(sanitize(this.text.toUpperCase(java.util.Locale.ROOT))).append("^FS\n");

        // 2. SETOR
        if (!this.setor.isEmpty()) {
            String setorText = "SETOR: " + this.setor.toUpperCase(java.util.Locale.ROOT);
            int sectorFont = compact ? fitFont(setorText, smallFont, contentWidth) : 20;
            contentBuilder.append("^FO").append(offsetX).append(",").append(ySector)
                    .append("^A0N,").append(sectorFont).append(',').append(sectorFont).append("^FB").append(contentWidth).append(",1,0,C,0^FDSETOR: ").append(sanitize(this.setor.toUpperCase(java.util.Locale.ROOT))).append("^FS\n");
        }

        // 3. DATA DE FABRICAÇÃO
        if (!this.dataFabricacao.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(',').append(yMfg)
                    .append("^A0N,").append(compact ? smallFont : 22).append(',').append(compact ? smallFont : 22).append("^FB").append(contentWidth).append(",1,0,C,0^FDFAB.: ").append(sanitize(this.dataFabricacao)).append("^FS\n");
        }

        // 4. DATA DE VALIDADE
        if (!this.dataValidade.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(',').append(yValidity)
                    .append("^A0N,").append(compact ? smallFont : 22).append(',').append(compact ? smallFont : 22).append("^FB").append(contentWidth).append(",1,0,C,0^FDVAL.: ").append(sanitize(this.dataValidade)).append("^FS\n");
        }

        // 5. REGISTRO SEQUENCIAL INCREMENTAL
        // Formata o número atual com 5 dígitos (ex: 00001, 00002)
        String regFormatado = String.format("%05d", this.currentRegistro);
        int registrationFont = compact ? 14 : 21;
        contentBuilder.append("^FO").append(offsetX).append(',').append(yRegistration)
                .append("^A0N,").append(registrationFont).append(',').append(registrationFont).append("^FB").append(contentWidth).append(",1,0,C,0^FDREG.: ").append(regFormatado).append("^FS\n");

        // ✅ O SEGREDO: Incrementa o contador! A próxima coluna ou etiqueta receberá o número seguinte.
        this.currentRegistro++;

        return contentBuilder.toString();
    }

    private int fitFont(String value, int maximum, int width) {
        if (value == null || value.isBlank()) return maximum;
        int estimated = (int) (width * 0.88 / (value.length() * 0.62));
        return Math.max(10, Math.min(maximum, estimated));
    }

}

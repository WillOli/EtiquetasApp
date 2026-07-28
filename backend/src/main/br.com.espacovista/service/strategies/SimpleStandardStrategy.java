package service.strategies;

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

    // ✅ Alterado para um contador mutável, permitindo avançar a cada coluna impressa
    private long currentRegistro;

    // Construtor principal recebendo o número base (long) para cálculo sequencial
    public SimpleStandardStrategy(String text, String setor, String dataFabricacao, String dataValidade, long registroInicial, int quantity) {
        super(quantity);
        this.text = (text != null) ? text : "";
        this.setor = (setor != null) ? setor : "";
        this.dataFabricacao = (dataFabricacao != null) ? dataFabricacao : "";
        this.dataValidade = (dataValidade != null) ? dataValidade : "";
        this.currentRegistro = registroInicial;
    }

    // Construtor de compatibilidade para quem ainda passar String (tenta converter para long)
    public SimpleStandardStrategy(String text, String setor, String dataFabricacao, String dataValidade, String registro, int quantity) {
        super(quantity);
        this.text = (text != null) ? text : "";
        this.setor = (setor != null) ? setor : "";
        this.dataFabricacao = (dataFabricacao != null) ? dataFabricacao : "";
        this.dataValidade = (dataValidade != null) ? dataValidade : "";

        long reg = 1L;
        if (registro != null && !registro.trim().isEmpty()) {
            try {
                reg = Long.parseLong(registro.trim());
            } catch (NumberFormatException e) {
                reg = 1L;
            }
        }
        this.currentRegistro = reg;
    }

    // Construtores de compatibilidade adicionais
    public SimpleStandardStrategy(String text, String setor, String dataFabricacao, String dataValidade, int quantity) {
        this(text, setor, dataFabricacao, dataValidade, 1L, quantity);
    }

    public SimpleStandardStrategy(String text, int quantity) {
        this(text, "CONFEITARIA", "", "", 1L, quantity);
    }

    /**
     * Gera o conteúdo ZPL adotando as mesmas dimensões e coordenadas da Etiqueta Simples.
     * A cada chamada (coluna 0 ou coluna 1), o registro é formatado e incrementado.
     */
    @Override
    protected String generateLabelContent(int startX, int column) {
        // Ajuste das colunas idêntico ao seu layout original
        int offsetX = (column == 0) ? 5 : 340;
        int fontHeightNome = (this.text.length() > 16) ? 24 : 28;

        StringBuilder contentBuilder = new StringBuilder();

        // 1. NOME DO PRODUTO
        contentBuilder.append("^FO").append(offsetX).append(",25")
                .append("^A0N,").append(fontHeightNome).append(",").append(fontHeightNome)
                .append("^FB340,1,0,C,0^FD").append(this.text.toUpperCase()).append("^FS\n");

        // 2. SETOR
        if (!this.setor.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(",65")
                    .append("^A0N,22,22^FB340,1,0,C,0^FDSETOR: ").append(this.setor.toUpperCase()).append("^FS\n");
        }

        // 3. DATA DE FABRICAÇÃO
        if (!this.dataFabricacao.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(",100")
                    .append("^A0N,20,20^FB340,1,0,C,0^FDFAB.: ").append(this.dataFabricacao).append("^FS\n");
        }

        // 4. DATA DE VALIDADE
        if (!this.dataValidade.isEmpty()) {
            contentBuilder.append("^FO").append(offsetX).append(",130")
                    .append("^A0N,20,20^FB340,1,0,C,0^FDVAL.: ").append(this.dataValidade).append("^FS\n");
        }

        // 5. REGISTRO SEQUENCIAL INCREMENTAL
        // Formata o número atual com 5 dígitos (ex: 00001, 00002)
        String regFormatado = String.format("%05d", this.currentRegistro);
        contentBuilder.append("^FO").append(offsetX).append(",160")
                .append("^A0N,20,20^FB340,1,0,C,0^FDREG.: ").append(regFormatado).append("^FS\n");

        // ✅ O SEGREDO: Incrementa o contador! A próxima coluna ou etiqueta receberá o número seguinte.
        this.currentRegistro++;

        return contentBuilder.toString();
    }
}
package model;

public class PrintRequest {

    private String text;
    private int quantity;
    private LabelType labelType;

    private String setor;
    private String dataFabricacao;
    private String dataValidade;
    private String registro;
    private LabelStyle style = new LabelStyle();

    public enum LabelType {
        STANDARD(40, 25, true),
        SIXTY_TWO_MM(80, 25, false),
        SINGLE_25X50(50, 25, false),
        DOUBLE_25X25(25, 25, true),
        SINGLE_30X50(50, 30, false);

        private final int widthMm;
        private final int heightMm;
        private final boolean doubleLabel;

        LabelType(int widthMm, int heightMm, boolean doubleLabel) {
            this.widthMm = widthMm;
            this.heightMm = heightMm;
            this.doubleLabel = doubleLabel;
        }

        public int getWidthMm() { return widthMm; }
        public int getHeightMm() { return heightMm; }
        public boolean isDoubleLabel() { return doubleLabel; }
        public boolean isNewStockType() {
            return this == SINGLE_25X50 || this == DOUBLE_25X25 || this == SINGLE_30X50;
        }
    }

    public PrintRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
    }

    public void setLabelType(String labelTypeStr) {

        if (labelTypeStr == null ||
                labelTypeStr.trim().isEmpty()) {

            this.labelType = null;
            return;
        }

        try {
            this.labelType =
                    LabelType.valueOf(
                            labelTypeStr
                                    .trim()
                                    .toUpperCase(java.util.Locale.ROOT)
                    );

        } catch (IllegalArgumentException e) {
            this.labelType = null;
        }
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getDataFabricacao() {
        return dataFabricacao;
    }

    public void setDataFabricacao(String dataFabricacao) {
        this.dataFabricacao = dataFabricacao;
    }

    public String getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(String dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }

    public LabelStyle getStyle() { return style; }
    public void setStyle(LabelStyle style) { this.style = style; }
}

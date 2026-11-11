package validation;

import model.ImmediateConsumptionRequest;
import model.LabelStyle;
import model.PrintRequest;
import model.ProductionRequest;
import model.ValidadePrintRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class PrintRequestValidator {

    public static final int MAX_PRODUCT_LENGTH = 80;
    public static final int MAX_SECTOR_LENGTH = 40;
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 50;

    private PrintRequestValidator() {
    }

    // =========================================================
    // ETIQUETA SIMPLES
    // =========================================================

    public static void validate(PrintRequest request) {

        if (request == null) {
            throw new RequestValidationException(
                    "Requisição de impressão não pode ser vazia."
            );
        }

        request.setText(
                requireText(
                        request.getText(),
                        "Nome do produto"
                )
        );

        request.setSetor(
                requireText(
                        request.getSetor(),
                        "Setor"
                )
        );

        /*
         * Remove espaços desnecessários das datas,
         * sem alterar o formato recebido.
         */
        request.setDataFabricacao(
                trimNullable(
                        request.getDataFabricacao()
                )
        );

        request.setDataValidade(
                trimNullable(
                        request.getDataValidade()
                )
        );

        validateQuantity(
                request.getQuantity()
        );

        validateLabelType(
                request.getLabelType()
        );
        validateStyle(request.getStyle());

        LocalDate fabricacao =
                requireDate(
                        request.getDataFabricacao(),
                        "Data de fabricação"
                );

        LocalDate validade =
                requireDate(
                        request.getDataValidade(),
                        "Data de validade"
                );

        if (validade.isBefore(fabricacao)) {
            throw new RequestValidationException(
                    "A data de validade não pode ser anterior à data de fabricação."
            );
        }
    }

    // =========================================================
    // ETIQUETA DE VALIDADE
    // =========================================================

    public static void validate(
            ValidadePrintRequest request
    ) {

        if (request == null) {
            throw new RequestValidationException(
                    "Requisição de etiqueta de validade não pode ser vazia."
            );
        }

        request.setProductName(
                requireText(
                        request.getProductName(),
                        "Nome do produto"
                )
        );

        request.setDataFabricacao(
                trimNullable(
                        request.getDataFabricacao()
                )
        );

        request.setDataAbertura(
                trimNullable(
                        request.getDataAbertura()
                )
        );

        request.setDataValidade(
                trimNullable(
                        request.getDataValidade()
                )
        );

        validateQuantity(
                request.getQuantity()
        );

        validateLabelType(
                request.getLabelType()
        );
        validateStyle(request.getStyle());

        LocalDate fabricacao =
                requireDate(
                        request.getDataFabricacao(),
                        "Data de fabricação"
                );

        LocalDate abertura =
                requireDate(
                        request.getDataAbertura(),
                        "Data de abertura"
                );

        LocalDate validade =
                requireDate(
                        request.getDataValidade(),
                        "Data de validade"
                );

        if (abertura.isBefore(fabricacao)) {
            throw new RequestValidationException(
                    "A data de abertura não pode ser anterior à data de fabricação."
            );
        }

        if (validade.isBefore(abertura)) {
            throw new RequestValidationException(
                    "A data de validade não pode ser anterior à data de abertura."
            );
        }
    }

    // =========================================================
    // CONSUMO IMEDIATO
    // =========================================================

    public static void validate(
            ImmediateConsumptionRequest request
    ) {

        if (request == null) {
            throw new RequestValidationException(
                    "Requisição de consumo imediato não pode ser vazia."
            );
        }

        request.setProductName(
                requireText(
                        request.getProductName(),
                        "Nome do produto"
                )
        );

        request.setDataFabricacao(
                trimNullable(
                        request.getDataFabricacao()
                )
        );

        request.setValidade(
                trimNullable(
                        request.getValidade()
                )
        );

        validateQuantity(
                request.getQuantity()
        );

        validateLabelType(
                request.getLabelType()
        );
        validateStyle(request.getStyle());

        LocalDate fabricacao =
                requireDate(
                        request.getDataFabricacao(),
                        "Data de fabricação"
                );

        LocalDate validade =
                requireDate(
                        request.getValidade(),
                        "Data de validade"
                );

        if (validade.isBefore(fabricacao)) {
            throw new RequestValidationException(
                    "A data de validade não pode ser anterior à data de fabricação."
            );
        }
    }

    // =========================================================
    // PRODUÇÃO
    // =========================================================

    public static void validate(
            ProductionRequest request
    ) {

        if (request == null) {
            throw new RequestValidationException(
                    "Requisição de produção não pode ser vazia."
            );
        }

        request.setProductName(
                requireText(
                        request.getProductName(),
                        "Nome do produto"
                )
        );

        request.setDataPreparacao(
                trimNullable(
                        request.getDataPreparacao()
                )
        );

        request.setHorarioPreparo(
                trimNullable(
                        request.getHorarioPreparo()
                )
        );

        request.setHorarioDescarte(
                trimNullable(
                        request.getHorarioDescarte()
                )
        );

        request.setDataValidade(
                trimNullable(
                        request.getDataValidade()
                )
        );

        validateQuantity(
                request.getQuantity()
        );

        validateLabelType(
                request.getLabelType()
        );
        validateStyle(request.getStyle());

        /*
         * Regra de negócio:
         * Produção aceita somente layout simples.
         */
        if (request.getLabelType() != PrintRequest.LabelType.SIXTY_TWO_MM
                && !request.getLabelType().isNewStockType()) {

            throw new RequestValidationException(
                    "Tipo de etiqueta não suportado para produção."
            );
        }

        LocalDate preparacao = requireDate(
                request.getDataPreparacao(),
                "Data de preparação"
        );

        requireTime(
                request.getHorarioPreparo(),
                "Horário de preparo"
        );

        requireTime(
                request.getHorarioDescarte(),
                "Horário de descarte"
        );

        LocalDate validade = requireDate(
                request.getDataValidade(),
                "Data de validade"
        );
        if (validade.isBefore(preparacao)) {
            throw new RequestValidationException(
                    "A data de validade não pode ser anterior à data de preparação.");
        }
    }

    // =========================================================
    // VALIDAÇÕES COMUNS
    // =========================================================

    private static String requireText(
            String value,
            String fieldName
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            throw new RequestValidationException(
                    fieldName + " é obrigatório."
            );
        }

        int maximum = fieldName.equals("Setor") ? MAX_SECTOR_LENGTH : MAX_PRODUCT_LENGTH;
        if (value.trim().codePointCount(0, value.trim().length()) > maximum) {
            throw new RequestValidationException(fieldName + " deve ter no máximo " + maximum + " caracteres.");
        }
        String normalized = service.ZplSanitizer.sanitize(value);
        if (normalized.isBlank()) throw new RequestValidationException(fieldName + " deve conter texto válido.");
        return normalized;
    }

    private static String trimNullable(
            String value
    ) {

        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private static void validateQuantity(
            int quantity
    ) {

        if (quantity < MIN_QUANTITY) {
            throw new RequestValidationException(
                    "A quantidade deve ser maior ou igual a "
                            + MIN_QUANTITY
                            + "."
            );
        }

        if (quantity > MAX_QUANTITY) {
            throw new RequestValidationException(
                    "A quantidade máxima permitida é "
                            + MAX_QUANTITY
                            + "."
            );
        }
    }

    private static void validateLabelType(
            PrintRequest.LabelType labelType
    ) {

        if (labelType == null) {
            throw new RequestValidationException(
                    "Tipo de etiqueta é obrigatório ou inválido."
            );
        }
    }

    private static void validateStyle(LabelStyle style) {
        if (style == null) return; // Compatível com clientes antigos sem opções de estilo.
        if (style.getFontScale() < 80 || style.getFontScale() > 125) {
            throw new RequestValidationException("O tamanho do texto deve ficar entre 80% e 125%.");
        }
        if (style.getAlignment() == null || !java.util.Set.of("left", "center", "right").contains(style.getAlignment())) {
            throw new RequestValidationException("Alinhamento de etiqueta inválido.");
        }
        if (!Double.isFinite(style.getOffsetXmm()) || Math.abs(style.getOffsetXmm()) > 10
                || !Double.isFinite(style.getOffsetYmm()) || Math.abs(style.getOffsetYmm()) > 10) {
            throw new RequestValidationException("O ajuste de posição deve ficar entre -10 mm e 10 mm.");
        }
        validateDoubleLabelOffset(style.getFirstLabelOffsetXmm(), -6, 6);
        validateDoubleLabelOffset(style.getSecondLabelOffsetXmm(), -6, 6);
        validateDoubleLabelOffset(style.getFirstLabelOffsetYmm(), -3, 3);
        validateDoubleLabelOffset(style.getSecondLabelOffsetYmm(), -3, 3);
        validateDoubleLabelAlignment(style.getFirstLabelAlignment());
        validateDoubleLabelAlignment(style.getSecondLabelAlignment());
        if (style.getSpacing() == null || !java.util.Set.of("compact", "normal", "relaxed").contains(style.getSpacing())) {
            throw new RequestValidationException("Espaçamento de etiqueta inválido.");
        }
        if (style.getFontFamily() == null || !java.util.Set.of("0", "A", "D").contains(style.getFontFamily())) {
            throw new RequestValidationException("Fonte de etiqueta inválida.");
        }
        if (style.getBorder() == null || !java.util.Set.of("none", "solid").contains(style.getBorder())) {
            throw new RequestValidationException("Borda de etiqueta inválida.");
        }
    }

    private static void validateDoubleLabelOffset(Double value, double min, double max) {
        if (value != null && (!Double.isFinite(value) || value < min || value > max)) {
            throw new RequestValidationException("O ajuste individual da etiqueta dupla está fora do limite permitido.");
        }
    }

    private static void validateDoubleLabelAlignment(String alignment) {
        if (alignment != null && !java.util.Set.of("left", "center", "right").contains(alignment)) {
            throw new RequestValidationException("Alinhamento individual da etiqueta dupla inválido.");
        }
    }

    // =========================================================
    // DATAS
    // =========================================================

    private static LocalDate requireDate(
            String value,
            String fieldName
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            throw new RequestValidationException(
                    fieldName + " é obrigatória."
            );
        }

        String normalizedValue = value.trim();
        if (!normalizedValue.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            throw new RequestValidationException(fieldName + " deve usar DD/MM/AAAA ou AAAA-MM-DD.");
        }

        /*
         * Formato utilizado atualmente pelo frontend
         * da Etiqueta Simples e Consumo Imediato:
         *
         * DD/MM/AAAA
         */
        DateTimeFormatter brazilianFormat =
                DateTimeFormatter
                        .ofPattern("dd/MM/uuuu")
                        .withResolverStyle(
                                ResolverStyle.STRICT
                        );

        try {

            return LocalDate.parse(
                    normalizedValue,
                    brazilianFormat
            );

        } catch (DateTimeParseException ignored) {

            /*
             * Se não for DD/MM/AAAA,
             * tentamos o padrão ISO abaixo.
             */
        }

        /*
         * Formato ISO utilizado por outros pontos
         * da aplicação e pelos testes:
         *
         * AAAA-MM-DD
         */
        try {

            return LocalDate.parse(
                    normalizedValue,
                    DateTimeFormatter.ISO_LOCAL_DATE
            );

        } catch (DateTimeParseException e) {

            throw new RequestValidationException(
                    fieldName
                            + " possui formato inválido. "
                            + "Use DD/MM/AAAA ou AAAA-MM-DD."
            );
        }
    }

    // =========================================================
    // HORÁRIOS
    // =========================================================

    private static LocalTime requireTime(
            String value,
            String fieldName
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            throw new RequestValidationException(
                    fieldName + " é obrigatório."
            );
        }

        if (!value.trim().matches("[0-9]{2}:[0-9]{2}(:[0-9]{2})?")) {
            throw new RequestValidationException(fieldName + " deve usar HH:mm ou HH:mm:ss.");
        }

        try {

            /*
             * LocalTime.parse aceita, por exemplo:
             *
             * 16:30
             * 16:30:00
             */
            return LocalTime.parse(
                    value.trim()
            );

        } catch (DateTimeParseException e) {

            throw new RequestValidationException(
                    fieldName
                            + " possui formato inválido. "
                            + "Use HH:mm."
            );
        }
    }
}

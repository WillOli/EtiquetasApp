package service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Contrato visual das etiquetas: datas ISO recebidas pela API são exibidas em DD/MM/AAAA. */
public final class LabelDateFormatter {
    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private LabelDateFormatter() {}
    public static String format(String value) {
        if (value == null) return "";
        try { return LocalDate.parse(value.trim()).format(OUTPUT); }
        catch (DateTimeParseException e) { return value.trim(); }
    }
}

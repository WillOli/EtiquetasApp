package service;

import java.text.Normalizer;

/** Texto de uma linha. Comandos, controles e escapes de ^FB nunca vêm do usuário. */
public final class ZplSanitizer {
    private ZplSanitizer() {}

    public static String sanitize(String value) {
        if (value == null) return "";
        StringBuilder result = new StringBuilder();
        Normalizer.normalize(value, Normalizer.Form.NFC).codePoints().forEach(code -> {
            if (code == '^' || code == '~' || code == '\\' || Character.isISOControl(code)
                    || Character.getType(code) == Character.FORMAT || Character.isWhitespace(code)) {
                result.append(' ');
            } else {
                result.appendCodePoint(code);
            }
        });
        return result.toString().replaceAll(" +", " ").trim();
    }
}

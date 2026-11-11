package service;

import model.LabelStyle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Applies the label laboratory settings to each physical label in a ZPL page. */
final class ZplStyleApplicator {
    private static final Pattern PAGE_WIDTH = Pattern.compile("\\^PW(\\d+)");
    private static final Pattern PAGE_LENGTH = Pattern.compile("\\^LL(\\d+)");
    private static final Pattern FONT = Pattern.compile("\\^A0([NRI]),(\\d+),(\\d+)");
    private static final Pattern FONT_HEIGHT = Pattern.compile("\\^A[A-Z0-9][NRI],(\\d+),");
    private static final Pattern FIELD = Pattern.compile(
            "\\^FO(-?\\d+),(-?\\d+)(\\^A[A-Z0-9][NRI],[^\\^]+)(?:\\^FB(\\d+),(\\d+),(\\d+),[LCR],(-?\\d+))?(\\^FD)");

    private ZplStyleApplicator() { }

    static String apply(String zpl, LabelStyle style) {
        if (zpl == null || zpl.isEmpty() || style == null) return zpl;
        String[] pages = zpl.split("(?=\\^XA)");
        StringBuilder result = new StringBuilder(zpl.length() + 128);
        for (String page : pages) result.append(applyToPage(page, style));
        return result.toString();
    }

    private static String applyToPage(String page, LabelStyle style) {
        int pageWidth = dimension(PAGE_WIDTH, page);
        int pageLength = dimension(PAGE_LENGTH, page);
        boolean simpleContinuous80x25 = pageWidth == 640 && pageLength == 200;
        boolean productionContinuous80x30 = pageWidth == 640 && pageLength == 240;
        boolean doubleLabels25x25 = pageWidth == 420 && pageLength == 200;
        boolean doubleLabels40x25 = pageWidth == 660 && pageLength == 200;
        boolean doubleLabelPage = doubleLabels25x25 || doubleLabels40x25;
        boolean validityDoubleLabels25x25 = doubleLabels25x25 && page.contains("^FXVALIDITY_DOUBLE^FS");
        double spacing = switch (style.getSpacing()) {
            case "compact" -> 0.8;
            case "relaxed" -> 1.05;
            default -> 1.0;
        };
        int offsetX = (int) Math.round(style.getOffsetXmm() * ZplConstants.DOTS_PER_MM);
        int offsetY = (int) Math.round(style.getOffsetYmm() * ZplConstants.DOTS_PER_MM);

        Matcher fonts = FONT.matcher(page);
        StringBuffer scaled = new StringBuffer();
        while (fonts.find()) {
            int height = Math.max(8, (int) Math.round(Integer.parseInt(fonts.group(2)) * style.getFontScale() / 100.0));
            int width = Math.max(8, (int) Math.round(Integer.parseInt(fonts.group(3)) * style.getFontScale() / 100.0));
            fonts.appendReplacement(scaled, Matcher.quoteReplacement("^A" + style.getFontFamily()
                    + fonts.group(1) + "," + height + "," + width));
        }
        fonts.appendTail(scaled);

        Matcher fields = FIELD.matcher(scaled.toString());
        StringBuffer positioned = new StringBuffer();
        while (fields.find()) {
            int originalX = Integer.parseInt(fields.group(1));
            int originalY = Integer.parseInt(fields.group(2));
            int columnStep = doubleLabels25x25 ? 210 : 330;
            int doubleColumnStart = doubleLabelPage && originalX >= columnStep ? columnStep : 0;
            boolean secondLabel = doubleLabelPage && doubleColumnStart > 0;
            double selectedOffsetX = doubleLabels25x25
                    ? (secondLabel ? valueOr(style.getSecondLabelOffsetXmm(), style.getOffsetXmm())
                    : valueOr(style.getFirstLabelOffsetXmm(), style.getOffsetXmm()))
                    : style.getOffsetXmm();
            double selectedOffsetY = doubleLabels25x25
                    ? (secondLabel ? valueOr(style.getSecondLabelOffsetYmm(), style.getOffsetYmm())
                    : valueOr(style.getFirstLabelOffsetYmm(), style.getOffsetYmm()))
                    : style.getOffsetYmm();
            String selectedAlignment = validityDoubleLabels25x25
                    ? (secondLabel ? valueOr(style.getSecondLabelAlignment(), "right")
                    : valueOr(style.getFirstLabelAlignment(), "left"))
                    : style.getAlignment();
            int fieldOffsetX = (int) Math.round(selectedOffsetX * ZplConstants.DOTS_PER_MM);
            int fieldOffsetY = (int) Math.round(selectedOffsetY * ZplConstants.DOTS_PER_MM);
            int x;
            int width;
            if (doubleLabels25x25) {
                // A área útil é de 152 dots dentro de cada coluna de 200 dots.
                // Limita o deslocamento para que a caixa ^FB nunca ultrapasse a etiqueta.
                fieldOffsetX = Math.max(-24, Math.min(24, fieldOffsetX));
                x = doubleColumnStart + 24 + fieldOffsetX;
                width = 152;
            } else if (doubleLabels40x25) {
                // Cada coluna mede 320 dots; o vão central tem 10 dots.
                // Ajusta a caixa dentro da etiqueta e reduz a largura disponível ao mover para a direita.
                int localX = Math.max(0, Math.min(312,
                        8 + (int) Math.round(selectedOffsetX * ZplConstants.DOTS_PER_MM)));
                x = doubleColumnStart + localX;
                width = 312 - localX;
            } else if (pageWidth == 400) {
                // Etiqueta simples horizontal: o bloco usa a largura física.
                // O ajuste horizontal do laboratório é aplicado em ^LS, fora do ^FO.
                x = 0;
                width = pageWidth;
            } else if (simpleContinuous80x25) {
                x = 24 + offsetX;
                width = Math.max(1, 592 - offsetX);
            } else {
                x = originalX;
                if ("left".equals(style.getAlignment()) && !productionContinuous80x30) {
                    x += pageWidth == 824 ? 80 : 24;
                }
                x += offsetX;
                if (pageWidth == 824 && "right".equals(selectedAlignment)) {
                    width = Math.max(1, pageWidth - x);
                } else if (fields.group(4) == null) {
                    width = 0;
                } else {
                    width = Integer.parseInt(fields.group(4));
                }
            }
            int y = (int) Math.round(originalY * spacing) + (doubleLabels25x25 ? fieldOffsetY : offsetY);
            if (doubleLabelPage) {
                Matcher fontSize = FONT_HEIGHT.matcher(fields.group(3));
                int fontHeight = fontSize.find() ? Integer.parseInt(fontSize.group(1)) : 14;
                int lineCount = fields.group(5) == null ? 1 : Integer.parseInt(fields.group(5));
                int maxY = Math.max(0, pageLength - fontHeight * lineCount);
                y = Math.max(0, Math.min(maxY, y));
            }
            if (!simpleContinuous80x25 && !doubleLabelPage && fields.group(4) == null && !"left".equals(selectedAlignment)) {
                width = availableWidth(pageWidth, x, y, selectedAlignment);
            }
            String block = width > 0
                    ? "^FB" + width + "," + (fields.group(5) == null ? "1" : fields.group(5))
                    + "," + (fields.group(6) == null ? "0" : fields.group(6)) + ","
                    + alignmentCode(selectedAlignment) + ","
                    + (fields.group(7) == null ? "0" : fields.group(7))
                    : "";
            String replacement = "^FO" + x + "," + y + fields.group(3) + block + fields.group(8);
            fields.appendReplacement(positioned, Matcher.quoteReplacement(replacement));
        }
        fields.appendTail(positioned);

        String result = positioned.toString();
        if (pageWidth == 400) result = applyHorizontalLabelShift(result, offsetX);
        if ("solid".equals(style.getBorder()) && pageWidth > 0 && pageLength > 0) {
            String borderCommands = pageWidth == 660
                    ? "^FO0,0^GB320," + pageLength + ",1^FS^FO330,0^GB320," + pageLength + ",1^FS"
                    : pageWidth == 420
                    ? "^FO0,0^GB200," + pageLength + ",1^FS^FO210,0^GB200," + pageLength + ",1^FS"
                    : "^FO0,0^GB" + pageWidth + "," + pageLength + ",1^FS";
            int end = result.lastIndexOf("^XZ");
            if (end >= 0) result = result.substring(0, end) + borderCommands + result.substring(end);
        }
        return result;
    }

    private static String applyHorizontalLabelShift(String page, int offsetDots) {
        if (offsetDots == 0) return page;
        String shift = "^LS" + offsetDots + "^LT0";
        if (page.contains("^LS")) {
            return page.replaceFirst("\\^LS-?\\d+(?:\\^LT-?\\d+)?", shift);
        }
        if (page.contains("^LL")) {
            return page.replaceFirst("(\\^LL\\d+)", "$1\n" + shift);
        }
        return page.replaceFirst("(\\^XA)", "$1\n" + shift);
    }

    private static double valueOr(Double value, double fallback) {
        return value == null ? fallback : value;
    }

    private static String valueOr(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static int dimension(Pattern pattern, String page) {
        Matcher matcher = pattern.matcher(page);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static int availableWidth(int pageWidth, int x, int y, String alignment) {
        int rightInset = "right".equals(alignment) ? 24 : 0;
        if (pageWidth == 660) {
            int localX = x >= 330 ? x - 330 : x;
            return Math.max(0, 320 - localX - rightInset);
        }
        if (pageWidth == 420) {
            int localX = x >= 210 ? x - 210 : x;
            return Math.max(0, 200 - localX - rightInset);
        }
        if (pageWidth == 824) return Math.max(0, pageWidth - x - ("right".equals(alignment) ? 0 : 12));
        if (pageWidth == 640) {
            int margin = "right".equals(alignment) ? 10 : rightInset;
            return Math.max(0, pageWidth - x - margin);
        }
        return Math.max(0, pageWidth - x - 12 - rightInset);
    }

    private static char alignmentCode(String alignment) {
        return switch (alignment) {
            case "right" -> 'R';
            case "center" -> 'C';
            default -> 'L';
        };
    }
}

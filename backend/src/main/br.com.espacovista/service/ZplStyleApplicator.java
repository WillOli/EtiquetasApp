package service;

import model.LabelStyle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Applies the printer-supported laboratory settings to each generated ZPL label. */
final class ZplStyleApplicator {
    private static final Pattern PAGE_WIDTH = Pattern.compile("\\^PW(\\d+)");
    private static final Pattern PAGE_LENGTH = Pattern.compile("\\^LL(\\d+)");
    private static final Pattern FONT = Pattern.compile("\\^A0([NRI]),(\\d+),(\\d+)");
    private static final Pattern FIELD_ORIGIN = Pattern.compile("\\^FO(\\d+),(\\d+)");
    private static final Pattern FIELD_BLOCK = Pattern.compile("(\\^FO(\\d+),(\\d+)\\^A[A-Z0-9][NRI],[^\\^]+\\^FB)(\\d+)(,\\d+,\\d+,)[LCR](,\\d+)");
    private static final Pattern UNALIGNED_FIELD = Pattern.compile("\\^FO(\\d+),(\\d+)(\\^A[A-Z0-9][NRI],[^\\^]+)(\\^FD)");

    private ZplStyleApplicator() { }

    static String apply(String zpl, LabelStyle style) {
        if (zpl == null || zpl.isEmpty() || style == null) return zpl;
        String[] pages = zpl.split("(?=\\^XA)");
        StringBuilder result = new StringBuilder(zpl.length() + 128);
        for (String page : pages) result.append(applyToPage(page, style));
        return result.toString();
    }

    private static String applyToPage(String page, LabelStyle style) {
        int pageWidth = findDimension(PAGE_WIDTH, page, 0);
        int pageLength = findDimension(PAGE_LENGTH, page, 0);
        boolean inferredPageSize = pageWidth == 0 || pageLength == 0;
        if (inferredPageSize && "solid".equals(style.getBorder())) {
            pageWidth = 640;
            pageLength = 200;
            page = page.replaceFirst("(\\^XA\\s*\\^CI\\d+\\s*)", "$1^PW640\n^LL200\n");
        }
        double spacingFactor = switch (style.getSpacing()) {
            case "compact" -> 0.8;
            case "relaxed" -> 1.05;
            default -> 1.0;
        };

        Matcher fontMatcher = FONT.matcher(page);
        StringBuffer fonts = new StringBuffer();
        while (fontMatcher.find()) {
            int height = Math.max(8, (int) Math.round(Integer.parseInt(fontMatcher.group(2)) * style.getFontScale() / 100.0));
            int width = Math.max(8, (int) Math.round(Integer.parseInt(fontMatcher.group(3)) * style.getFontScale() / 100.0));
            fontMatcher.appendReplacement(fonts, Matcher.quoteReplacement("^A" + style.getFontFamily()
                    + fontMatcher.group(1) + "," + height + "," + width));
        }
        fontMatcher.appendTail(fonts);

        Matcher originMatcher = FIELD_ORIGIN.matcher(fonts.toString());
        StringBuffer origins = new StringBuffer();
        while (originMatcher.find()) {
            int x = Integer.parseInt(originMatcher.group(1));
            int y = Math.max(0, (int) Math.round(Integer.parseInt(originMatcher.group(2)) * spacingFactor));
            if ("left".equals(style.getAlignment())) {
                x += pageWidth == 824 ? 80 : 24; // 10 mm na etiqueta contínua larga; 3 mm nos demais formatos.
            }
            originMatcher.appendReplacement(origins,
                    Matcher.quoteReplacement("^FO" + x + "," + y));
        }
        originMatcher.appendTail(origins);

        Matcher fieldBlocks = FIELD_BLOCK.matcher(origins.toString());
        StringBuffer boundedBlocks = new StringBuffer();
        while (fieldBlocks.find()) {
            int x = Integer.parseInt(fieldBlocks.group(2));
            int y = Integer.parseInt(fieldBlocks.group(3));
            int width = Integer.parseInt(fieldBlocks.group(4));
            int safeWidth = pageWidth == 640 && width > 500
                    ? Math.max(0, pageWidth - x - 12 - ("right".equals(style.getAlignment()) ? 24 : 0))
                    : availableWidth(pageWidth, x, y, style.getAlignment());
            if (safeWidth > 0) width = Math.min(width, safeWidth);
            String replacement = fieldBlocks.group(1) + width + fieldBlocks.group(5)
                    + alignmentCode(style.getAlignment()) + fieldBlocks.group(6);
            fieldBlocks.appendReplacement(boundedBlocks, Matcher.quoteReplacement(replacement));
        }
        fieldBlocks.appendTail(boundedBlocks);
        String aligned = boundedBlocks.toString();
        if (!"left".equals(style.getAlignment())) {
            Matcher unaligned = UNALIGNED_FIELD.matcher(aligned);
            StringBuffer blocks = new StringBuffer();
            while (unaligned.find()) {
                int x = Integer.parseInt(unaligned.group(1));
                int y = Integer.parseInt(unaligned.group(2));
                int width = availableWidth(pageWidth, x, y, style.getAlignment());
                if (width <= 0) {
                    unaligned.appendReplacement(blocks, Matcher.quoteReplacement(unaligned.group()));
                } else {
                    String replacement = unaligned.group(1) + "," + unaligned.group(2) + unaligned.group(3)
                            + "^FB" + width + ",1,0," + alignmentCode(style.getAlignment()) + ",0" + unaligned.group(4);
                    unaligned.appendReplacement(blocks, Matcher.quoteReplacement(replacement));
                }
            }
            unaligned.appendTail(blocks);
            aligned = blocks.toString();
        }

        if ("solid".equals(style.getBorder()) && pageWidth > 0 && pageLength > 0) {
            String borderCommands = pageWidth == 660
                    ? "^FO0,0^GB320," + pageLength + ",1^FS^FO330,0^GB320," + pageLength + ",1^FS"
                    : "^FO0,0^GB" + pageWidth + "," + pageLength + ",1^FS";
            int end = aligned.lastIndexOf("^XZ");
            if (end >= 0) aligned = aligned.substring(0, end) + borderCommands + aligned.substring(end);
        }
        return aligned;
    }

    private static int findDimension(Pattern pattern, String page, int fallback) {
        Matcher matcher = pattern.matcher(page);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static int availableWidth(int pageWidth, int x, int y, String alignment) {
        int rightInset = "right".equals(alignment) ? 24 : 0;
        if (pageWidth == 660) {
            int localX = x >= 330 ? x - 330 : x;
            return Math.max(0, 320 - localX - rightInset);
        }
        if (pageWidth == 824) {
            int continuousLabelRightInset = "right".equals(alignment) ? 120 : 12;
            return Math.max(0, pageWidth - x - continuousLabelRightInset);
        }
        if (pageWidth == 640) {
            if (x < 100 && y <= 20) return Math.max(0, 628 - x - rightInset);
            if (x < 300) return Math.max(0, 300 - x - rightInset);
            return Math.max(0, 628 - x - rightInset);
        }
        if (pageWidth == 0) return Math.max(0, 628 - x - rightInset);
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

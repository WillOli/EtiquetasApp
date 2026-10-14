package model;

/** Printer-supported style options supplied by the label-style laboratory. */
public class LabelStyle {
    private int fontScale = 100;
    private String alignment = "center";
    private String spacing = "normal";
    private String fontFamily = "0";
    private String border = "none";

    public int getFontScale() { return fontScale; }
    public void setFontScale(int fontScale) { this.fontScale = fontScale; }
    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment; }
    public String getSpacing() { return spacing; }
    public void setSpacing(String spacing) { this.spacing = spacing; }
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    public String getBorder() { return border; }
    public void setBorder(String border) { this.border = border; }
}

package model;

/** Printer-supported style options supplied by the label-style laboratory. */
public class LabelStyle {
    private int fontScale = 100;
    private String alignment = "center";
    private double offsetXmm;
    private double offsetYmm;
    private Double firstLabelOffsetXmm;
    private Double firstLabelOffsetYmm;
    private Double secondLabelOffsetXmm;
    private Double secondLabelOffsetYmm;
    private String firstLabelAlignment;
    private String secondLabelAlignment;
    private String spacing = "normal";
    private String fontFamily = "0";
    private String border = "none";

    public int getFontScale() { return fontScale; }
    public void setFontScale(int fontScale) { this.fontScale = fontScale; }
    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment; }
    public double getOffsetXmm() { return offsetXmm; }
    public void setOffsetXmm(double offsetXmm) { this.offsetXmm = offsetXmm; }
    public double getOffsetYmm() { return offsetYmm; }
    public void setOffsetYmm(double offsetYmm) { this.offsetYmm = offsetYmm; }
    public Double getFirstLabelOffsetXmm() { return firstLabelOffsetXmm; }
    public void setFirstLabelOffsetXmm(Double value) { this.firstLabelOffsetXmm = value; }
    public Double getFirstLabelOffsetYmm() { return firstLabelOffsetYmm; }
    public void setFirstLabelOffsetYmm(Double value) { this.firstLabelOffsetYmm = value; }
    public Double getSecondLabelOffsetXmm() { return secondLabelOffsetXmm; }
    public void setSecondLabelOffsetXmm(Double value) { this.secondLabelOffsetXmm = value; }
    public Double getSecondLabelOffsetYmm() { return secondLabelOffsetYmm; }
    public void setSecondLabelOffsetYmm(Double value) { this.secondLabelOffsetYmm = value; }
    public String getFirstLabelAlignment() { return firstLabelAlignment; }
    public void setFirstLabelAlignment(String value) { this.firstLabelAlignment = value; }
    public String getSecondLabelAlignment() { return secondLabelAlignment; }
    public void setSecondLabelAlignment(String value) { this.secondLabelAlignment = value; }
    public String getSpacing() { return spacing; }
    public void setSpacing(String spacing) { this.spacing = spacing; }
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    public String getBorder() { return border; }
    public void setBorder(String border) { this.border = border; }
}

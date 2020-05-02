package Main;

import org.opencv.core.Scalar;

public enum color {
    YELLOW(20, 255, -1, -6, new Scalar(0,50,130), new Scalar(255, 255, 255), new Scalar(30, 100, 100)),//soft thresh targets
    RED(180, 255, -1, -80, new Scalar(0, 50, 130), new Scalar(255, 255, 255), new Scalar(30, 100, 100)),
    BLUE(120, 150, -1, -80, new Scalar(0, 50, 80), new Scalar(255, 255, 255), new Scalar(30, 100, 100)),
    WHITE(-1, -1, 255, -120, new Scalar(0, 0, 200), new Scalar(255, 50, 255), new Scalar(255, 100, 255));

    public int hue;
    public int sat;
    public int val;
    public int standoutness;

    public Scalar allowedRangeLower;
    public Scalar allowedRangeUpper;

    public Scalar hardRange;

    color(int hue, int sat, int val, int standoutness, Scalar allowedRangeLower, Scalar allowedRangeUpper, Scalar hardHueRange) {
        this.hue = hue;
        this.sat = sat;
        this.val = val;
        this.standoutness = standoutness;
        this.allowedRangeLower = allowedRangeLower;
        this.allowedRangeUpper = allowedRangeUpper;
        this.hardRange = hardHueRange; //So HSV
    }
}

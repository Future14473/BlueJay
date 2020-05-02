import Main.Display;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

public class Try {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat m = new Mat(100,100, CvType.CV_8UC1);
        Core.bitwise_not(m, m);
        Display.show(m, "m");
        HighGui.waitKey(0);
        System.exit(69);

        System.out.println("done");
    }
}

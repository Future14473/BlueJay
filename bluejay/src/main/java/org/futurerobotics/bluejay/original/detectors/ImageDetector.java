package org.futurerobotics.bluejay.original.detectors;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.futurerobotics.bluejay.original.Localizers.Localizer;
import org.futurerobotics.bluejay.original.Localizers.orientation;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

public class ImageDetector implements Localizer {
    private static final String VUFORIA_KEY =
            "AdpOQvf/////AAABmVMPMsn7aUUnm1KFPBduEHRDxu+mTcwssSUu9XwZdfhNnf" +
                    "gEDfs+klqAkWKOsuZBS8101agFiMYFoJUZvnBjKYUYF2omelpSnPnhAS" +
                    "vQBrte9NaKCwWtg7tkzpA6HLBwz85LOG7q+qg+XMNJ2VSlbq4kKN0jD6" +
                    "j0vDG2acgcrI4htjNpNtpv6U41IvMK8EE8baX9HvVohRfZxP40UXV7noV" +
                    "rXHTlErsHWMxAAtRoIzkp1Ro7qfueNcjYRgsjHLaeCfAhQA4IQK28Xqir" +
                    "J8Jv2Ek7cdWYvzHpa3uVyuF76chz7qGc4eAw8nSC0Ebqd0yf9S0oyqIh7" +
                    "MhUBCtvSvbsCpnbWHG/30IBpkC6ENlZhJSh";

    private static VuforiaLocalizer VuforiaStatic = null;
    private OpMode opMode;
    private HardwareMap hardwareMap;
    public Telemetry telemetry;
    private OpenGLMatrix Location = null;//will be null if not updated
    Bitmap cameraraw; //last seen image

    //phone attributes
    private static final boolean PHONE_IS_PORTRAIT = false;
    private float phoneXRotate = 0;
    private float phoneYRotate = 0;
    private float phoneZRotate = 0;
    // Everything is in mm
    private static final float mmPerInch = 25.4f;
    private static final float mmTargetHeight = (6) * mmPerInch;          // the height of the center of the target image above the floor

    // Constant for Stone Target
    private static final float stoneZ = 2.00f * mmPerInch;

    // Constants for the center support targets
    private static final float bridgeZ = 6.42f * mmPerInch;
    private static final float bridgeY = 23 * mmPerInch;
    private static final float bridgeX = 5.18f * mmPerInch;
    private static final float bridgeRotY = 59;                                 // Units are degrees
    private static final float bridgeRotZ = 180;

    // Constants for perimeter targets
    private static final float halfField = 72 * mmPerInch;
    private static final float quadField = 36 * mmPerInch;

    //list of trackable types
    VuforiaTrackables allTrackables;

    volatile boolean activated=false;

    Thread run = new Thread(){
        @Override
        public void run() {
            while (activated){
                updateposition();
            }
        }
    };

    public ImageDetector(OpMode opMode) {
        this(opMode,false);
    }
    public ImageDetector(OpMode opMode, boolean useDisplay) {

        this.opMode = opMode;
        this.hardwareMap = opMode.hardwareMap;
        this.telemetry = opMode.telemetry;

        if(isVuforiaInitialized())return;

        SetupVuforia(useDisplay);

        allTrackables = VuforiaStatic.loadTrackablesFromAsset("Skystone");

        setuptrackables(allTrackables);

        setupPhone();
    }

    public void start() {
        for (VuforiaTrackable trackable : allTrackables) {
            allTrackables.activate();
        }

        activated=true;
        run.start();
    }

    public void stop() {
        activated=false;

        for (VuforiaTrackable trackable : allTrackables) {
            allTrackables.deactivate();
        }
    }

    /**
     * Will be null if unavailable
     * @return
     */
    public orientation getPosition(){

        if (Location !=null && activated) {
            // express position (translation) of robot in inches.
            VectorF translation = Location.getTranslation();
            // express the rotation of the robot in degrees.
            Orientation rotation = Orientation.getOrientation(Location, EXTRINSIC, XYZ, DEGREES);

            //translation.get(n) 0:x 1:y 2:z  Also remember the constant mmPerInch for Unit conversions
            //rotation.firstangle 1st:roll 2nd:pitch 3rd:heading

            return new orientation(translation.get(0),translation.get(1),rotation.thirdAngle);
        } else {
            return null;
        }
    }

    private void updateposition() {
        List<VuforiaTrackable> ret = new ArrayList<VuforiaTrackable>();

        for (VuforiaTrackable trackable : allTrackables) {
            if (((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()) {

                if(trackable.getName().equals("Stone Target")){
                    //we cannot depend on this for coordinates
                    continue;
                    //TODO future use
                }

                //telemetry.addData("Visible Target", trackable.getName());

                // getUpdatedRobotLocation() will return null if no new information is available since
                // the last time that call was made, or if the trackable is not currently visible.
                OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener) trackable.getListener()).getUpdatedRobotLocation();
                if (robotLocationTransform != null) {
                    Location = robotLocationTransform;
                }
                break;//we can only work with one position, for now
            }
        }
    }

    /**
     *
     * @return Bitmap that will be null if image unavailable
     */
    public Bitmap getImage(){
        updateImage();

        return cameraraw;
    }

    private void updateImage(){
        cameraraw = null;

        try {
            VuforiaLocalizer.CloseableFrame closeableFrame = VuforiaStatic.getFrameQueue().take();

            for (int i = 0; i < closeableFrame.getNumImages(); i++) {
                Image image = closeableFrame.getImage(i);

                if (image.getFormat() == PIXEL_FORMAT.RGB565) {

                    Bitmap bm = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
                    bm.copyPixelsFromBuffer(image.getPixels());
                    cameraraw=bm;

                    break;
                }
            }

            closeableFrame.close();
        } catch (InterruptedException e) { }

    }

    public void printposition(orientation toprint) {
        if (toprint !=null) {
            telemetry.addData("Vuforia-Position (mm) (rot)",toprint.x+" "+toprint.y+" "+toprint.rot);
        } else {
            telemetry.addData("Vuforia", "offline");
        }
    }
    /**
     * ensures that the static vuforia is made
     **/
    private void SetupVuforia(boolean useDisplay) {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        //Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
        VuforiaLocalizer.Parameters parameters;
        if(useDisplay)  parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        else            parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        VuforiaLocalizer vuforia = ClassFactory.getInstance().createVuforia(parameters);

        //not necessary, you can ignore it
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);

        VuforiaStatic = vuforia;
    }

    private void setuptrackables(VuforiaTrackables targetsSkyStone) {
        // Load the data sets for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.

        VuforiaTrackable stoneTarget = targetsSkyStone.get(0);
        stoneTarget.setName("Stone Target");
        VuforiaTrackable blueRearBridge = targetsSkyStone.get(1);
        blueRearBridge.setName("Blue Rear Bridge");
        VuforiaTrackable redRearBridge = targetsSkyStone.get(2);
        redRearBridge.setName("Red Rear Bridge");
        VuforiaTrackable redFrontBridge = targetsSkyStone.get(3);
        redFrontBridge.setName("Red Front Bridge");
        VuforiaTrackable blueFrontBridge = targetsSkyStone.get(4);
        blueFrontBridge.setName("Blue Front Bridge");
        VuforiaTrackable red1 = targetsSkyStone.get(5);
        red1.setName("Red Perimeter 1");
        VuforiaTrackable red2 = targetsSkyStone.get(6);
        red2.setName("Red Perimeter 2");
        VuforiaTrackable front1 = targetsSkyStone.get(7);
        front1.setName("Front Perimeter 1");
        VuforiaTrackable front2 = targetsSkyStone.get(8);
        front2.setName("Front Perimeter 2");
        VuforiaTrackable blue1 = targetsSkyStone.get(9);
        blue1.setName("Blue Perimeter 1");
        VuforiaTrackable blue2 = targetsSkyStone.get(10);
        blue2.setName("Blue Perimeter 2");
        VuforiaTrackable rear1 = targetsSkyStone.get(11);
        rear1.setName("Rear Perimeter 1");
        VuforiaTrackable rear2 = targetsSkyStone.get(12);
        rear2.setName("Rear Perimeter 2");

        stoneTarget.setLocation(OpenGLMatrix
                .translation(0, 0, stoneZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        //Set the position of the bridge support targets with relation to origin (center of field)
        blueFrontBridge.setLocation(OpenGLMatrix
                .translation(-bridgeX, bridgeY, bridgeZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 0, bridgeRotY, bridgeRotZ)));

        blueRearBridge.setLocation(OpenGLMatrix
                .translation(-bridgeX, bridgeY, bridgeZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 0, -bridgeRotY, bridgeRotZ)));

        redFrontBridge.setLocation(OpenGLMatrix
                .translation(-bridgeX, -bridgeY, bridgeZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 0, -bridgeRotY, 0)));

        redRearBridge.setLocation(OpenGLMatrix
                .translation(bridgeX, -bridgeY, bridgeZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 0, bridgeRotY, 0)));

        //Set the position of the perimeter targets with relation to origin (center of field)
        red1.setLocation(OpenGLMatrix
                .translation(quadField, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        red2.setLocation(OpenGLMatrix
                .translation(-quadField, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        front1.setLocation(OpenGLMatrix
                .translation(-halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

        front2.setLocation(OpenGLMatrix
                .translation(-halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

        blue1.setLocation(OpenGLMatrix
                .translation(-quadField, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));

        blue2.setLocation(OpenGLMatrix
                .translation(quadField, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));

        rear1.setLocation(OpenGLMatrix
                .translation(halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        rear2.setLocation(OpenGLMatrix
                .translation(halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

    }

    private void setupPhone() {
        // We need to rotate the camera around its long axis to bring the correct camera forward.
        //if (CAMERA_CHOICE == BACK) { We always use the back camera
        if (true) {
            phoneYRotate = -90;
        } else {
            phoneYRotate = 90;
        }

        // Rotate the phone vertical about the X axis if it's in portrait mode
        if (PHONE_IS_PORTRAIT) {
            phoneXRotate = 90;
        }

        // Next, translate the camera lens to where it is on the robot.
        // In this example, it is centered (left to right), but forward of the middle of the robot, and above ground level.
        final float CAMERA_FORWARD_DISPLACEMENT = 4.0f * mmPerInch;   // eg: Camera is 4 Inches in front of robot center
        final float CAMERA_VERTICAL_DISPLACEMENT = 8.0f * mmPerInch;   // eg: Camera is 8 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix robotFromCamera = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES, phoneYRotate, phoneZRotate, phoneXRotate));

        /**  Let all the trackable listeners know where the phone is.  */
        for (VuforiaTrackable trackable : allTrackables) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(robotFromCamera, BACK);
        }
    }

    public void print(String s){
       //telemetry.addData("",s);
       //telemetry.update();
    }

    public static boolean isVuforiaInitialized(){
        return  VuforiaStatic!=null;
    }

    public static VuforiaLocalizer getVuforia(){
        return  VuforiaStatic;
    }

}

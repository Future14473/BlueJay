package org.futurerobotics.bluejay.original.CVBridger;

import android.app.Activity;
import android.content.Context;
import android.view.Surface;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;


public abstract class OpenCVPipeline implements CameraBridgeViewBase.CvCameraViewListener2 {

    //OpenCV-related
    protected        JavaCameraView cameraView;
    private volatile ViewDisplay    viewDisplay;
    protected        Context        context;
    private          boolean        initStarted = false;
    private          boolean        inited      = false;

    //Loads the OpenCV library
    static {
        try {
            System.loadLibrary("opencv_java3");
        } catch (UnsatisfiedLinkError e) {
            OpenCVLoader.initDebug();
            // pass
        }
    }

    /**
     * Initializes the OpenCVPipeline.
     *
     * @param context     the application context, usually hardwareMap.appContext
     * @param viewDisplay the ViewDisplay that will display the underlying JavaCameraView to the screen;
     *                    in most cases, using CameraViewDisplay.getInstance() as the argument is just fine.
     */
    public void init(Context context, ViewDisplay viewDisplay) {
        this.initStarted = true;
        this.viewDisplay = viewDisplay;
        this.context = context;

        //Starts CV on separate thread
        final Activity activity = (Activity) context;
        final Context finalContext = context;
        final CameraBridgeViewBase.CvCameraViewListener2 self = this;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // JavaCameraViews must be instantiated on a UI thread

                //0 is front
                cameraView = new CustomCameraView(finalContext, 0);
                cameraView.setCameraIndex(0);
                cameraView.setCvCameraViewListener(self);
                cameraView.enableFpsMeter();

                inited = true;
            }
        });
    }

    /**
     * Enables the detector.
     * This function must be called AFTER init().
     *
     * @throws IllegalStateException if enable() is called before init()
     */
    public void enable() {
        //NEED to have been INITED before start
        if (!initStarted)
            throw new IllegalStateException("init() needs to be called before an OpenCVPipeline can be enabled!");
        try {
            while (!inited) Thread.sleep(10);
        } catch (InterruptedException e) {
            return;
        }


        //Runs if simple DogeCV
        cameraView.enableView();
        viewDisplay.setCurrentView(context, getCameraView());

    }

    /**
     * Be careful not to:
     * - disable twice
     * - disable before enabling
     */
    public void disable() {
        cameraView.disableView();

        viewDisplay.removeCurrentView(context);
    }

    /**
     * Exposes the underlying JavaCameraView used. Before init() is called, this is null.
     *
     * @return the JavaCameraView.
     */
    public JavaCameraView getCameraView() {
        return cameraView;
    }

    /**
     * This function is called when the camera is started; overriding this may be useful to set the
     * maximum width and height parameters of an image processing pipeline.
     *
     * @param width  -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    /**
     * Override this function if there should be logic on camera close.
     */
    @Override
    public void onCameraViewStopped() {
    }

    /**
     * The method that calls {@link #processFrame(Mat, Mat)}; there's little reason to override this, if ever.
     *
     * @param inputFrame the input frame given by the internal JavaCameraView
     * @return the result of {@link #processFrame(Mat, Mat)}
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = new Mat();
        Mat gray = new Mat();

        switch (((Activity) context).getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                // These methods don't work. Please tell Levi if you ever find yourself needing to use them!
                Core.rotate(inputFrame.rgba(), rgba, Core.ROTATE_90_CLOCKWISE);
                Core.rotate(inputFrame.gray(), gray, Core.ROTATE_90_CLOCKWISE);
                break;
            case Surface.ROTATION_90:
                rgba = inputFrame.rgba();
                gray = inputFrame.gray();
                break;
            case Surface.ROTATION_270:
                // These methods don't work. Please tell Levi if you ever find yourself needing to use them!
                Core.rotate(inputFrame.rgba(), rgba, Core.ROTATE_180);
                Core.rotate(inputFrame.gray(), gray, Core.ROTATE_180);
                break;
        }


        return processFrame(rgba);
    }

    /**
     * Override this with the main image processing logic. This is run every time the camera receives a frame.
     *
     * @param rgba a {@link Mat} that is in RGBA format
     * @return the Mat that should be displayed to the screen; in most cases one would probably just want to return rgba
     */
    public abstract Mat processFrame(Mat rgba);
}
package org.firstinspires.ftc.teamcode.Original.CVBridger;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import org.opencv.BuildConfig;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class CustomCameraView extends JavaCameraView {
    private static final String TAG = "CustomCameraView";

    public CustomCameraView(Context context, int cameraId) {
        super(context, cameraId);
        enableFpsMeter();
    }

    @Override
    protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
        Mat modified;

        int deviceOrientation = getContext().getResources().getConfiguration().orientation;


        modified = frame.rgba();


        boolean bmpValid = true;

        if (modified != null) {
            try {
                // fix bitmap size
                if (mCacheBitmap.getWidth() != modified.cols() || mCacheBitmap.getHeight() != modified.rows()) {
                    mCacheBitmap = Bitmap.createBitmap(modified.cols(), modified.rows(), Bitmap.Config.ARGB_8888);
                }
                Utils.matToBitmap(modified, mCacheBitmap);
            } catch(Exception e) {
                Log.e(TAG, "Mat type: " + modified.cols() + "*" + modified.rows());
                Log.e(TAG, "Bitmap type: " + mCacheBitmap.getWidth() + "*" + mCacheBitmap.getHeight());
                Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
                bmpValid = false;
            }
        }

        if (bmpValid && mCacheBitmap != null) {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "mStretch value: " + mScale);

                // commented out bc this can add distortion to the image
                // maximize size of the bitmap to remove black borders in portrait orientation
                //mCacheBitmap = Bitmap.createScaledBitmap(mCacheBitmap, canvas.getHeight(), canvas.getWidth(), true);

                if (mScale != 0) {
                    canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                            new Rect((int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2),
                                    (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2),
                                    (int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2 + mScale*mCacheBitmap.getWidth()),
                                    (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2 + mScale*mCacheBitmap.getHeight())), null);
                } else {
                    canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                            new Rect((canvas.getWidth() - mCacheBitmap.getWidth()) / 2,
                                    (canvas.getHeight() - mCacheBitmap.getHeight()) / 2,
                                    (canvas.getWidth() - mCacheBitmap.getWidth()) / 2 + mCacheBitmap.getWidth(),
                                    (canvas.getHeight() - mCacheBitmap.getHeight()) / 2 + mCacheBitmap.getHeight()), null);
                }

                // temporarily rotate canvas to draw FPS meter in correct orientation in portrait
                if(deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    canvas.save();

                    canvas.rotate(-90, getWidth() / 2, getHeight() / 2);

                    if (mFpsMeter != null) {
                        mFpsMeter.measure();
                        mFpsMeter.draw(canvas, 20, 30);
                    }

                    canvas.restore();
                }

                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }


}

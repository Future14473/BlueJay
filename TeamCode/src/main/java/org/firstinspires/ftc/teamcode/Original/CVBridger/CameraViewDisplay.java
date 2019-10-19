package org.firstinspires.ftc.teamcode.Original.CVBridger;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class CameraViewDisplay implements ViewDisplay {
    private static CameraViewDisplay instance;

    View view;
    private CameraViewDisplay() {
    }

    public static CameraViewDisplay getInstance() {
        if (instance == null) instance = new CameraViewDisplay();
        return instance;
    }

    public void setCurrentView(final Context context, View newView) {
        // finding the resID dynamically allows this class to exist outside of the TeamCode module
        final int resID = context.getResources().getIdentifier("RelativeLayout", "id", context.getPackageName());
        final Activity activity = (Activity) context;
        final View queuedView = newView;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup l = (ViewGroup) activity.findViewById(resID); //R.id.RelativeLayout);
                if (view != null) {
                    l.removeView(view);
                }
                l.addView(new ScrollView(context));
                l.addView(queuedView);
                view = queuedView;
            }
        });
    }

    public void removeCurrentView(Context context) {
        final int resID = context.getResources().getIdentifier("RelativeLayout", "id", context.getPackageName());
        final Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //cameraMonitorViewId
                ViewGroup l = (ViewGroup) activity.findViewById(resID); // .id.RelativeLayout);
                if (view != null) {
                    l.removeView(view);
                }
                view = null;
            }
        });
    }
}

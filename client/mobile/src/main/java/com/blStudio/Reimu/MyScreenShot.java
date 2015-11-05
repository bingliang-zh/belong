package com.blStudio.Reimu;

import android.app.Activity;
import android.os.Environment;

import java.util.Date;

/**
 * Created by BL on 2015/9/24.
 */
public class MyScreenShot {

    private static Activity mApp;

    public static void screenShot(Activity instance){
        mApp = instance;
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        String mPath = Environment.getExternalStorageDirectory().toString() + "/belong_" + now + ".png";
        LAppRenderer.captureFrame(mPath, mApp);
        showTip(mApp.getString(R.string.screen_shot_tip));
    }

    private static void showTip(String str){
        MainActivity.showTip(str);
    }
}

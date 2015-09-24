package com.blStudio.belong;

import android.app.Activity;

/**
 * Created by BL on 2015/9/24.
 */
public class MyPrintScreen {
    public static void printScreen(Activity mApp){
        showTip("blahblahblahblah");
    }

    private static void showTip(String str){
        MainActivity.showTip(str);
    }
}

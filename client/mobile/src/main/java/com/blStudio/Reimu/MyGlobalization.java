package com.blStudio.Reimu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by bl_indie on 2015/11/4.
 */
public class MyGlobalization {

    private static Activity mApp;

    public static void init(Activity activity){
        mApp = activity;
        SharedPreferences setting=mApp.getSharedPreferences("setting", Context.MODE_PRIVATE);
        String str = setting.getString("locale", "");
        Locale locale;

        if (str.equals("")) {
            // 使用系统语言
            locale = Locale.getDefault();
        } else {
            locale =  Str2Loc(str);
        }

        Resources resources =mApp.getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等。
        config.locale = locale;
        resources.updateConfiguration(config, dm);
    }

    public static void updateLanguage(Locale locale) {
        SharedPreferences sharedPreferences=mApp.getSharedPreferences("setting", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("locale", Loc2Str(locale)).apply();
        mApp.finish();
        mApp.startActivity(mApp.getIntent());
    }

    public static String Loc2Str(Locale l) {
        return l.getLanguage() + "," + l.getCountry();
    }

    public static Locale Str2Loc(String s) {
        StringTokenizer tempStringTokenizer = new StringTokenizer(s,",");
        String l = "";
        String c = "";
        if(tempStringTokenizer.hasMoreTokens()) {
            l = tempStringTokenizer.nextToken();
        }
        if(tempStringTokenizer.hasMoreTokens()) {
            c = tempStringTokenizer.nextToken();
        }
        return new Locale(l,c);
    }
}

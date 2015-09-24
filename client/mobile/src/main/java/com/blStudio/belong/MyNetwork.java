package com.blStudio.belong;

import android.app.Activity;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bl_indie on 2015/9/24.
 */
public class MyNetwork {

    private static Activity mApp;

    public static void init(Activity activity){
        mApp = activity;
        MyTuringRobot.init(mApp);
    }
    public static void sendMessage(String inputStr){
        switch (inputStr) {
            case "开灯。":
            case "开灯":
                new DownloadTask().execute(mApp.getString(R.string.home_url) + "gpio/gpio_set.php?id=38&mode=out&voltage=high");
                break;
            case "关灯":
                new DownloadTask().execute(mApp.getString(R.string.home_url) + "gpio/gpio_set.php?id=38&mode=out&voltage=low");
                break;
            default:
                MyTuringRobot.getTuringRobotReply(inputStr);
                break;
        }
    }

    public static void printResult(String result) {
        switch (result) {
//            case "开灯":
//            case "开灯。":
//                new DownloadTask().execute(getString(R.string.home_url) + "gpio/gpio_set.php?id=38&mode=out&voltage=high");
//                showOnInComingBalloon("灯已打开~");
//                break;
//            case "关灯":
//            case "关灯。":
//                new DownloadTask().execute(getString(R.string.home_url) + "gpio/gpio_set.php?id=38&mode=out&voltage=low");
//                showOnInComingBalloon("灯已关闭~");
//                break;
            default:
                MyTuringRobot.getTuringRobotReply(result);
                break;
        }

    }

    public static void selectItem(int position) {
        switch (position){
            case 0:
                new DownloadTask().execute(mApp.getString(R.string.home_url)+"get_temperature.php");
                break;
            case 1:
                new DownloadTask().execute(mApp.getString(R.string.home_url)+"gpio/gpio_set.php?id=7&mode=in");
                break;
            case 2:
                new DownloadTask().execute(mApp.getString(R.string.home_url)+"gpio/gpio_set.php?id=38&mode=out&voltage=high");
                break;
            case 3:
                new DownloadTask().execute(mApp.getString(R.string.home_url)+"gpio/gpio_set.php?id=38&mode=out&voltage=low");
                break;
            default:
                break;
        }
    }

    private static class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
//                Resources res = mApp.getResources();
                return mApp.getString(R.string.connection_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject mJson = new JSONObject(result);
                String name = mJson.optString("name");
                if (name.equals("temperature")){
                    double var = mJson.optDouble("var");
                    double var1 = (double)((int)(var/100))/10;
                    showTip("服务器CPU温度为" + var1 + "摄氏度");
                }
                else if(name.equals("gpio")){
                    int id = mJson.optInt("id");
                    String mode = mJson.optString("mode");
                    if(id==7&&mode.equals("in")){
                        int var = mJson.getInt("var");
                        if(var==0){
                            showTip("植物不需要浇水~");
                        }
                        else{
                            showTip("要浇水啦要浇水啦！");
                        }
                    }
                    else if(id==38&&mode.equals("out")){
                        String var = mJson.getString("var");
                        if(var.equals("high")){
                            showTip("灯已打开~");
                        }
                        else{
                            showTip("灯已关闭~");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString);
            str = readIt(stream, 500);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }

    private static InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(1000 /* milliseconds */);
        conn.setConnectTimeout(3000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Start the query
        conn.connect();
        return conn.getInputStream();
    }

    private static String readIt(InputStream stream, int len) throws IOException {
        if (stream != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[len];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(stream, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            }
            finally {
                stream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    private static void showTip(String str){
        MainActivity.showTip(str);
    }
}

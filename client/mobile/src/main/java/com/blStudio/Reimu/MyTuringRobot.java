package com.blStudio.Reimu;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by bl_indie on 2015/9/24.
 */
public class MyTuringRobot {

    private static Activity mApp;

    public static void init(Activity activity){
        mApp = activity;
    }

    public static void getTuringRobotReply(String outgoingString){
        Log.d("TuringRobot", outgoingString);
        String info;
        try {
            info = URLEncoder.encode(outgoingString, "UTF-8");
            String url = mApp.getString(R.string.turing_robot_api)
                    +"key="+mApp.getString(R.string.turing_robot_key)
                    +"&info="+info;
            Log.d("TuringRobot",url);
            new GetTuringRobot().execute(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static class GetTuringRobot extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return MyNetwork.loadFromNetwork(urls[0]);
            } catch (IOException e) {
                showTip("TuringRobot后台暂时无法访问");
                return mApp.getString(R.string.connection_error);
            }
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(String replyJson) {
            Log.d("TuringRobot", replyJson);
            try {
                JSONObject mJson = new JSONObject(replyJson);
                int code = mJson.optInt("code");
                String text = mJson.optString("text");
                String url;
                switch (code){
                    case 100000:break;//文本类数据
                    case 305000:
                        text = text+" 列车详情功能尚未完成";
                        break;//列车
                    case 306000:
                        text = text+" 航班详情功能尚未完成";
                        break;//航班
                    case 200000:
                        url = mJson.optString("url");
                        text = text+" "+url;
                        break;//网址类数据
                    case 302000:
                        text = text+" 新闻详情功能尚未完成";
                        break;//新闻
                    case 308000:
                        text = text+" 菜谱、视频、小说详情功能尚未完成";
                        break;//菜谱、视频、小说
                    case 40001:showTip("key的长度错误（32位）");break;//key的长度错误（32位）
                    case 40002:showTip("请求内容为空");break;//请求内容为空
                    case 40003:showTip("key错误或帐号未激活");break;//key错误或帐号未激活
                    case 40004:showTip("当天请求次数已用完");break;//当天请求次数已用完
                    case 40005:showTip("暂不支持该功能");break;//暂不支持该功能
                    case 40006:showTip("服务器升级中");break;//服务器升级中
                    case 40007:showTip("服务器数据格式异常");break;//服务器数据格式异常
                    default:showTip("未知错误码"+code);break;
                }
                Log.d("TuringRobot",text);
                if(text==null||text.equals("")){
                    text = mApp.getString(R.string.robot_no_reply);
                }
                Log.d("TuringRobot",text);
                MainActivity.showOnIncomingBalloon(text);
                MainActivity.startSpeaking(text);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private static void showTip(String str){
        MainActivity.showTip(str);
    }

}

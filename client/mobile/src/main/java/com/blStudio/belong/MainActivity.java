/**
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */

package com.blStudio.belong;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import jp.live2d.utils.android.FileManager;
import jp.live2d.utils.android.SoundManager;

public class MainActivity extends Activity implements MyLeftDrawer.OnItemClickListener{

    static private Activity instance;

    // Live2d相关
    private LAppLive2DManager live2DMgr ;
    public static Boolean lipSync = true;
    public static boolean isSpeaking = false;

    // 界面相关
    private static EditText mEditText;
    private static Button oBalloon;
    private static Button iBalloon;
    private static Toast mToast;
    static ObjectAnimator oBalloonFadeOut;
    static ObjectAnimator iBalloonFadeOut;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        instance=this;
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // live2d
        SoundManager.init(this);
        live2DMgr = new LAppLive2DManager();
        FileManager.init(this.getApplicationContext());
        
        // 初始化讯飞语音云平台
        MyVoiceCloud.init(instance);

        // 界面
        setContentView(R.layout.activity_main);
        setupGUI();
    }
    
    void setupGUI(){        
        LAppView view = live2DMgr.createView(this) ;
        
        FrameLayout layout=(FrameLayout) findViewById(R.id.live2DLayout);
        layout.addView(view, 0, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        ImageButton imgBtnVoice = (ImageButton)findViewById(R.id.imgBtnVoice);
        imgBtnVoice.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                MyVoiceCloud.getVoice();
            }
        });
        
        ImageButton imgBtnSend = (ImageButton)findViewById(R.id.imgBtnSend);
        imgBtnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendMessage();
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        mEditText = (EditText)findViewById(R.id.mainEditText);
        oBalloon = (Button)findViewById(R.id.outgoingBalloon);
        iBalloon = (Button)findViewById(R.id.incomingBalloon);
        oBalloonFadeOut = MyAnimation.setFadeOut(oBalloon);
        iBalloonFadeOut = MyAnimation.setFadeOut(iBalloon);
        MyLeftDrawer.init(instance);
        MyLeftDrawer.mDrawerList.setAdapter(new MyLeftDrawer(MyLeftDrawer.mDrawerTitles, this));
    }

    void sendMessage(){
        String inputStr = mEditText.getText().toString();
        if (inputStr.equals("")) {
            //未输入字符
            showTip(getString(R.string.no_text_in_edit_text));
        }
        else{
            mEditText.setText("");
            showOnBalloon(inputStr, oBalloon, oBalloonFadeOut);
            switch (inputStr) {
                case "开灯。":
                case "开灯":
                    new DownloadTask().execute(getString(R.string.home_url) + "gpio/gpio_set.php?id=38&mode=out&voltage=high");
                    break;
                case "关灯":
                    new DownloadTask().execute(getString(R.string.home_url) + "gpio/gpio_set.php?id=38&mode=out&voltage=low");
                    break;
                default:
                    getTuringRobotReply(inputStr);
                    break;
            }
        }
    }
    
    @Override
    protected void onResume(){
        //live2DMgr.onResume() ;
        super.onResume();
    }
    
    @Override
    protected void onPause(){
        live2DMgr.onPause() ;
        super.onPause();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MyLeftDrawer.onConfigurationChanged(newConfig);
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = MyLeftDrawer.isDrawerOpen();
        menu.findItem(R.id.author_detail).setVisible(!drawerOpen);
        menu.findItem(R.id.change_model).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // call ActionBarDrawerToggle.onOptionsItemSelected(), if it returns true
        // then it has handled the app icon touch event
        if (MyLeftDrawer.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()){
            case R.id.author_detail:
                onAuthorDetailClicked();
                return true;
            case R.id.change_model:
                showTip(getString(R.string.change_model));
                live2DMgr.changeModel();//Live2D Event
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /* The click listener for RecyclerView in the navigation drawer */
    @Override
    public void onClick(View view, int position) {
        selectItem(position);
    }
    
    private long exitTime = System.currentTimeMillis();
    
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        //双击后退键退出
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime)>2000) {
                showTip(getString(R.string.click_KEYCODE_BACK));
                exitTime = System.currentTimeMillis();
            } else {
                exit();
            }
        }
        return false;
    }

    private void selectItem(int position) {
        switch (position){
        case 0:
            new DownloadTask().execute(getString(R.string.home_url)+"get_temperature.php");
            break;
        case 1:
            new DownloadTask().execute(getString(R.string.home_url)+"gpio/gpio_set.php?id=7&mode=in");
            break;
        case 2:
            new DownloadTask().execute(getString(R.string.home_url)+"gpio/gpio_set.php?id=38&mode=out&voltage=high");
            break;
        case 3:
            new DownloadTask().execute(getString(R.string.home_url)+"gpio/gpio_set.php?id=38&mode=out&voltage=low");
            break;
        default:
            break;
        }
    }
    
    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
              return getString(R.string.connection_error);
            }
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject mJson = new JSONObject(result);
                String name = mJson.optString("name");
                if (name.equals("temperature")){
                    double var = mJson.optDouble("var");
                    double var1 = (double)((int)(var/100))/10;
                    showTip("服务器CPU温度为"+var1+"摄氏度");
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
    
    /** Initiates the fetch operation. */
    private static String loadFromNetwork(String urlString) throws IOException {
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
    
    /**
     * Given a string representation of a URL, sets up a connection and gets
     * an input stream.
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     * @throws java.io.IOException
     */
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

    /** Reads an InputStream and converts it to a String.
     * @param stream InputStream containing HTML from targeted site.
     * @param len Length of string that this method returns.
     * @return String concatenated according to len parameter.
     * @throws java.io.IOException
     * @throws java.io.UnsupportedEncodingException
     */
        
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

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        MyLeftDrawer.syncState();
    }
    
    void onAuthorDetailClicked(){
        Log.i("TAG", "=========选中作者详情键");
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setTitle(R.string.author_detail_title);
        builder.setMessage(R.string.author_detail_text);
        builder.setIcon(R.drawable.logo);
        builder.create().show();
    }

    public static void printResult(String result) {

        showOnBalloon(result, oBalloon, oBalloonFadeOut);
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
                getTuringRobotReply(result);
                break;
        }

    }

    private static void showOnBalloon(String str, Button btn, ObjectAnimator obj) {
        btn.setText(str);
        btn.setAlpha(1f);
        btn.setVisibility(View.VISIBLE);

        // 如果上一个动画还在则取消它
        if(obj.isStarted()){
            obj.cancel();
        }

        Log.d("FADEOUT", "Fadeout start");
        obj.start();
    }
    
    private static void getTuringRobotReply(String outgoingString){
        Log.d("TuringRobot", outgoingString);
        String info;
        try {
            info = URLEncoder.encode(outgoingString, "UTF-8");
            String url = instance.getString(R.string.turing_robot_api)
                    +"key="+instance.getString(R.string.turing_robot_key)
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
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
                showTip("TuringRobot后台暂时无法访问");
                return instance.getString(R.string.connection_error);
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
//                String list;
                switch (code){
                case 100000:break;//文本类数据
                case 305000:
//                  list = mJson.optString("list");
//                  text = text+" "+list;
                    text = text+" 列车详情功能尚未完成";
                    break;//列车
                case 306000:
//                  list = mJson.optString("list");
//                  text = text+" "+list;
                    text = text+" 航班详情功能尚未完成";
                    break;//航班
                case 200000:
                    url = mJson.optString("url");
                    text = text+" "+url;
                    break;//网址类数据
                case 302000:
//                  list = mJson.optString("list");
//                  text = text+" "+list;
                    text = text+" 新闻详情功能尚未完成";
                    break;//新闻
                case 308000:
//                  list = mJson.optString("list");
//                  text = text+" "+list;
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
//              if(result!=100){
//                  showTip(String.valueOf(result)+msg);
//              }
//              else{
//                  Log.d("TuringRobot",text);
//                  if(text==null||text.equals("")){
//                      text = getString(R.string.simsimi_no_reply);
//                  }
//                  Log.d("TuringRobot",text);
//                  
//              }
                Log.d("TuringRobot",text);
                if(text==null||text.equals("")){
                    text = instance.getString(R.string.robot_no_reply);
                }
                Log.d("TuringRobot",text);
                showOnBalloon(text, iBalloon, iBalloonFadeOut);
                startSpeaking(text);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    private static void startSpeaking(String str){
        if (MyVoiceCloud.startSpeaking(str)) {
            isSpeaking = true;
        }
    }

    public static void updateIsSpeaking(){
        isSpeaking = MyVoiceCloud.isSpeaking();
    }

    static public void exit(){
        SoundManager.release();
        instance.finish();
    }

    public static void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
}

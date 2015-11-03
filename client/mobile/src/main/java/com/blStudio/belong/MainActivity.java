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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import jp.live2d.utils.android.FileManager;
import jp.live2d.utils.android.SoundManager;

public class MainActivity extends Activity implements MyLeftDrawer.OnItemClickListener{

    static private Activity instance;
    private static Toast mToast;

    // Live2d相关
    private LAppLive2DManager live2DMgr ;
    public static Boolean lipSync = true;
    public static boolean isSpeaking = false;

    // 界面相关
    private static EditText mEditText;
    private static Button sendBalloon;
    private static Button replyBalloon;
    private static ObjectAnimator sendBalloonFadeOut;
    private static ObjectAnimator replyBalloonFadeOut;
    private static int spinnerCount;
    private static LinearLayout spinner;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        instance=this;
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        Message msg = Message.obtain(MainActivity.handler,
                MyDefine.SPINNER, MyDefine.SHOW);
        msg.sendToTarget();

        // live2d
        SoundManager.init(this);
        live2DMgr = new LAppLive2DManager();
        FileManager.init(this.getApplicationContext());
        
        // 初始化讯飞语音云平台
        MyVoiceCloud.init(instance);

        // 初始化自定义网络模块
        MyNetwork.init(instance);

        // 界面
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
        setupGUI();

        Message msg2 = Message.obtain(MainActivity.handler,
                MyDefine.SPINNER, MyDefine.HIDE);
        msg2.sendToTarget();
    }
    
    void setupGUI(){
        spinner = (LinearLayout)findViewById(R.id.loadingSpinner);
        spinnerCount = 0;

        LAppView view = live2DMgr.createView(this) ;

        FrameLayout layout=(FrameLayout) findViewById(R.id.live2DLayout);
        layout.addView(view, 0, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        ImageButton imgBtnVoice = (ImageButton)findViewById(R.id.imgBtnVoice);
        imgBtnVoice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyVoiceCloud.getVoice();
            }
        });
        
        ImageButton imgBtnSend = (ImageButton)findViewById(R.id.imgBtnSend);
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage();
            }
        });

        ImageButton imgBtnCamera = (ImageButton)findViewById(R.id.imgBtnCamera);
        imgBtnCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyScreenShot.screenShot(instance);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        mEditText = (EditText)findViewById(R.id.mainEditText);
        sendBalloon = (Button)findViewById(R.id.sendBalloon);
        replyBalloon = (Button)findViewById(R.id.replyBalloon);
        sendBalloonFadeOut = MyAnimation.setFadeOut(sendBalloon);
        replyBalloonFadeOut = MyAnimation.setFadeOut(replyBalloon);
        MyLeftDrawer.init(instance);
        MyLeftDrawer.mDrawerList.setAdapter(new MyLeftDrawer(MyLeftDrawer.mDrawerTitles, this));
    }

    void sendMessage(){
        String inputStr = mEditText.getText().toString();
        if (inputStr.equals("")) {
            // 未输入字符
            showTip(getString(R.string.no_text_in_edit_text));
        } else {
            mEditText.setText("");
            showOnBalloon(inputStr, sendBalloon, sendBalloonFadeOut);
            MyNetwork.sendMessage(inputStr);
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
        if (MyLeftDrawer.onOptionsItemSelected(item)) {
            return super.onOptionsItemSelected(item);
        }
        switch(item.getItemId()){
            case R.id.author_detail:
                onAuthorDetailClicked();
                break;
            case R.id.change_model:
//                showTip(getString(R.string.change_model));
                live2DMgr.changeModel();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void onAuthorDetailClicked(){
        Log.i("TAG", "=========选中作者详情键");
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setTitle(R.string.author_detail_title);
        builder.setMessage(R.string.author_detail_text);
        builder.setIcon(R.drawable.logo);
        builder.create().show();
    }
    
    /* The click listener for RecyclerView in the navigation drawer */
    @Override
    public void onClick(View view, int position) {
        MyLeftDrawer.selectItem(position);
    }
    
    private long exitTime = System.currentTimeMillis();
    
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        // 双击后退键退出
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ( (System.currentTimeMillis() - exitTime) > 2000) {
                showTip(getString(R.string.click_KEYCODE_BACK));
                exitTime = System.currentTimeMillis();
            } else {
                exit();
            }
        }
        return false;
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

    public static void printResult(String result) {
        showOnBalloon(result, sendBalloon, sendBalloonFadeOut);
        MyNetwork.printResult(result);
    }

    public static void showOnIncomingBalloon(String str){
        showOnBalloon(str, replyBalloon, replyBalloonFadeOut);
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

    public static void startSpeaking(String str){
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

    private static void updateSpinnerStatus(Object obj) {
        if (obj == MyDefine.SHOW) {
            spinnerCount++;
        }
        else if (obj == MyDefine.HIDE) {
            spinnerCount--;
        }

        if(spinnerCount > 0) {
            spinner.setVisibility(View.VISIBLE);
        }
        else {
            spinner.setVisibility(View.INVISIBLE);
        }
    }

    public static Handler handler = new MyHandler();

    private static class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MyDefine.SPINNER:
                    updateSpinnerStatus(msg.obj);
                    break;
                default: break;
            }
        }
    }

}

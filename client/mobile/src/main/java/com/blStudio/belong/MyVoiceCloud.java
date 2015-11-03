package com.blStudio.belong;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by bl_indie on 2015/9/23.
 */
public class MyVoiceCloud {

    private static SpeechSynthesizer mTts;
    private static String mEngineType = SpeechConstant.TYPE_CLOUD;
    private static SharedPreferences mTtsSharedPreferences;
    private static String voice = "xiaoqi";

    private static SpeechRecognizer mIat;
    private static SharedPreferences mIatSharedPreferences;
    private static HashMap<String, String> mIatResults = new LinkedHashMap<>();

    private static Activity mApp;

    public static void init(Activity activity){
        mApp = activity;
        SpeechUtility.createUtility(mApp, "appid=" + mApp.getString(R.string.voicecloud_app_id));
        mIat = SpeechRecognizer.createRecognizer(mApp, mInitListener);
        mIatSharedPreferences = mApp.getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        mTts = SpeechSynthesizer.createSynthesizer(mApp, mTtsInitListener);
        mTtsSharedPreferences = mApp.getSharedPreferences("com.iflytek.setting", Context.MODE_PRIVATE);
    }

    // ----------------Iat start----------------
    public static void getVoice(){
        mIatResults.clear();
        setIatParam();
        int code = mIat.startListening(recognizerListener);
        if (code != ErrorCode.SUCCESS) {
            showTip(mApp.getString(R.string.fail) + code);
        } else {
            showTip(mApp.getString(R.string.text_begin));
        }
    }

    public static void setIatParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mIatSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }
        // 设置语音前端点
        mIat.setParameter(SpeechConstant.VAD_BOS, mIatSharedPreferences.getString("iat_vadbos_preference", "4000"));
        // 设置语音后端点
        mIat.setParameter(SpeechConstant.VAD_EOS, mIatSharedPreferences.getString("iat_vadeos_preference", "1000"));
        // 设置标点符号
        mIat.setParameter(SpeechConstant.ASR_PTT, mIatSharedPreferences.getString("iat_punc_preference", "1"));
        // 设置音频保存路径
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()
                + "/iflytek/wavaudio.pcm");
        // 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
        // 注：该参数暂时只对在线听写有效
        mIat.setParameter(SpeechConstant.ASR_DWA, mIatSharedPreferences.getString("iat_dwa_preference", "0"));
    }

    private static InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip(mApp.getString(R.string.fail) + code);
            }
        }
    };

    private static RecognizerListener recognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语音+）需要提示用户开启语音+的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);

            if (isLast) {
                StringBuilder resultBuffer = new StringBuilder();
                for (String key : mIatResults.keySet()) {
                    resultBuffer.append(mIatResults.get(key));
                }

                String result = resultBuffer.toString();
                MainActivity.printResult(result);
            }
        }

        @Override
        public void onVolumeChanged(int volume) {
            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };
    // ----------------Iat end----------------

    // ----------------TTS start----------------
    public static boolean startSpeaking(String str){
        setTtsParam(voice);
        int code = mTts.startSpeaking(str, mTtsListener);
        if(code != ErrorCode.SUCCESS){
            showTip(mApp.getString(R.string.fail) + code);
            return false;
        } else {
            return true;
        }
    }

    private static void setTtsParam(String voice){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voice);
            //设置语速
            mTts.setParameter(SpeechConstant.SPEED,mTtsSharedPreferences.getString("speed_preference", "50"));
            //设置音调
            mTts.setParameter(SpeechConstant.PITCH,mTtsSharedPreferences.getString("pitch_preference", "50"));
            //设置音量
            mTts.setParameter(SpeechConstant.VOLUME,mTtsSharedPreferences.getString("volume_preference", "50"));
            //设置播放器音频流类型
            mTts.setParameter(SpeechConstant.STREAM_TYPE,mTtsSharedPreferences.getString("stream_preference", "3"));
        }
    }

    private static InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip(mApp.getString(R.string.fail)+code);
            }
        }
    };

    private static SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            //showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            //showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            //showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    public static boolean isSpeaking(){
        return mTts.isSpeaking();
    }
    // ----------------TTS end----------------

    private static void showTip(String str){
        MainActivity.showTip(str);
    }
}

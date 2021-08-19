package com.example.voice;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

@SuppressLint("SetTextI18n")
public class SpeechRecognizeActivity extends AppCompatActivity implements RecognitionListener {
    private final static String TAG = "SpeechRecognizeActivity";
    private TextView tv_result; // 声明一个文本视图对象
    private Button btn_recognize; // 声明一个按钮对象
    private SpeechRecognizer mRecognizer; // 声明一个语音识别对象
    private boolean isRecognizing = false; // 是否正在识别

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognize);
        tv_result = findViewById(R.id.tv_result);
        btn_recognize = findViewById(R.id.btn_recognize);
        // 检查系统是否支持原生的语音识别
        boolean enable = SpeechRecognizer.isRecognitionAvailable(this);
        if (enable) {
            initRecognize(); // 初始化语音识别
        } else {
            tv_result.setText("原生的语音识别服务不可用");
            btn_recognize.setEnabled(false);
            btn_recognize.setTextColor(Color.GRAY);
        }
    }

    // 初始化语音识别
    private void initRecognize() {
        String serviceComponent = Settings.Secure.getString(
                getContentResolver(), "voice_recognition_service");
        // 获得系统内置的语音识别服务
        ComponentName component = ComponentName.unflattenFromString(serviceComponent);
        Log.d(TAG, "getPackageName="+component.getPackageName()+",getClassName="+component.getClassName());
        tv_result.setText("原生的语音识别服务采用"+component.getPackageName()
                +"里的服务"+component.getClassName());
        // 创建原生的语音识别器对象
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(this); // 设置语音识别监听器
        btn_recognize.setOnClickListener(v -> {
            if (!isRecognizing) { // 未在识别
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                mRecognizer.startListening(intent); // 开始语音识别
            } else { // 正在识别
                mRecognizer.stopListening(); // 停止语音识别
                //mRecognizer.cancel(); // 取消语音识别
            }
            isRecognizing = !isRecognizing;
            btn_recognize.setText(isRecognizing?"停止识别":"开始识别");
        });
    }

    @Override
    public void onReadyForSpeech(Bundle params) {}

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onRmsChanged(float rmsdB) {}

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(int error) {
        Log.d(TAG, "Recognize error:"+error);
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "onResults Start");
        String desc = "";
        String key = SpeechRecognizer.RESULTS_RECOGNITION;
        try { // 百度语音分支
            String result = results.getString(key);
            Log.d(TAG, "result="+result);
            JSONObject object = new JSONObject(result);
            String recognizeResult = object.getString("recognizeResult");
            JSONArray recognizeArray = new JSONArray(recognizeResult);
            for (int i=0; i<recognizeArray.length(); i++) {
                JSONObject item = (JSONObject) recognizeArray.get(i);
                String se_query = item.getString("se_query");
                desc = desc + "\n" + se_query;
                Log.d(TAG, "desc="+desc);
            }
        } catch (Exception e) { // 讯飞语音分支
            e.printStackTrace();
            List<String> resultList = results.getStringArrayList(key);
            for (String str : resultList) {
                desc = desc + "\n" + str;
                Log.d(TAG, "desc="+desc);
            }
        }
        tv_result.setText("识别到的文字为："+desc);
        Log.d(TAG, "onResults End");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecognizer != null) {
            mRecognizer.destroy(); // 销毁语音识别器
        }
    }

}
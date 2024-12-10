package com.nanyang.richeditor.util;

import static java.util.Locale.getAvailableLocales;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nanyang.richeditor.R;

import java.util.ArrayList;
import java.util.Locale;


public class VoiceRecognition {
    private final LayoutInflater mLayoutInflater;
    private AudioManager mAudioManager;
    private final WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View rootView; // 最外层view
    private TextView returnedText;
    private ToggleButton toggleButton;
    private SpeechRecognizer speechRecognizer = null;
    private Intent recognizerIntent;
    private final String TAG = "VoiceRecognition";
    private String speechString = "";
    private boolean speechStarted = false;
    private boolean mIsMinimize = true;
    private ScrollView scrollView;
    private final Context mContext;
    private static volatile VoiceRecognition instance = null;
    private Locale language = Locale.US;

    private VoiceRecognition(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);// 获取系统的Window管理者
        mLayoutParams = new WindowManager.LayoutParams();
        initView(context);
    }

    public static VoiceRecognition getInstance(Context context) {
        if (instance == null) {
            synchronized (VoiceRecognition.class) {
                if (instance == null) {
                    instance = new VoiceRecognition(context);
                }
            }
        }
        return instance;
    }


    private void initView(Context context) {
        //  voiceRecognition = this;
        rootView = mLayoutInflater.inflate(R.layout.voice_recognition, null);
        rootView.setOnTouchListener(mOnTouchListener);
        rootView.setBackgroundColor(Color.WHITE);
        //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = dip2px(mContext, 400);
        mWindowManager.addView(rootView, mLayoutParams);
        //   narrowScreen();
        returnedText = rootView.findViewById(R.id.speech_text);
        toggleButton = rootView.findViewById(R.id.speech_toggle);
        scrollView = rootView.findViewById(R.id.scroll_speech);

        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecognize();
                } else {
                    if (speechRecognizer != null) {
                        speechRecognizer.stopListening();
                        speechRecognizer.destroy();
                    }
                }
            }
        });
        ((ToggleButton) rootView.findViewById(R.id.language_toggle)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(TAG, "all local" + getAvailableLocales().toString());

                if (isChecked) {
                    language = Locale.US;
                } else {
                    language = Locale.getDefault();
                }
                toggleButton.setChecked(true);
            }
        });

    }

    public void start() {
        toggleButton.setChecked(true);
    }

    public void stop() {
        toggleButton.setChecked(false);
    }


    public void destroy() {
        toggleButton.setChecked(false);
        if (rootView != null) mWindowManager.removeView(rootView);
    }


    private void startRecognize() {

        //  returnedText.setText("");
        if (speechRecognizer != null) speechRecognizer.destroy();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        speechRecognizer.setRecognitionListener(recognitionListener);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        // Specify language model
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Specify how many results to receive
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.startListening(recognizerIntent);
    }


    RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {
            speechStarted = true;
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // progressBar.setProgress((int) rmsdB);
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            speechStarted = false;
            // Log.e(TAG, "onEndOfSpeech");
            speechRecognizer.startListening(recognizerIntent);
        }

        @Override
        public void onError(int error) {
            // Log.e(TAG, "onError-----" + error);
            // if (!speechStarted)
            speechRecognizer.startListening(recognizerIntent);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            speechString = speechString + ".\n" + matches.get(0);
            returnedText.setText(speechString);
            scrollView.fullScroll(View.FOCUS_DOWN);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            returnedText.setText(speechString + ".\n" + matches.get(0));
            scrollView.fullScroll(View.FOCUS_DOWN);
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    // 缩小屏幕操作
    @SuppressLint("UseCompatLoadingForDrawables")
    public void narrowScreen() {
        if (rootView == null) {
            return;
        }
        mIsMinimize = true;
        mLayoutParams.x = dip2px(mContext, 500) / 2;
        mLayoutParams.y = dip2px(mContext, 320) / 2;
        mLayoutParams.width = dip2px(mContext, 500);
        mLayoutParams.height = dip2px(mContext, 320);
        mWindowManager.updateViewLayout(rootView, mLayoutParams);
        //updateSurfaceFrame();
    }

    // 放大屏幕操作
    @SuppressLint("UseCompatLoadingForDrawables")
    public void enlargeScreen() {
        if (rootView == null) {
            // Log.w(TAG, CommonConstant.EXCEPTION_MESSAGE_03);
            return;
        }
        mLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        mLayoutParams.gravity = Gravity.CENTER;
        mWindowManager.updateViewLayout(rootView, mLayoutParams);
        mIsMinimize = false;
        // updateSurfaceFrame();
    }

    // -------------------------- 本地私有方法 finish -----------------------------------
    private boolean isResizing = false;
    // [popup] initial coordinates and distance between fingers
    private double initPointerDistance = -1;
    private float initFirstPointerX = -1;
    private float initFirstPointerY = -1;
    private float initSecPointerX = -1;
    private float initSecPointerY = -1;
    // 触摸事件监听
    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        private int downX;
        private int downY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {

            if (event.getPointerCount() == 2 && !isResizing) {
                //record coordinates of fingers
                initFirstPointerX = event.getX(0);
                initFirstPointerY = event.getY(0);
                initSecPointerX = event.getX(1);
                initSecPointerY = event.getY(1);
                //record distance between fingers
                initPointerDistance = Math.hypot(initFirstPointerX - initSecPointerX,
                        initFirstPointerY - initSecPointerY);

                isResizing = true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE && isResizing) {
                return handleMultiDrag(event);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = (int) event.getRawX();
                    downY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - downX;
                    int movedY = nowY - downY;
                    downX = nowX;
                    downY = nowY;
                    mLayoutParams.x = mLayoutParams.x + movedX;
                    mLayoutParams.y = mLayoutParams.y + movedY;
                    mWindowManager.updateViewLayout(view, mLayoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    // 启动计时器
                    //mCountDownTimer.start();
                    savePositionAndSize();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void savePositionAndSize() {

    }

    private boolean handleMultiDrag(final MotionEvent event) {
        if (initPointerDistance != -1 && event.getPointerCount() == 2) {
            // get the movements of the fingers
            final double firstPointerMove = Math.hypot(event.getX(0) - initFirstPointerX,
                    event.getY(0) - initFirstPointerY);
            final double secPointerMove = Math.hypot(event.getX(1) - initSecPointerX,
                    event.getY(1) - initSecPointerY);

            // minimum threshold beyond which pinch gesture will work
            //  final int minimumMove = ViewConfiguration.get(service).getScaledTouchSlop();
            final int minimumMove = 20;
            if (Math.max(firstPointerMove, secPointerMove) > minimumMove) {
                // calculate current distance between the pointers
                final double currentPointerDistance =
                        Math.hypot(event.getX(0) - event.getX(1),
                                event.getY(0) - event.getY(1));

                //   final double popupWidth = playerImpl.getPopupWidth();
                // change co-ordinates of popup so the center stays at the same position
                //   final double newWidth = (popupWidth * currentPointerDistance / initPointerDistance);
                initPointerDistance = currentPointerDistance;
                //   playerImpl.getPopupLayoutParams().x += (popupWidth - newWidth) / 2;

                //   playerImpl.checkPopupPositionBounds();
                // playerImpl.updateScreenSize();
//
                //   playerImpl.updatePopupSize((int) Math.min(playerImpl.getScreenWidth(), newWidth),
                //     -1);
                return true;
            }
        }
        return false;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}

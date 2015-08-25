package com.easemob.easeui.widget;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseVoicePlayClickListener;
import com.easemob.easeui.utils.EaseSmileUtils;


public class EaseChatPrimaryMenu extends RelativeLayout implements OnClickListener {
    private EditText editText;
    private View buttonSetModeKeyboard;
    private RelativeLayout edittext_layout;
    private View buttonSetModeVoice;
    private View buttonSend;
    private View buttonPressToSpeak;
    private ImageView faceNormal;
    private ImageView faceChecked;
    private Button buttonMore;
    private ChatPrimaryMenuListener listener;
    private Activity activity;
    private InputMethodManager inputManager;
    private RelativeLayout faceLayout;
    private Context context;
    private EaseVoiceRecorderView voiceRecorderView;

    public EaseChatPrimaryMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public EaseChatPrimaryMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatPrimaryMenu(Context context) {
        super(context);
        init(context, null);
    }

    private void init(final Context context, AttributeSet attrs) {
        this.context = context;
        this.activity = (Activity) context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_primary_menu, this);
        editText = (EditText) findViewById(R.id.et_sendmessage);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonSend = findViewById(R.id.btn_send);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        faceNormal = (ImageView) findViewById(R.id.iv_face_normal);
        faceChecked = (ImageView) findViewById(R.id.iv_face_checked);
        faceLayout = (RelativeLayout) findViewById(R.id.rl_face);
        buttonMore = (Button) findViewById(R.id.btn_more);
        edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_normal);
        
        inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        buttonSend.setOnClickListener(this);
        buttonSetModeKeyboard.setOnClickListener(this);
        buttonSetModeVoice.setOnClickListener(this);
        buttonMore.setOnClickListener(this);
        faceLayout.setOnClickListener(this);
        editText.setOnClickListener(this);
        editText.requestFocus();
        
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_active);
                } else {
                    edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_normal);
                }

            }
        });
        // 监听文字框
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    buttonMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                } else {
                    buttonMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        
        
        buttonPressToSpeak.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        if (EaseVoicePlayClickListener.isPlaying)
                            EaseVoicePlayClickListener.currentPlayListener.stopPlayVoice();
                        v.setPressed(true);
                        voiceRecorderView.startRecording();
                    } catch (Exception e) {
                        v.setPressed(false);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        voiceRecorderView.showReleaseToCancelHint();
                    } else {
                        voiceRecorderView.showMoveUpToCancelHint();
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    if (event.getY() < 0) {
                        // discard the recorded audio.
                        voiceRecorderView.discardRecording();
                    } else {
                        // stop recording and send voice file
                        try {
                            int length = voiceRecorderView.stopRecoding();
                            if (length > 0) {
                                if(listener != null)
                                    listener.onVoiceRecorded(voiceRecorderView.getVoiceFilePath(), voiceRecorderView.getVoiceFileName(),
                                        length);
                            } else if (length == EMError.INVALID_FILE) {
                                Toast.makeText(context, R.string.Recording_without_permission, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, R.string.The_recording_time_is_too_short, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, R.string.send_failure_please, Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                default:
                    if (voiceRecorderView != null) {
                        voiceRecorderView.discardRecording();
                    }
                    return false;
                }
            }
        });
    }
    
    /**
     * 设置长按说话录制控件
     * @param voiceRecorderView
     */
    public void setPressToSpeakRecorderView(EaseVoiceRecorderView voiceRecorderView){
        this.voiceRecorderView = voiceRecorderView;
    }

    /**
     * 表情输入
     * @param name
     */
    public void onEmojiconInputEvent(String name){
        try {
            // 这里用的反射，所以混淆的时候不要混淆SmileUtils这个类
            Class clz = Class.forName("com.easemob.easeui.utils.EaseSmileUtils");
            Field field = clz.getField(name);
            editText.append(EaseSmileUtils.getSmiledText(context,(String) field.get(null)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 表情删除
     */
    public void onEmojiconDeleteEvent(){
        if (!TextUtils.isEmpty(editText.getText())) {

            int selectionStart = editText.getSelectionStart();// 获取光标的位置
            if (selectionStart > 0) {
                String body = editText.getText().toString();
                String tempStr = body.substring(0, selectionStart);
                int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
                if (i != -1) {
                    CharSequence cs = tempStr.substring(i, selectionStart);
                    if (EaseSmileUtils.containsKey(cs.toString()))
                        editText.getEditableText().delete(i, selectionStart);
                    else
                        editText.getEditableText().delete(selectionStart - 1,
                                selectionStart);
                } else {
                    editText.getEditableText().delete(selectionStart - 1, selectionStart);
                }
            }
        }
    }
    
    /**
     * 点击事件
     * @param view
     */
    @Override
    public void onClick(View view){
        int id = view.getId();
        if (id == R.id.btn_send) {
            if(listener != null){
                String s = editText.getText().toString();
                editText.setText("");
                listener.onSendBtnClicked(s);
            }
        } else if (id == R.id.btn_set_mode_voice) {
            setModeVoice();
            showNormalFaceImage();
            if(listener != null)
                listener.onToggleVoiceBtnClicked();
        } else if (id == R.id.btn_set_mode_keyboard) {
            setModeKeyboard();
            showNormalFaceImage();
            if(listener != null)
                listener.onToggleVoiceBtnClicked();
        } else if (id == R.id.btn_more) {
            buttonSetModeVoice.setVisibility(View.VISIBLE);
            buttonSetModeKeyboard.setVisibility(View.GONE);
            edittext_layout.setVisibility(View.VISIBLE);
            buttonPressToSpeak.setVisibility(View.GONE);
            showNormalFaceImage();
            if(listener != null)
                listener.onToggleExtendClicked();
        } else if (id == R.id.et_sendmessage) {
            edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_active);
            faceNormal.setVisibility(View.VISIBLE);
            faceChecked.setVisibility(View.INVISIBLE);
            if(listener != null)
                listener.onEditTextClicked();
        } else if (id == R.id.rl_face) {
            toggleFaceImage();
            if(listener != null){
                listener.onToggleEmojiconClicked();
            }
        } else {
        }
    }
    
    
    /**
     * 显示语音图标按钮
     * 
     */
    protected void setModeVoice() {
        hideKeyboard();
        edittext_layout.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        buttonMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        faceNormal.setVisibility(View.VISIBLE);
        faceChecked.setVisibility(View.INVISIBLE);

    }

    /**
     * 显示键盘图标
     */
    protected void setModeKeyboard() {
        edittext_layout.setVisibility(View.VISIBLE);
        buttonSetModeKeyboard.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        // mEditTextContent.setVisibility(View.VISIBLE);
        editText.requestFocus();
        // buttonSend.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(editText.getText())) {
            buttonMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        } else {
            buttonMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        }

    }
    
    
    protected void toggleFaceImage(){
        if(faceNormal.getVisibility() == View.VISIBLE){
            showSelectedFaceImage();
        }else{
            showNormalFaceImage();
        }
    }
    
    public void showNormalFaceImage(){
        faceNormal.setVisibility(View.VISIBLE);
        faceChecked.setVisibility(View.INVISIBLE);
    }
    
    public void showSelectedFaceImage(){
        faceNormal.setVisibility(View.INVISIBLE);
        faceChecked.setVisibility(View.VISIBLE);
    }
    
    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (activity.getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    
    /**
     * 设置主按钮栏相关listener
     * @param listener
     */
    public void setChatPrimaryMenuListener(ChatPrimaryMenuListener listener){
        this.listener = listener;
    }
    
    public interface ChatPrimaryMenuListener{
        /**
         * 发送按钮点击事件
         * @param content 发送内容
         */
        void onSendBtnClicked(String content);
        
        /**
         * 语音录制成功
         * @param filePath
         * @param fileName
         * @param length
         */
        void onVoiceRecorded(String filePath, String fileName, int length);
        
        /**
         * 长按说话按钮隐藏或显示事件
         */
        void onToggleVoiceBtnClicked();
        
        /**
         * 隐藏或显示扩展menu按钮点击点击事件
         */
        void onToggleExtendClicked();
        
        /**
         * 隐藏或显示emojicon按钮点击事件
         */
        void onToggleEmojiconClicked();
        
        /**
         * 文字输入框点击事件
         */
        void onEditTextClicked();
    }


}

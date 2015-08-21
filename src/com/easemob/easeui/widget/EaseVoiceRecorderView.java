package com.easemob.easeui.widget;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.easeui.R;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;

/**
 * 按住说话录制控件
 *
 */
public class EaseVoiceRecorderView extends RelativeLayout  {
    protected Context context;
    protected LayoutInflater inflater;
    protected Drawable[] micImages;
    protected VoiceRecorder voiceRecorder;
    
    protected PowerManager.WakeLock wakeLock;
    protected ImageView micImage;
    protected TextView recordingHint;
    
    protected Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 切换msg切换图片
            micImage.setImageDrawable(micImages[msg.what]);
        }
    };
    
    public EaseVoiceRecorderView(Context context) {
        super(context);
        init(context);
    }

    public EaseVoiceRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public EaseVoiceRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    
    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_voice_recorder, this);
        
        
        micImage = (ImageView) findViewById(R.id.mic_image);
        recordingHint = (TextView) findViewById(R.id.recording_hint);
        
        voiceRecorder = new VoiceRecorder(micImageHandler);
        
        // 动画资源文件,用于录制语音时
        micImages = new Drawable[] {
                getResources().getDrawable(R.drawable.ease_record_animate_01),
                getResources().getDrawable(R.drawable.ease_record_animate_02),
                getResources().getDrawable(R.drawable.ease_record_animate_03),
                getResources().getDrawable(R.drawable.ease_record_animate_04),
                getResources().getDrawable(R.drawable.ease_record_animate_05),
                getResources().getDrawable(R.drawable.ease_record_animate_06),
                getResources().getDrawable(R.drawable.ease_record_animate_07),
                getResources().getDrawable(R.drawable.ease_record_animate_08),
                getResources().getDrawable(R.drawable.ease_record_animate_09),
                getResources().getDrawable(R.drawable.ease_record_animate_10),
                getResources().getDrawable(R.drawable.ease_record_animate_11),
                getResources().getDrawable(R.drawable.ease_record_animate_12),
                getResources().getDrawable(R.drawable.ease_record_animate_13),
                getResources().getDrawable(R.drawable.ease_record_animate_14), };
        
        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
    }
    
    public void startRecording() throws Exception {
        if (!EaseCommonUtils.isExitsSdcard()) {
            Toast.makeText(context, R.string.Send_voice_need_sdcard_support, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            wakeLock.acquire();
            this.setVisibility(View.VISIBLE);
            recordingHint.setText(context.getString(R.string.move_up_to_cancel));
            recordingHint.setBackgroundColor(Color.TRANSPARENT);
            voiceRecorder.startRecording(context);
        } catch (Exception e) {
            e.printStackTrace();
            if (wakeLock.isHeld())
                wakeLock.release();
            if (voiceRecorder != null)
                voiceRecorder.discardRecording();
            this.setVisibility(View.INVISIBLE);
            Toast.makeText(context, R.string.recoding_fail, Toast.LENGTH_SHORT).show();
            return;
        }
    }
    
    public void showReleaseToCancelHint() {
        recordingHint.setText(context.getString(R.string.release_to_cancel));
        recordingHint.setBackgroundResource(R.drawable.ease_recording_text_hint_bg);
    }
    
    public void showMoveUpToCancelHint() {
        recordingHint.setText(context.getString(R.string.move_up_to_cancel));
        recordingHint.setBackgroundColor(Color.TRANSPARENT);
    }
    
    public void discardRecording() {
        if (wakeLock.isHeld())
            wakeLock.release();
        try {
            // 停止录音
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                this.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
        }
    }
    
    public int stopRecoding() {
        this.setVisibility(View.INVISIBLE);
        if (wakeLock.isHeld())
            wakeLock.release();
        return voiceRecorder.stopRecoding();
    }
    
    public String getVoiceFilePath() {
        return voiceRecorder.voiceFilePath;
    }
    
    public String getVoiceFileName() {
        return voiceRecorder.voiceFileName;
    }
    
    public boolean isRecording() {
        return voiceRecorder.isRecording();
    }
    
    public class VoiceRecorder {

        MediaRecorder recorder;

        static final String PREFIX = "voice";
        static final String EXTENSION = ".amr";

        private boolean isRecording = false;
        private long startTime;
        private String voiceFilePath = null;
        private String voiceFileName = null;
        private File file;
        private Handler handler;

        public VoiceRecorder(Handler handler) {
            this.handler = handler;
        }

        /**
         * start recording to the file
         */
        public String startRecording(Context appContext) {
            file = null;
            try {
                // need to create recorder every time, otherwise, will got exception
                // from setOutputFile when try to reuse
                if (recorder != null) {
                    recorder.release();
                    recorder = null;
                }
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setAudioChannels(1); // MONO
                recorder.setAudioSamplingRate(8000); // 8000Hz
                recorder.setAudioEncodingBitRate(64); // seems if change this to
                                                        // 128, still got same file
                                                        // size.
                // one easy way is to use temp file
                // file = File.createTempFile(PREFIX + userId, EXTENSION,
                // User.getVoicePath());
                voiceFileName = getVoiceFileName(EMChatManager.getInstance().getCurrentUser());
                voiceFilePath = getVoiceFilePath();
                file = new File(voiceFilePath);
                recorder.setOutputFile(file.getAbsolutePath());
                recorder.prepare();
                isRecording = true;
                recorder.start();
            } catch (IOException e) {
                EMLog.e("voice", "prepare() failed");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (isRecording) {
                            android.os.Message msg = new android.os.Message();
                            msg.what = recorder.getMaxAmplitude() * 13 / 0x7FFF;
                            handler.sendMessage(msg);
                            SystemClock.sleep(100);
                        }
                    } catch (Exception e) {
                        // from the crash report website, found one NPE crash from
                        // one android 4.0.4 htc phone
                        // maybe handler is null for some reason
                        EMLog.e("voice", e.toString());
                    }
                }
            }).start();
            startTime = new Date().getTime();
            EMLog.d("voice", "start voice recording to file:" + file.getAbsolutePath());
            return file == null ? null : file.getAbsolutePath();
        }

        /**
         * stop the recoding
         * 
         * @return seconds of the voice recorded
         */

        public void discardRecording() {
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                    if (file != null && file.exists() && !file.isDirectory()) {
                        file.delete();
                    }
                } catch (IllegalStateException e) {
                } catch (RuntimeException e){}
                isRecording = false;
            }
        }

        public int stopRecoding() {
            if(recorder != null){
                isRecording = false;
                recorder.stop();
                recorder.release();
                recorder = null;
                
                if(file == null || !file.exists() || !file.isFile()){
                    return EMError.INVALID_FILE;
                }
                if (file.length() == 0) {
                    file.delete();
                    return EMError.INVALID_FILE;
                }
                int seconds = (int) (new Date().getTime() - startTime) / 1000;
                EMLog.d("voice", "voice recording finished. seconds:" + seconds + " file length:" + file.length());
                return seconds;
            }
            return 0;
        }

        protected void finalize() throws Throwable {
            super.finalize();
            if (recorder != null) {
                recorder.release();
            }
        }

        String getVoiceFileName(String uid) {
            Time now = new Time();
            now.setToNow();
            return uid + now.toString().substring(0, 15) + EXTENSION;
        }

        public boolean isRecording() {
            return isRecording;
        }

        String getVoiceFilePath() {
            return PathUtil.getInstance().getVoicePath() + "/" + voiceFileName;
        }
    }


}

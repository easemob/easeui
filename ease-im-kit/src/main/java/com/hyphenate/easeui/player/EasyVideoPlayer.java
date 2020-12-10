package com.hyphenate.easeui.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.hyphenate.easeui.R;

import java.io.IOException;

/**
 * 简易视频播放器，参考：https://github.com/mmbadimunei/easyVideoPlayer
 * 可以实现播放进度控制，播放暂停与继续播放等功能
 */
public class EasyVideoPlayer extends FrameLayout
        implements EasyIUserMethods,
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnErrorListener,
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private static final int UPDATE_INTERVAL = 100;

    private TextureView mTextureView;
    private Surface mSurface;
    private View mControlsFrame;
    private View mClickFrame;
    private SeekBar mSeeker;
    private TextView mLabelPosition;
    private TextView mLabelDuration;
    private ImageButton mBtnPlayPause;
    private MediaPlayer mPlayer;

    private boolean mSurfaceAvailable;
    private boolean mIsPrepared;
    private boolean mWasPlaying;
    private int mInitialTextureWidth;
    private int mInitialTextureHeight;

    private Handler mHandler;

    private Uri mSource;
    private EasyVideoCallback mCallback;
    private EasyVideoProgressCallback mProgressCallback;
    private Drawable mPlayDrawable;
    private Drawable mPauseDrawable;
    private boolean mHideControlsOnPlay = true;
    private boolean mAutoPlay;
    private int mInitialPosition = -1;
    private boolean mControlsDisabled;
    private int mThemeColor = 0;
    private boolean mAutoFullscreen = false;
    private boolean mLoop = false;
    private int currentPos;

    // Runnable used to run code on an interval to update counters and seeker
    private final Runnable mUpdateCounters =
            new Runnable() {
                @Override
                public void run() {
                    if (mHandler == null || !mIsPrepared || mSeeker == null || mPlayer == null)
                        return;
                    int pos = mPlayer.getCurrentPosition();
                    final int dur = mPlayer.getDuration();
                    if ("oppo".equals(Build.BRAND.toLowerCase()) && "OPPO R9sk".equals(Build.MODEL) && pos <= EasyVideoPlayer.this.currentPos) {
                        pos = EasyVideoPlayer.this.currentPos;
                    }
                    if (pos > dur) pos = dur;
                    mLabelPosition.setText(Util.getDurationString(pos, false));
                    mLabelDuration.setText(Util.getDurationString(dur, false));
                    mSeeker.setProgress(pos);
                    mSeeker.setMax(dur);

                    if (mProgressCallback != null)
                        mProgressCallback.onVideoProgressUpdate(pos, dur);
                    if (mHandler != null) mHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            };

    public EasyVideoPlayer(Context context) {
        super(context);
        init(context, null);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setBackgroundColor(Color.BLACK);

        if (attrs != null) {
            TypedArray a =
                    context.getTheme().obtainStyledAttributes(attrs, R.styleable.EasyVideoPlayer, 0, 0);
            try {
                String source = a.getString(R.styleable.EasyVideoPlayer_easy_source);
                if (source != null && !source.trim().isEmpty()) mSource = Uri.parse(source);

                int playDrawableResId = a.getResourceId(R.styleable.EasyVideoPlayer_easy_playDrawable, -1);
                int pauseDrawableResId = a.getResourceId(R.styleable.EasyVideoPlayer_easy_pauseDrawable, -1);

                if (playDrawableResId != -1) {
                    mPlayDrawable = AppCompatResources.getDrawable(context, playDrawableResId);
                }
                if (pauseDrawableResId != -1) {
                    mPauseDrawable = AppCompatResources.getDrawable(context, pauseDrawableResId);
                }

                mHideControlsOnPlay =
                        a.getBoolean(R.styleable.EasyVideoPlayer_easy_hideControlsOnPlay, true);
                mAutoPlay = a.getBoolean(R.styleable.EasyVideoPlayer_easy_autoPlay, false);
                mControlsDisabled = a.getBoolean(R.styleable.EasyVideoPlayer_easy_disableControls, false);

                mThemeColor =
                        a.getColor(
                                R.styleable.EasyVideoPlayer_easy_themeColor,
                                Util.resolveColor(context, R.attr.colorPrimary));

                mAutoFullscreen = a.getBoolean(R.styleable.EasyVideoPlayer_easy_autoFullscreen, false);
                mLoop = a.getBoolean(R.styleable.EasyVideoPlayer_easy_loop, false);
            } finally {
                a.recycle();
            }
        } else {
            mHideControlsOnPlay = true;
            mAutoPlay = false;
            mControlsDisabled = false;
            mThemeColor = Util.resolveColor(context, R.attr.colorPrimary);
            mAutoFullscreen = false;
            mLoop = false;
        }

        if (mPlayDrawable == null)
            mPlayDrawable = AppCompatResources.getDrawable(context, R.drawable.easy_action_play);
        if (mPauseDrawable == null)
            mPauseDrawable = AppCompatResources.getDrawable(context, R.drawable.easy_action_pause);
    }

    @Override
    public void setSource(@NonNull Uri source) {
        boolean hadSource = mSource != null;
        if (hadSource) stop();
        mSource = source;
        if (mPlayer != null) {
            if (hadSource) {
                sourceChanged();
            } else {
                prepare();
            }
        }
    }

    @Override
    public void setCallback(@NonNull EasyVideoCallback callback) {
        mCallback = callback;
    }

    @Override
    public void setProgressCallback(@NonNull EasyVideoProgressCallback callback) {
        mProgressCallback = callback;
    }

    @Override
    public void setPlayDrawable(@NonNull Drawable drawable) {
        mPlayDrawable = drawable;
        if (!isPlaying()) mBtnPlayPause.setImageDrawable(drawable);
    }

    @Override
    public void setPlayDrawableRes(@DrawableRes int res) {
        setPlayDrawable(AppCompatResources.getDrawable(getContext(), res));
    }

    @Override
    public void setPauseDrawable(@NonNull Drawable drawable) {
        mPauseDrawable = drawable;
        if (isPlaying()) mBtnPlayPause.setImageDrawable(drawable);
    }

    @Override
    public void setPauseDrawableRes(@DrawableRes int res) {
        setPauseDrawable(AppCompatResources.getDrawable(getContext(), res));
    }

    @Override
    public void setThemeColor(@ColorInt int color) {
        mThemeColor = color;
        invalidateThemeColors();
    }

    @Override
    public void setThemeColorRes(@ColorRes int colorRes) {
        setThemeColor(ContextCompat.getColor(getContext(), colorRes));
    }

    @Override
    public void setHideControlsOnPlay(boolean hide) {
        mHideControlsOnPlay = hide;
    }

    @Override
    public void setAutoPlay(boolean autoPlay) {
        mAutoPlay = autoPlay;
    }

    @Override
    public void setInitialPosition(@IntRange(from = 0, to = Integer.MAX_VALUE) int pos) {
        mInitialPosition = pos;
    }

    private void sourceChanged() {
        setControlsEnabled(false);
        mSeeker.setProgress(0);
        mSeeker.setEnabled(false);
        mPlayer.reset();
        if (mCallback != null) mCallback.onPreparing(this);
        try {
            setSourceInternal();
        } catch (IOException e) {
            throwError(e);
        }
    }

    private void setSourceInternal() throws IOException {
        if (mSource.getScheme() != null
                && (mSource.getScheme().equals("http") || mSource.getScheme().equals("https"))) {
            LOG("Loading web URI: " + mSource.toString());
            mPlayer.setDataSource(mSource.toString());
        } else if (mSource.getScheme() != null
                && (mSource.getScheme().equals("file") && mSource.getPath().contains("/android_assets/"))) {
            LOG("Loading assets URI: " + mSource.toString());
            AssetFileDescriptor afd;
            afd =
                    getContext()
                            .getAssets()
                            .openFd(mSource.toString().replace("file:///android_assets/", ""));
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } else if (mSource.getScheme() != null && mSource.getScheme().equals("asset")) {
            LOG("Loading assets URI: " + mSource.toString());
            AssetFileDescriptor afd;
            afd = getContext().getAssets().openFd(mSource.toString().replace("asset://", ""));
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } else {
            LOG("Loading local URI: " + mSource.toString());
            mPlayer.setDataSource(getContext(), mSource);
        }
        mPlayer.prepareAsync();
    }

    private void prepare() {
        if (!mSurfaceAvailable || mSource == null || mPlayer == null || mIsPrepared) return;
        if (mCallback != null) mCallback.onPreparing(this);
        try {
            mPlayer.setSurface(mSurface);
            setSourceInternal();
        } catch (IOException e) {
            throwError(e);
        }
    }

    private void setControlsEnabled(boolean enabled) {
        if (mSeeker == null) return;
        mSeeker.setEnabled(enabled);
        mBtnPlayPause.setEnabled(enabled);

        final float disabledAlpha = .4f;
        mBtnPlayPause.setAlpha(enabled ? 1f : disabledAlpha);

        mClickFrame.setEnabled(enabled);
    }

    @Override
    public void showControls() {
        if (mControlsDisabled || isControlsShown() || mSeeker == null) return;

        mControlsFrame.animate().cancel();
        mControlsFrame.setAlpha(0f);
        mControlsFrame.setVisibility(View.VISIBLE);
        mBtnPlayPause.setVisibility(VISIBLE);
        mControlsFrame
                .animate()
                .alpha(1f)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (mAutoFullscreen) {
                                    setFullscreen(false);
                                }
                            }
                        })
                .start();
    }

    @Override
    public void hideControls() {
        if (mControlsDisabled || !isControlsShown() || mSeeker == null) return;
        mControlsFrame.animate().cancel();
        mControlsFrame.setAlpha(1f);
        mControlsFrame.setVisibility(View.VISIBLE);
        mControlsFrame
                .animate()
                .alpha(0f)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                setFullscreen(true);
                                if (mControlsFrame != null) {
                                    mControlsFrame.setVisibility(View.INVISIBLE);
                                    mBtnPlayPause.setVisibility(INVISIBLE);
                                }

                            }
                        })
                .start();
    }

    @CheckResult
    @Override
    public boolean isControlsShown() {
        return !mControlsDisabled && mControlsFrame != null && mControlsFrame.getAlpha() > .5f;
    }

    @Override
    public void toggleControls() {
        if (mControlsDisabled) return;
        if (isControlsShown()) {
            hideControls();
        } else {
            showControls();
        }
    }

    @Override
    public void enableControls(boolean andShow) {
        mControlsDisabled = false;
        if (andShow) showControls();
        mClickFrame.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleControls();
                    }
                });
        mClickFrame.setClickable(true);
    }

    @Override
    public void disableControls() {
        mControlsDisabled = true;
        mControlsFrame.setVisibility(View.GONE);
        mClickFrame.setOnClickListener(null);
        mClickFrame.setClickable(false);
    }

    @CheckResult
    @Override
    public boolean isPrepared() {
        return mPlayer != null && mIsPrepared;
    }

    @CheckResult
    @Override
    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    @CheckResult
    @Override
    public int getCurrentPosition() {
        if (mPlayer == null) return -1;
        return mPlayer.getCurrentPosition();
    }

    @CheckResult
    @Override
    public int getDuration() {
        if (mPlayer == null) return -1;
        return mPlayer.getDuration();
    }

    @Override
    public void start() {
        if (mPlayer == null) return;
        mPlayer.start();
        if (mCallback != null) mCallback.onStarted(this);
        if (mHandler == null) mHandler = new Handler();
        mHandler.post(mUpdateCounters);
        mBtnPlayPause.setImageDrawable(mPauseDrawable);
    }

    @Override
    public void seekTo(@IntRange(from = 0, to = Integer.MAX_VALUE) int pos) {
        if (mPlayer == null) return;
        mPlayer.seekTo(pos);
    }

    @Override
    public void setVolume(
            @FloatRange(from = 0f, to = 1f) float leftVolume,
            @FloatRange(from = 0f, to = 1f) float rightVolume) {
        if (mPlayer == null || !mIsPrepared)
            throw new IllegalStateException(
                    "You cannot use setVolume(float, float) until the player is prepared.");
        mPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public void pause() {
        if (mPlayer == null || !isPlaying()) return;
        mPlayer.pause();
        if (mCallback != null) mCallback.onPaused(this);
        if (mHandler == null) return;
        mHandler.removeCallbacks(mUpdateCounters);
        mBtnPlayPause.setImageDrawable(mPlayDrawable);
    }

    @Override
    public void stop() {
        if (mPlayer == null) return;
        try {
            mPlayer.stop();
        } catch (Throwable ignored) {
        }
        if (mHandler == null) return;
        mHandler.removeCallbacks(mUpdateCounters);
        mBtnPlayPause.setImageDrawable(mPlayDrawable);
    }

    @Override
    public void reset() {
        if (mPlayer == null) return;
        mIsPrepared = false;
        mPlayer.reset();
        mIsPrepared = false;
    }

    @Override
    public void release() {
        mIsPrepared = false;
        if (mPlayer != null) {
            try {
                mPlayer.release();
            } catch (Throwable ignored) {
            }
            mPlayer = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCounters);
            mHandler = null;
        }
        LOG("Released player and Handler");
    }

    @Override
    public void setAutoFullscreen(boolean autoFullscreen) {
        this.mAutoFullscreen = autoFullscreen;
    }

    @Override
    public void setLoop(boolean loop) {
        mLoop = loop;
        if (mPlayer != null) mPlayer.setLooping(loop);
    }

    // Surface listeners

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        LOG("Surface texture available: %dx%d", width, height);
        mInitialTextureWidth = width;
        mInitialTextureHeight = height;
        mSurfaceAvailable = true;
        mSurface = new Surface(surfaceTexture);
        if (mIsPrepared) {
            mPlayer.setSurface(mSurface);
            if ("oppo".equals(Build.BRAND.toLowerCase()) && "OPPO R9sk".equals(Build.MODEL)) {
                this.mPlayer.seekTo(this.getCurrentPosition());
            }
        } else {
            prepare();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        LOG("Surface texture changed: %dx%d", width, height);
        adjustAspectRatio(width, height, mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        LOG("Surface texture destroyed");
        if ("oppo".equals(Build.BRAND.toLowerCase()) && "OPPO R9sk".equals(Build.MODEL)) {
            this.currentPos = this.getCurrentPosition();
        }
        mSurfaceAvailable = false;
        mSurface = null;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        LOG("onPrepared()");
        mIsPrepared = true;
        if (mCallback != null) mCallback.onPrepared(this);
        mLabelPosition.setText(Util.getDurationString(0, false));
        mLabelDuration.setText(Util.getDurationString(mediaPlayer.getDuration(), false));
        mSeeker.setProgress(0);
        mSeeker.setMax(mediaPlayer.getDuration());
        setControlsEnabled(true);

        if (mAutoPlay) {
            if (!mControlsDisabled && mHideControlsOnPlay) hideControls();
            start();
            if (mInitialPosition > 0) {
                seekTo(mInitialPosition);
                mInitialPosition = -1;
            }
        } else {
            // Hack to show first frame, is there another way?
            mPlayer.start();
            mPlayer.pause();
            mBtnPlayPause.setImageDrawable(mPlayDrawable);
            mBtnPlayPause.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        LOG("Buffering: %d%%", percent);
        if (mCallback != null) mCallback.onBuffering(percent);
        if (mSeeker != null) {
            if (percent == 100) mSeeker.setSecondaryProgress(0);
            else mSeeker.setSecondaryProgress(mSeeker.getMax() * (percent / 100));
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LOG("onCompletion()");
        if ("oppo".equals(Build.BRAND.toLowerCase()) && "OPPO R9sk".equals(Build.MODEL)) {
            this.currentPos = 0;
        }
        if (mLoop) {
            mBtnPlayPause.setImageDrawable(mPlayDrawable);
            if (mHandler != null) mHandler.removeCallbacks(mUpdateCounters);
            mSeeker.setProgress(mSeeker.getMax());
            showControls();
        } else {
            seekTo(0);
            mSeeker.setProgress(0);
            mLabelPosition.setText(Util.getDurationString(0, false));
            mBtnPlayPause.setImageDrawable(mPlayDrawable);
            showControls();
        }
        if (mCallback != null) {
            mCallback.onCompletion(this);
            if (mLoop) mCallback.onStarted(this);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        LOG("Video size changed: %dx%d", width, height);
        adjustAspectRatio(mInitialTextureWidth, mInitialTextureHeight, width, height);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        if (what == -38) {
            // Error code -38 happens on some Samsung devices
            // Just ignore it
            return false;
        }
        String errorMsg = "Preparation/playback error (" + what + "): ";
        switch (what) {
            default:
                errorMsg += "Unknown error";
                break;
            case MediaPlayer.MEDIA_ERROR_IO:
                errorMsg += "I/O error";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                errorMsg += "Malformed";
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                errorMsg += "Not valid for progressive playback";
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                errorMsg += "Server died";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                errorMsg += "Timed out";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                errorMsg += "Unsupported";
                break;
        }
        throwError(new Exception(errorMsg));
        return false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isInEditMode()) {
            return;
        }
        setKeepScreenOn(true);
        mHandler = new Handler();
        mPlayer = new MediaPlayer();

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnVideoSizeChangedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setLooping(mLoop);

        initTextureView();
        initClickFrame();
        initPlayButton();
        initControlsFrame();

        if (mControlsDisabled) {
            mClickFrame.setOnClickListener(null);
            mControlsFrame.setVisibility(View.GONE);
        } else {
            mClickFrame.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            toggleControls();
                            if (mCallback != null) {
                                mCallback.onClickVideoFrame(EasyVideoPlayer.this);
                            }
                        }
                    });
        }

        invalidateThemeColors();

        setControlsEnabled(false);
        prepare();
    }

    private void initTextureView() {
        // Instantiate and add TextureView for rendering
        final LayoutParams textureLp =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTextureView = new TextureView(getContext());
        addView(mTextureView, textureLp);
        mTextureView.setSurfaceTextureListener(this);
    }

    private void initPlayButton() {
        mBtnPlayPause = new ImageButton(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mBtnPlayPause.setImageDrawable(mPauseDrawable);
        mBtnPlayPause.setOnClickListener(this);
        addView(mBtnPlayPause, params);
        mBtnPlayPause.setId(R.id.btnPlayPause);
        mBtnPlayPause.setVisibility(VISIBLE);
    }

    private void initControlsFrame() {
        final LayoutInflater li = LayoutInflater.from(getContext());
        // Inflate controls
        mControlsFrame = li.inflate(R.layout.easy_include_controls, this, false);
        final LayoutParams controlsLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        controlsLp.gravity = Gravity.BOTTOM;
        addView(mControlsFrame, controlsLp);

        // Retrieve controls
        mSeeker = (SeekBar) mControlsFrame.findViewById(R.id.seeker);
        mSeeker.setOnSeekBarChangeListener(this);

        mLabelPosition = (TextView) mControlsFrame.findViewById(R.id.position);
        mLabelPosition.setText(Util.getDurationString(0, false));

        mLabelDuration = (TextView) mControlsFrame.findViewById(R.id.duration);
        mLabelDuration.setText(Util.getDurationString(0, false));
    }

    private void initClickFrame() {
        // Instantiate and add click frame (used to toggle controls)
        mClickFrame = new FrameLayout(getContext());
        addView(mClickFrame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnPlayPause) {
            if (mPlayer.isPlaying()) {
                pause();
            } else {
                if (mHideControlsOnPlay && !mControlsDisabled) {
                    hideControls();
                }
                start();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) seekTo(value);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mWasPlaying = isPlaying();
        if (mWasPlaying) mPlayer.pause(); // keeps the time updater running, unlike pause()
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mWasPlaying) mPlayer.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LOG("Detached from window");
        release();

        mSeeker = null;
        mLabelPosition = null;
        mLabelDuration = null;
        mBtnPlayPause = null;

        mControlsFrame = null;
        mClickFrame = null;

        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCounters);
            mHandler = null;
        }
    }

    private static void LOG(String message, Object... args) {
        try {
            if (args != null) message = String.format(message, args);
            Log.d("EasyVideoPlayer", message);
        } catch (Exception ignored) {
        }
    }

    private void adjustAspectRatio(int viewWidth, int viewHeight, int videoWidth, int videoHeight) {
        final double aspectRatio = (double) videoHeight / videoWidth;
        int newWidth, newHeight;

        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }

        final int xoff = (viewWidth - newWidth) / 2;
        final int yoff = (viewHeight - newHeight) / 2;

        final Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }

    private void throwError(Exception e) {
        if (mCallback != null) mCallback.onError(this, e);
        else throw new RuntimeException(e);
    }

    private static void setTint(@NonNull SeekBar seekBar, @ColorInt int color) {
        ColorStateList s1 = ColorStateList.valueOf(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(s1);
            seekBar.setProgressTintList(s1);
            seekBar.setSecondaryProgressTintList(s1);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            Drawable progressDrawable = DrawableCompat.wrap(seekBar.getProgressDrawable());
            seekBar.setProgressDrawable(progressDrawable);
            DrawableCompat.setTintList(progressDrawable, s1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Drawable thumbDrawable = DrawableCompat.wrap(seekBar.getThumb());
                DrawableCompat.setTintList(thumbDrawable, s1);
                seekBar.setThumb(thumbDrawable);
            }
        } else {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                mode = PorterDuff.Mode.MULTIPLY;
            }
            if (seekBar.getIndeterminateDrawable() != null)
                seekBar.getIndeterminateDrawable().setColorFilter(color, mode);
            if (seekBar.getProgressDrawable() != null)
                seekBar.getProgressDrawable().setColorFilter(color, mode);
        }
    }

    private Drawable tintDrawable(@NonNull Drawable d, @ColorInt int color) {
        d = DrawableCompat.wrap(d.mutate());
        DrawableCompat.setTint(d, color);
        return d;
    }

    private void tintSelector(@NonNull View view, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && view.getBackground() instanceof RippleDrawable) {
            final RippleDrawable rd = (RippleDrawable) view.getBackground();
            rd.setColor(ColorStateList.valueOf(Util.adjustAlpha(color, 0.3f)));
        }
    }

    private void invalidateThemeColors() {
        final int labelColor = Util.isColorDark(mThemeColor) ? Color.WHITE : Color.BLACK;
        mControlsFrame.setBackgroundColor(Util.adjustAlpha(mThemeColor, 0.8f));
        tintSelector(mBtnPlayPause, labelColor);
        mLabelDuration.setTextColor(labelColor);
        mLabelPosition.setTextColor(labelColor);
        setTint(mSeeker, labelColor);
        mPlayDrawable = tintDrawable(mPlayDrawable.mutate(), labelColor);
        mPauseDrawable = tintDrawable(mPauseDrawable.mutate(), labelColor);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setFullscreen(boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (mAutoFullscreen) {
                int flags = !fullscreen ? 0 : View.SYSTEM_UI_FLAG_LOW_PROFILE;

                ViewCompat.setFitsSystemWindows(mControlsFrame, !fullscreen);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    flags |=
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                    if (fullscreen) {
                        flags |=
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
                    }
                }
                mClickFrame.setSystemUiVisibility(flags);
            }
        }
    }
}

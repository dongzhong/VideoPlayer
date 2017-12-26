package dongzhong.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dongzhong on 2017/11/8.
 */

public class VideoPlayer extends FrameLayout implements View.OnClickListener {
    private Context context;

    private FrameLayout mainView;
    private SurfaceView displayView;
    private TextView titleView;
    //private RelativeLayout coverView;
    private LinearLayout titleControlView;
    private LinearLayout bottomControlView;
    private ImageView backView;
    private ImageView startView;
    private TextView nowTimeView;
    private TextView totalTimeView;
    private SeekBar seekBar;

    private SurfaceHolder displayViewHolder;

    private MediaPlayer mediaPlayer;
    private VideoPlayerListener listener;

    private Timer progressTimer;
    private Timer coverViewVisibleTimer;

    private String url;
    private String title;
    private boolean needPlayAfterPrepared = false;

    private VideoPlayerConstant.CurrentState currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_NULL;
    private VideoPlayerConstant.CoverViewState coverViewState = VideoPlayerConstant.CoverViewState.COVER_VIEW_VISIBLE;
    private boolean isTouchingSeekBar = false;

    /******** UI资源 *********/
    private int ui_startButtonPlay = R.drawable.ic_play;
    private int ui_startButtonPause = R.drawable.ic_pause;
    private int ui_backButton = R.drawable.ic_back;
    private int ui_seekBar = R.drawable.seek_progress;
    private int ui_seekBarThumb = R.drawable.seek_thumb;
    /*************************/

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.videoplayer_displayview) {
            if (currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PLAYING
                || currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PAUSE) {
                if (coverViewState == VideoPlayerConstant.CoverViewState.COVER_VIEW_VISIBLE) {
                    coverViewState = VideoPlayerConstant.CoverViewState.COVER_VIEW_INVISIBLE;
                    setControllerVisibility(false);
                    cancelCoverViewVisibleTimer();
                }
                else {
                    coverViewState = VideoPlayerConstant.CoverViewState.COVER_VIEW_VISIBLE;
                    setControllerVisibility(true);
                    startCoverViewVisibleTimer();
                }
            }
        }
        else if (viewId == R.id.videoplayer_back) {
            currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_NULL;
            coverViewState = VideoPlayerConstant.CoverViewState.COVER_VIEW_VISIBLE;
            cancelProgressTimer();
            cancelCoverViewVisibleTimer();
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            if (listener != null) {
                listener.onBack();
            }
        }
        else if (viewId == R.id.videoplayer_start) {
            if (mediaPlayer == null) {
                return;
            }
            if (currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PREPARED
                    || currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PAUSE) {
                currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_PLAYING;
                mediaPlayer.start();
            }
            else if (currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PLAYING) {
                currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_PAUSE;
                mediaPlayer.pause();
            }
        }
    }

    /**
     * 资源预设置
     * @param url 播放内容的url
     * @param title 视频标题
     */
    public void preset(@Nullable String url, String title, boolean needPlayAfterPrepared) {
        this.url = url;
        this.needPlayAfterPrepared = needPlayAfterPrepared;
        if (title != null && !title.equals("")) {
            this.title = title;
            titleView.setText(this.title);
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(context, Uri.parse(this.url));
            applyVideoPlayerListener();
            currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_PREPARING;
            mediaPlayer.prepareAsync();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mediaPlayer == null) {
            return;
        }
        if (currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PLAYING) {
            currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_PAUSE;
            mediaPlayer.pause();
        }
    }

    /**
     * 继续播放
     */
    public void resume() {
        if (mediaPlayer == null) {
            return;
        }
        if (currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PAUSE) {
            currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_PLAYING;
            mediaPlayer.start();
        }
    }

    /**
     * 销毁控件
     */
    public void destroy() {
        cancelCoverViewVisibleTimer();
        cancelProgressTimer();
        currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_NULL;
        coverViewState = VideoPlayerConstant.CoverViewState.COVER_VIEW_INVISIBLE;
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void setVideoPlayerListener(final VideoPlayerListener listener) {
        this.listener = listener;
        applyVideoPlayerListener();
    }

    private void applyVideoPlayerListener() {
        if (mediaPlayer != null) {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (currentState != VideoPlayerConstant.CurrentState.CURRENT_STATE_PREPARING) {
                        return;
                    }
                    currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_PREPARED;

                    if (needPlayAfterPrepared) {
                        currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_PLAYING;
                        mediaPlayer.start();
                        startCoverViewVisibleTimer();
                        startProgressTimer();
                    }

                    if (listener != null) {
                        listener.onPrepared();
                    }
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    currentState = VideoPlayerConstant.CurrentState.CURRENT_STATE_OVER;
                    cancelProgressTimer();
                    cancelCoverViewVisibleTimer();

                    setControllerVisibility(true);
                    coverViewState = VideoPlayerConstant.CoverViewState.COVER_VIEW_VISIBLE;

                    if (listener != null) {
                        listener.onCompletion();
                    }
                }
            });
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    if (currentState != VideoPlayerConstant.CurrentState.CURRENT_STATE_NULL
                            && currentState != VideoPlayerConstant.CurrentState.CURRENT_STATE_PREPARING) {
                        setProgressBuffer(percent);

                        if (listener != null) {
                            listener.onBufferingUpdate(percent);
                        }
                    }
                }
            });
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {

                    if (listener != null) {
                        listener.onSeekComplete();
                    }
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (what != -38) {
                        mediaPlayer.release();
                        cancelProgressTimer();
                        cancelCoverViewVisibleTimer();

                        if (listener != null) {
                            listener.onError(what, extra);
                        }
                    }
                    return true;
                }
            });
            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    int videoHeight = height;
                    int videoWidth = width;
                    if (videoHeight != 0 && videoWidth != 0) {
                        displayViewHolder.setFixedSize(videoWidth, videoHeight);
                        displayView.requestLayout();

                        if (listener != null) {
                            listener.onVideoSizeChanged(videoHeight, videoWidth);
                        }
                    }
                }
            });

        }
    }

    private void init(Context context) {
        this.context = context;

        mediaPlayer = new MediaPlayer();

        View.inflate(this.context, R.layout.video_player, this);
        mainView = (FrameLayout) findViewById(R.id.videoplayer_mainview);
        displayView = (SurfaceView) findViewById(R.id.videoplayer_displayview);
        titleView = (TextView) findViewById(R.id.videoplayer_title);
        titleControlView = (LinearLayout) findViewById(R.id.videoplayer_title_control);
        bottomControlView = (LinearLayout) findViewById(R.id.videoplayer_bottom_control);
        backView = (ImageView) findViewById(R.id.videoplayer_back);
        startView = (ImageView) findViewById(R.id.videoplayer_start);
        nowTimeView = (TextView) findViewById(R.id.videoplayer_nowtime);
        totalTimeView = (TextView) findViewById(R.id.videoplayer_totaltime);
        seekBar = (SeekBar) findViewById(R.id.videoplayer_progress);

//        backView.setImageResource(ui_backButton);
//        startView.setImageResource(ui_startButtonPlay);

        displayViewHolder = displayView.getHolder();
        displayViewHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mediaPlayer.setDisplay(displayViewHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        displayView.setOnClickListener(this);
        startView.setOnClickListener(this);
        backView.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int duration = mediaPlayer.getDuration();
                    int seekPosition = duration * progress / 100;
                    mediaPlayer.seekTo(seekPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouchingSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouchingSeekBar = false;
            }
        });

        listener = new VideoPlayerListener() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onCompletion() {

            }

            @Override
            public void onBufferingUpdate(int percent) {

            }

            @Override
            public void onSeekComplete() {

            }

            @Override
            public void onError(int what, int extra) {

            }

            @Override
            public void onVideoSizeChanged(int height, int width) {

            }

            @Override
            public void onBack() {

            }
        };
    }

    /**
     * 开启进度条时钟任务
     */
    private void startProgressTimer() {
        cancelProgressTimer();
        progressTimer = new Timer();
        progressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (context != null && context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer == null) {
                                return;
                            }
                            if (currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PLAYING) {
                                // TODO: 设置进度条和进度时间
                                int position = mediaPlayer.getCurrentPosition();
                                int duration = mediaPlayer.getDuration();
                                int percent = position * 100 / (duration == 0 ? 1 : duration);
                                updateProgress(percent, position, duration);
                                startView.setImageResource(ui_startButtonPause);
                            }
                            else if (currentState == VideoPlayerConstant.CurrentState.CURRENT_STATE_PAUSE) {
                                startView.setImageResource(ui_startButtonPlay);
                            }
                            else {
                                startView.setImageResource(ui_startButtonPlay);
                            }
                        }
                    });
                }
            }
        }, 0, 300);
    }

    /**
     * 取消进度条时钟任务
     */
    private void cancelProgressTimer() {
        if (progressTimer != null) {
            progressTimer.cancel();
        }
    }

    /**
     * 开启控制控件消失时钟任务
     */
    private void startCoverViewVisibleTimer() {
        cancelCoverViewVisibleTimer();
        coverViewVisibleTimer = new Timer();
        coverViewVisibleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (context != null && context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setControllerVisibility(false);
                            coverViewState = VideoPlayerConstant.CoverViewState.COVER_VIEW_INVISIBLE;
                        }
                    });
                }
            }
        }, 3000);
    }

    /**
     * 取消控制控件消失时钟任务
     */
    private void cancelCoverViewVisibleTimer() {
        if (coverViewVisibleTimer != null) {
            coverViewVisibleTimer.cancel();
        }
    }

    /**
     * 更新进度条和时间
     * @param percent 播放进度
     * @param currentTime 播放时间
     * @param totalTime 总时间
     */
    private void updateProgress(int percent, int currentTime, int totalTime) {
        if (!isTouchingSeekBar && percent >= 0) {
            seekBar.setProgress(percent);
        }
        nowTimeView.setText(Utils.parseTimeToString(currentTime));
        totalTimeView.setText(Utils.parseTimeToString(totalTime));
    }

    /**
     * 设置缓冲进度
     * @param percent
     */
    private void setProgressBuffer(int percent) {
        if (percent >= 0) {
            seekBar.setSecondaryProgress(percent);
        }
    }
    
    private void setControllerVisibility(boolean isVisible) {
        if (isVisible) {
            titleControlView.setVisibility(View.VISIBLE);
            bottomControlView.setVisibility(View.VISIBLE);
            backView.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.VISIBLE);
            startView.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            nowTimeView.setVisibility(View.VISIBLE);
            totalTimeView.setVisibility(View.VISIBLE);
        }
        else {
            titleControlView.setVisibility(View.INVISIBLE);
            bottomControlView.setVisibility(View.INVISIBLE);
            backView.setVisibility(View.INVISIBLE);
            titleView.setVisibility(View.INVISIBLE);
            startView.setVisibility(View.INVISIBLE);
            seekBar.setVisibility(View.INVISIBLE);
            nowTimeView.setVisibility(View.INVISIBLE);
            totalTimeView.setVisibility(View.INVISIBLE);
        }
    }

    private String getCoverViewState(VideoPlayerConstant.CoverViewState state) {
        if (state == VideoPlayerConstant.CoverViewState.COVER_VIEW_INVISIBLE) {
            return "处于不可见状态";
        }
        else {
            return "处于可见状态";
        }
    }

    /*************** 设置UI *****************/
    /**
     * 设置播放暂停按钮的UI
     * @param resPlay
     * @param resPause
     */
    public void setUIStartButton(int resPlay, int resPause) {
        setUIStartButtonPlay(resPlay);
        setUIStartButtonPause(resPause);
    }

    /**
     * 设置播放按钮的UI
     * @param res
     */
    public void setUIStartButtonPlay(int res) {
        ui_startButtonPlay = res;
    }

    /**
     * 设置暂停按钮的UI
     * @param res
     */
    public void setUIStartButtonPause(int res) {
        ui_startButtonPause = res;
    }

    /**
     * 设置返回按钮的UI
     * @param res
     */
    public void setUIBackButton(int res) {
        ui_backButton = res;
        if (backView != null) {
            backView.setImageResource(ui_backButton);
        }
    }

    /**
     * 设置进度条UI
     * @param res
     */
    public void setUISeekBar(int res) {
        ui_seekBar = res;
        if (seekBar != null) {
            if (context != null) {
                Drawable progressDrawable = context.getResources().getDrawable(ui_seekBar);
                seekBar.setProgressDrawable(progressDrawable);
            }
        }
    }

    /**
     * 设置进度条Thumb的UI
     * @param res
     */
    public void setUISeekBarThumb(int res) {
        ui_seekBarThumb = res;
        if (seekBar != null) {
            if (context != null) {
                Drawable thumbDrawable = context.getResources().getDrawable(ui_seekBarThumb);
                seekBar.setThumb(thumbDrawable);
            }
        }
    }

    /**
     * 设置标题栏字体大小
     * @param unit
     * @param size
     */
    public void setTitleTextSize(int unit, float size) {
        titleView.setTextSize(unit, size);
    }

    /**
     * 设置时间字体大小
     * @param unit
     * @param size
     */
    public void setTimeTextViewSize(int unit, float size) {
        nowTimeView.setTextSize(unit, size);
        totalTimeView.setTextSize(unit, size);
    }
    /****************************************/
}

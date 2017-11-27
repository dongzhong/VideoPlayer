package dongzhong.videoplayer;

/**
 * Created by dongzhong on 2017/11/8.
 */

public interface VideoPlayerListener {
    void onPrepared();

    void onCompletion();

    void onBufferingUpdate(int percent);

    void onSeekComplete();

    void onError(int what, int extra);

    void onVideoSizeChanged(int height, int width);

    void onBack();
}

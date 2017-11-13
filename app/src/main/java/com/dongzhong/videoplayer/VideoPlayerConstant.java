package com.dongzhong.videoplayer;

/**
 * Created by dongzhong on 2017/11/8.
 */

public class VideoPlayerConstant {
    public enum CurrentState {
        CURRENT_STATE_NULL,
        CURRENT_STATE_PREPARING,
        CURRENT_STATE_PREPARED,
        CURRENT_STATE_PAUSE,
        CURRENT_STATE_PLAYING,
        CURRENT_STATE_OVER,
    }

    public enum CoverViewState {
        COVER_VIEW_VISIBLE,
        COVER_VIEW_INVISIBLE,
    }
}

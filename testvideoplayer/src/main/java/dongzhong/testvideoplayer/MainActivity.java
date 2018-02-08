package dongzhong.testvideoplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import dongzhong.videoplayer.VideoPlayer;
import dongzhong.videoplayer.VideoPlayerListener;

public class MainActivity extends AppCompatActivity {
    private VideoPlayer videoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        videoPlayer.setVideoPlayerListener(new VideoPlayerListener() {
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
        });
        videoPlayer.setUIStartButton(R.drawable.ic_play, R.drawable.ic_pause);
        videoPlayer.setUIBackButton(R.drawable.ic_back);
        videoPlayer.preset("https://github.com/dongzhong/ImageAndVideoStore/blob/master/Bruno%20Mars%20-%20Treasure.mp4?raw=true",
                "视频", true);
    }
}

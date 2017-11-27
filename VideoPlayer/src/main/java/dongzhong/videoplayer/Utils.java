package dongzhong.videoplayer;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by dongzhong on 2017/11/9.
 */

class Utils {
    public static String parseTimeToString(int timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        int totalSeconds = timeMs / 1000;
        int second = totalSeconds % 60;
        int minute = (totalSeconds / 60) % 60;
        int hour = totalSeconds / 3600;
        StringBuilder timeString = new StringBuilder();
        Formatter formatter = new Formatter(timeString, Locale.getDefault());
        if (hour > 0) {
            return formatter.format("%d:%02d:%02d", hour, minute, second).toString();
        }
        else {
            return formatter.format("%02d:%02d", minute, second).toString();
        }
    }
}

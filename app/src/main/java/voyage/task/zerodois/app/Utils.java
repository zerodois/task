package voyage.task.zerodois.app;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;

/**
 * Created by felipe on 19/09/17.
 */

public class Utils {

    public static final void setSystemBarLight(final Activity activity, final int color, final boolean dark) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            final Window window = activity.getWindow();
            final int lFlags = window.getDecorView().getSystemUiVisibility();
            window.getDecorView().setSystemUiVisibility(!dark ? (lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (lFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
            window.setStatusBarColor(color);
        }
    }

    public static final void setSystemBarLight(final Activity activity, final int color) {
        setSystemBarLight(activity, color, false);
    }
}
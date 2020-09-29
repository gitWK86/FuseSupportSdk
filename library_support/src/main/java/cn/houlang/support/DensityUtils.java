package cn.houlang.support;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.text.DecimalFormat;

//import android.support.annotation.RequiresApi;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class DensityUtils {
    private DensityUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static String convertSize2String(int size) {
        String result = null;
        int K = 1024;
        int M = K * 1024;
        int G = M * 1024;
        DecimalFormat fmt = new DecimalFormat("#.##");
        if (size / K < 1) {
            result = size + "K";
        } else if (size / M < 1) {
            result = fmt.format(size * 1.0 / K) + "M";
        } else if (size / G < 1) {
            result = fmt.format(size * 1.0 / M) + "G";
        } else {
            result = fmt.format(size * 1.0 * K / G) + "G";
        }
        return result;
    }

    /**
     * 获取屏幕分辨率【方法体过时，推荐用getHeigthAndWidth】
     *
     * @param activity
     * @return
     */
    @Deprecated
    public static int[] getHeigthAndWeigth(Context activity) {
        WindowManager wm = (WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int[] disp = new int[2];

        disp[0] = metrics.widthPixels; // 当前屏幕像素
        disp[1] = metrics.heightPixels; // 当前屏幕像素

        return disp;
    }

    /**
     * 获取屏幕分辨率
     *
     * @param activity
     * @return
     */
    public static int[] getHeigthAndWidth(Activity activity) {
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int w = point.x;
        int h = point.y;

        int[] disp = new int[2];
        disp[0] = w;
        disp[1] = h;
        return disp;
    }

    /**
     * 获取屏幕分辨率widthPixels
     *
     * @param activity
     * @return
     */
    public static int getDisplayWidth(Activity activity) {
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int w = point.x;
        int h = point.y;
        return w;
    }

    /**
     * 获取屏幕分辨率heightPixels
     *
     * @param activity
     * @return
     */
    public static int getDisplayHeigth(Activity activity) {
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int w = point.x;
        int h = point.y;
        return h;
    }

    /**
     * 【方法体过时，推荐用 getResolutionByFullScreen】
     *
     * @param context
     * @return
     */
    @Deprecated
    public static String getResolution(Context context) {
        try {
            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            int w = metrics.widthPixels;
            int h = metrics.heightPixels;
            return w + "x" + h;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "0x0";

    }

    /**
     * 获取分辨率：应用程序的显示区域，不包系统栏
     *
     * @param activity
     * @return
     */
    public static String getResolutionByApp(Activity activity) {
        try {
            Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
            Point point = new Point();
            defaultDisplay.getSize(point);
            int w = point.x;
            int h = point.y;
            return w + "x" + h;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0x0";
    }

    /**
     * 获取分辨率：整个屏幕，包系统栏
     *
     * @param activity
     * @return
     */
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getResolutionByFullScreen(Activity activity) {
        try {
            Point outSize = new Point();
            activity.getWindowManager().getDefaultDisplay().getRealSize(outSize);
            int w = outSize.x;
            int h = outSize.y;

            return w + "x" + h;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0x0";
    }
}

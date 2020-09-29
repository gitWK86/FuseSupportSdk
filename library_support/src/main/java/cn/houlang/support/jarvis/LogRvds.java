package cn.houlang.support.jarvis;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Array;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class LogRvds {
    public static boolean DEBUG = true;
    public static Handler handler;

    private static final String TAG = "houlang_rvds";

    public static void e(String s) {
        if (s != null)
            e(TAG, s);
    }

    public static void e(String tag, String s) {
        if (s != null)
            Log.e(tag, s);
    }

    /**
     * 正常可见
     *
     * @param obj
     */
    public static void i(String tag, Object obj) {
        String s;
        if (obj == null) {
            s = "null";
        } else {
            Class<? extends Object> clz = obj.getClass();
            if (clz.isArray()) {
                StringBuilder sb = new StringBuilder(clz.getSimpleName());
                sb.append(" [ ");
                int len = Array.getLength(obj);
                for (int i = 0; i < len; i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    Object tmp = Array.get(obj, i);
                    sb.append(tmp);
                }
                sb.append(" ]");
                s = sb.toString();
            } else {
                s = "" + obj;
            }
        }
        Log.i(tag, s);
    }

    /**
     * 正常可见
     *
     * @param obj
     */
    public static void i(Object obj) {
        i(TAG, obj);
    }

    /**
     * own debug true可见
     *
     * @param obj
     */
    public static void d(Object obj) {
        d(TAG, obj);
    }

    public static void d(String tag, Object obj) {
        String s;
        if (obj == null) {
            s = "null";
        } else {
            Class<? extends Object> clz = obj.getClass();
            if (clz.isArray()) {
                StringBuilder sb = new StringBuilder(clz.getSimpleName());
                sb.append(" [ ");
                int len = Array.getLength(obj);
                for (int i = 0; i < len; i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    Object tmp = Array.get(obj, i);
                    sb.append(tmp);
                }
                sb.append(" ]");
                s = sb.toString();
            } else {
                s = "" + obj;
            }
        }
        if (DEBUG) {
            Log.d(tag, s);
        }
    }

    public static void logHandler(Handler handler, String s) {
        if (handler != null) {
            Message msg = new Message();
            msg.what = 111;
            msg.obj = s;
            handler.sendMessage(msg);
        }
    }

    public static void logHandler(String s) {
        if (handler != null) {
            logHandler(handler, s);
        }
    }
}

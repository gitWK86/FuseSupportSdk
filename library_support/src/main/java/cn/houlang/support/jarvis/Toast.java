package cn.houlang.support.jarvis;

import android.content.Context;

/**
 * @author #Suyghur.
 * Created on 2020/7/13
 */
public class Toast {

    public static boolean DEBUG = false;

    public static void toastInfo(Context context, String message) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    public static void toastDebugInfo(Context context, String msg) {
        if (DEBUG) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}

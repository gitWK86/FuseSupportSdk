package cn.houlang.rvds;

import android.content.Context;

/**
 * @author #Suyghur.
 * Created on 2020/7/30
 */
public class ResUtils {

    public static int getResId(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }
}

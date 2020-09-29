package cn.houlang.support;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author #Suyghur.
 * Created on 9/25/20
 */
public class HostModelUtils {


    //主机环境：1.dev 2.test 3.online 4.马甲包
    public static final int ENV_DEV = 1;
    public static final int ENV_TEST = 2;
    public static final int ENV_ONLINE = 3;

    public static void setHostModel(Context context, int ipModel) {
        SharedPreferences sp = context.getSharedPreferences("houlang_host_model", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("host_model", ipModel);
        editor.apply();
    }

    public static int getHostModel(Context context) {
        SharedPreferences sp = context.getSharedPreferences("houlang_host_model", MODE_PRIVATE);
        //默认线上环境
        return sp.getInt("host_model", 3);
    }
}

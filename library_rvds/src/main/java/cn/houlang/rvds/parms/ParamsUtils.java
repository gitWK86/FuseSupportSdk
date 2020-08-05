package cn.houlang.rvds.parms;

import android.content.Context;

import cn.houlang.rvds.jarvis.OwnDebugUtils;
import cn.houlang.rvds.jarvis.Trace;


/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class ParamsUtils {

    public static final String CONFIG_NAME = "houlang_game.properties";
    private static final String WAVES_PLATFORM_ID = "HL_PLATFORM_ID";
    private static final String HL_GID = "HL_GID";
    private static final String HL_CID = "HL_CID";
    private static final String HL_PID = "HL_PID";
    private static final String HL_H5_GAME = "HL_H5_GAME";
    private static final String HL_HAS_SPLASH = "HL_HAS_SPLASH";


//    /**
//     * 接入平台渠道id
//     *
//     * @param ctx
//     * @return
//     */
//    public static int getWavesPlatformId(Context ctx) {
//        if (ctx == null) {
//            RvdsLog.e("getPlatformChannelId Context is null");
//        }
//        String ids = PropertiesUtils.getValue4Properties(ctx, CONFIG_NAME, WAVES_PLATFORM_ID);
//        if (ids != null) {
//            return Integer.parseInt(ids);
//        }
//        return -1;
//    }

    public static String getGid(Context ctx) {
        if (ctx == null) {
            Trace.e("getGId Context is null");
        }
        return PropertiesUtils.getValue4Properties(ctx, CONFIG_NAME, HL_GID);
    }

    /**
     * 接入平台渠道id
     *
     * @param ctx
     * @return
     */
    public static String getCid(Context ctx) {
        return PropertiesUtils.getValue4Properties(ctx, CONFIG_NAME, HL_CID);
    }

    public static String getPid(Context ctx) {
        return PropertiesUtils.getValue4Properties(ctx, CONFIG_NAME, HL_PID);
    }

    public static boolean getH5GameFlag(Context ctx) {
        String h5 = PropertiesUtils.getValue4Properties(ctx, CONFIG_NAME, HL_H5_GAME);
        if (h5 != null) {
            return Boolean.parseBoolean(h5);
        }
        return false;
    }

    public static boolean getHasSplashFlag(Context ctx) {
        String hasLogo = PropertiesUtils.getValue4Properties(ctx, CONFIG_NAME, HL_HAS_SPLASH);
        if (hasLogo != null) {
            return Boolean.parseBoolean(hasLogo);
        }
        return false;
    }

    /**
     * 获取 我们内部使用 debug模式
     *
     * @param context
     * @return
     */
    public static boolean getOWNDebug(Context context) {
        //优先取sd卡里的
        boolean isOwn = OwnDebugUtils.isOwnDebug(context);
        return isOwn;
    }
}

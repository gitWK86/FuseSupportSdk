package cn.houlang.rvds.jarvis;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

import cn.houlang.rvds.FileUtils;
import cn.houlang.rvds.SDCardUtils;
import cn.houlang.rvds.PropertiesUtils;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class OwnDebugUtils {
    private static final String DOW_DEBUG_FILE = "own.txt";

    /**
     * 判断是否是own debug模式，从sd卡取
     *
     * @param mContext
     * @return
     */
    public static boolean isOwnDebug(Context mContext) {
        if (!SDCardUtils.isMounted()) {
            return getOwnDebugFromFuseCfg(mContext);
        }
        if (!FileUtils.hasPermission(mContext, FileUtils.PERMISSION_READ_EXTERNAL_STORAGE)) {
            //log("get sd utma failed, no read sd card permission ");
            return getOwnDebugFromFuseCfg(mContext);
        }
        String fileName = Environment.getExternalStorageDirectory() + "/" + DOW_DEBUG_FILE;
        File file2 = new File(fileName);
        if (!file2.exists()) {
            return getOwnDebugFromFuseCfg(mContext);
        }
        String data = FileUtils.readFile(fileName);
        if (!TextUtils.isEmpty(data)) {
            return data.trim().equals("own");
        } else {
            return false;
        }
    }

    public static boolean getOwnDebugFromFuseCfg(Context context) {

        String value = PropertiesUtils.getValue4Properties(context, "houlang_game.properties", "HL_OWN_DEBUG");
        LogRvds.d("own value : " + value);
        return !TextUtils.isEmpty(value) && Boolean.parseBoolean(value);
    }

    /**
     * 写入own debug模式
     *
     * @param mContext
     * @return
     */
    public static boolean writeOwnDebug(Context mContext) {
        if (!SDCardUtils.isMounted()) {
            return false;
        }
        if (!FileUtils.hasPermission(mContext, FileUtils.PERMISSION_READ_EXTERNAL_STORAGE)) {
            //log("get sd utma failed, no read sd card permission ");
            return false;
        }
        String fileName = Environment.getExternalStorageDirectory() + "/" + DOW_DEBUG_FILE;
        return FileUtils.writeStringToFile("own", fileName, false, null);
    }
}

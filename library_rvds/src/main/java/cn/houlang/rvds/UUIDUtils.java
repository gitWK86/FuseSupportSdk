package cn.houlang.rvds;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import cn.houlang.rvds.device.DeviceInfoUtils;
import cn.houlang.rvds.encryption.Md5Utils;
import cn.houlang.rvds.encryption.aes.AesUtils;
import cn.houlang.rvds.jarvis.Trace;

/**
 * 应用存储无缓存，SD卡无缓存，新生成UTMA，保存到应用缓存和SD卡缓存（有权限）
 * 应用存储有缓存，SD卡无缓存，获取应用缓存，并同步到SD卡缓存（有权限）
 * 应用存储无缓存，SD卡有缓存，获取SD卡缓存，并同步到应用缓存
 * 应用存储有缓存，SD卡有缓存，优先获取应用缓存
 * 测试内容：
 * 没有权限：
 * 1.随机uuid存入app缓存，请求检测imei接口，是黑名单，重新生成uuid存入app缓存，然后是初始化接口
 * 有权限
 * 1.从sd卡读取uuid，不请求检测imei接口，走初始化
 * 2.sd卡没有数据，就规则生成uuid存入app缓存和sd卡，请求检测imei接口，是否要重新生成uuid
 * 没有权限，然后再给权限
 * 按步骤是会先app缓存，值保持不变
 *
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class UUIDUtils {

    private static boolean isPrintLog;
    private static String TAG;

    private static final String UUID_AES_KEY = "houlangyouxi2020";

    public static void setPrint(boolean isPrint, String tag) {
        isPrintLog = isPrint;
        TAG = tag;
    }

    private static void log(String s) {
        if (isPrintLog) {
            if (TAG != null) {
                Log.d(TAG, s);
            } else {
                Log.d("kkk_tools", s);
            }
        }
    }

    /**
     * 获取utma
     * 1.先从sd卡取，如果没有再从app缓存取，都没有就生成utma
     * 2.从app缓存取，没有再从sd卡取
     * 3.如果app缓存为空，sd不为空，同步数据到app缓存
     * 4.如果sd为空，app缓存不为空，同步数据到sd
     *
     * @param context
     * @return
     */
    public static synchronized String getUUIDInfo(Context context) {
        String uuid = null;
        String uuidCache = get2AppCache(context, FileUtils.UUID_C_DAT);

        String uuidSDCard = get2SDCard(context, FileUtils.UUID_C_DAT);
        //如果2个都是空
        if (TextUtils.isEmpty(uuidCache) && TextUtils.isEmpty(uuidSDCard)) {
            log("uuidCache & uuidSDCard = null");
            try {
                String imei = DeviceInfoUtils.getImei(context);
                //imei = "0";
                //未获取到imei或imei为重复的情况，utma为随机uuid
                if (TextUtils.isEmpty(imei) || imei.equals("0") || imei.equals("000000000000000")) {
                    String timeMillis = (System.currentTimeMillis() / 1000) + "";
                    //万一String位数不够后面补0
                    timeMillis = StrUtils.getStringAppendLength(timeMillis, 10);
                    //随机生成utma，格式：ar_时间戳10位+19位随机字母和数字
                    uuid = "ar_" + timeMillis + "" + StrUtils.getRandomString(19).toLowerCase();
                    log("随机生成utma = " + uuid);
                } else {
                    log("new md5 imei");
                    uuid = Md5Utils.encodeByMD5(DeviceInfoUtils.getModel() + DeviceInfoUtils.getMacAddress(context) + imei);
                }
                saveUUIDInfo(context, uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //如果app缓存的和sd卡都有值
            if (!TextUtils.isEmpty(uuidCache) && !TextUtils.isEmpty(uuidSDCard)) {
                //不管2个值是否相同，都优先取utmaCache
                log("uuidCache & uuidSDCard not null, get uuidCache");
                log("uuidCache = " + uuidCache + "\tuuidSDCard =" + uuidSDCard);
                uuid = uuidCache;
            } else {
                //如果app缓存为空，sd不为空，同步数据到app缓存
                if (TextUtils.isEmpty(uuidCache) && !TextUtils.isEmpty(uuidSDCard)) {
                    Trace.d("uuidCache is null, uuidSDCard not null");
                    uuid = uuidSDCard;
                    //putRequestCheckImei(context, false);//不请求检查imei接口
                    saveUUIDInfo2AppCache(context, uuidSDCard);

                }
                //如果sd为空，app缓存不为空，同步数据到sd
                if (!TextUtils.isEmpty(uuidCache) && TextUtils.isEmpty(uuidSDCard)) {
                    log("uuidCache not null, uuidSDCard is null");
                    uuid = uuidCache;
                    saveUtmaInfo2SDCard(context, uuidCache);

                }
            }
            //如果app缓存的和sd卡的值不一样，取app缓存的uuidCache的值
            if (uuid == null) {
                log("uuidCache i& uuidSDCard not same, get uuidCache");
                uuid = uuidCache != null ? uuidCache : uuidSDCard;
            }
        }
        log("getUUIDInfo uuid = " + uuid);
        return uuid;
    }

    /**
     * 重新生成utma
     *
     * @param mContext
     * @return
     */
    public static synchronized String makeUUIDInfo(Context mContext) {
        String uuid = null;
        String timeMillis = (System.currentTimeMillis() / 1000) + "";
        //万一String位数不够后面补0
        timeMillis = StrUtils.getStringAppendLength(timeMillis, 10);
        //随机生成utma，格式：ar_时间戳10位+19位随机字母和数字
        uuid = "ar_" + timeMillis + "" + StrUtils.getRandomString(19).toLowerCase();
        log("makeUUIDInfo 重新生成uuid = " + uuid);
        saveUUIDInfo(mContext, uuid);
//        putRequestCheckImei(mContext, false);
        return uuid;
    }

    private static synchronized void saveUUIDInfo(Context mContext, String utma) {
        try {
            saveUUIDInfo2AppCache(mContext, utma);
            saveUtmaInfo2SDCard(mContext, utma);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 将uuid写入app缓存
     *
     * @param context
     * @param uuid
     */
    private static void saveUUIDInfo2AppCache(Context context, String uuid) {
        if (!TextUtils.isEmpty(uuid)) {
            // AES加密
            String content = null;
            try {
                content = AesUtils.encrypt(UUID_AES_KEY, uuid);
                FileUtils.write2AppCache(context, FileUtils.UUID_C_DAT, content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将uuid写入sd card
     *
     * @param context
     * @param uuid
     */
    private static void saveUtmaInfo2SDCard(Context context, String uuid) {
        if (!FileUtils.hasPermission(context, FileUtils.PERMISSION_WRITE_EXTERNAL_STORAGE)) {
            log("save uuid failed, no write sd card permission ");
            return;
        }
        if (!TextUtils.isEmpty(uuid)) {
            // AES加密
            String content = null;
            try {
                content = AesUtils.encrypt(UUID_AES_KEY, uuid);
                FileUtils.writeStringToFile(content, Environment.getExternalStorageDirectory() + FileUtils.INFO_DIR + FileUtils.UUID_C_DAT, false, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * 从app缓存读数据
     *
     * @param mContext
     * @param fileName
     * @return
     */
    private static String get2AppCache(Context mContext, String fileName) {
        String data = FileUtils.get2AppCache(mContext, fileName);
        String content = null;
        if (!TextUtils.isEmpty(data)) {
            // AES解密
            try {
                content = AesUtils.encrypt(UUID_AES_KEY, data);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return content;

    }


    /**
     * 从sd卡读数据
     *
     * @param context
     * @param fileName
     * @return
     */
    private static String get2SDCard(Context context, String fileName) {
        if (!SDCardUtils.isMounted()) {
            return null;
        }
        if (!FileUtils.hasPermission(context, FileUtils.PERMISSION_READ_EXTERNAL_STORAGE)) {
            log("get sd uuid failed, no read sd card permission ");
            return null;
        }
        String fileName2 = Environment.getExternalStorageDirectory() + FileUtils.INFO_DIR + fileName;
        File file2 = new File(fileName2);
        if (!file2.exists()) {
            return null;
        }
        String data = FileUtils.readFile(fileName2);
        String content = null;
        if (!TextUtils.isEmpty(data)) {
            // AES解密
            try {
                content = AesUtils.decrypt(UUID_AES_KEY, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return content;
    }


    private static synchronized void putRequestCheckImei(Context activity, boolean b) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("commonsdk_is_request_check_imei", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_check_imei", b);
        editor.commit();
    }

    /**
     * 是否需要请求检测imei接口
     *
     * @param activity
     * @return
     */
    public static synchronized boolean isRequestCheckImei(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("commonsdk_is_request_check_imei", Context.MODE_PRIVATE);
        boolean data = sharedPreferences.getBoolean("is_check_imei", true);
        return data;
    }
}

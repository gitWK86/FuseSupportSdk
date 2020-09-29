package cn.houlang.support;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import cn.houlang.support.encryption.aes.AesUtils;

/**
 * 应用存储无缓存，SD卡无缓存，新生成UUID，保存到应用缓存和SD卡缓存（有权限）
 * 应用存储有缓存，SD卡无缓存，获取应用缓存，并同步到SD卡缓存（有权限）
 * 应用存储无缓存，SD卡有缓存，获取SD卡缓存，并同步到应用缓存
 * 应用存储有缓存，SD卡有缓存，优先获取应用缓存
 * 测试内容：
 * 没有权限：
 * 1.随机uuid存入app缓存，请求检测imei接口，是黑名单，重新生成uuid存入app缓存，然后是初始化接口
 * 有权限
 * 1.从sd卡读取uuid
 * 2.sd卡没有数据，就规则生成uuid存入app缓存和sd卡
 * 没有权限，然后再给权限
 * 按步骤是会先app缓存，值保持不变
 *
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class UUIDUtils {
    private static boolean isPrintLog;
    private static String TAG;

    private static final String AESKEY = "houlang2020game0";

    public static void setPrint(boolean isPrint, String tag) {
        isPrintLog = isPrint;
        TAG = tag;
    }

    private static void log(String s) {
        if (isPrintLog) {
            if (TAG != null) {
                Log.d(TAG, s);
            } else {
                Log.d("rvds", s);
            }
        }
    }

    /**
     * 获取uuid
     * 1.先从sd卡取，如果没有再从app缓存取，都没有就生成uuid
     * 2.从app缓存取，没有再从sd卡取
     * 3.如果app缓存为空，sd不为空，同步数据到app缓存
     * 4.如果sd为空，app缓存不为空，同步数据到sd
     *
     * @param context
     * @return
     */
    public static synchronized String getUUID(Context context) {
        //FileUtils.mkdirs(Environment.getExternalStorageDirectory() + FileUtils.INFO_DIR);
        //1.先从sd卡取，如果没有再从app缓存取，都没有就生成uuid
        //2.从app缓存取，没有再从sd卡取
        //
        String uuid = null;
        String uuidCache = get2AppCache(context);
        String uuidSDCard = get2SDCard(context);
        //如果2个都是空
        if (TextUtils.isEmpty(uuidCache) && TextUtils.isEmpty(uuidSDCard)) {
            log("uuidCache & uuidSDCard = null");
            try {
                String timeMillis = (System.currentTimeMillis() / 1000) + "";
                //万一String位数不够后面补0
                timeMillis = StrUtils.getStringAppendLength(timeMillis, 10);
                //随机生成uuid，格式：uar_时间戳10位+18位随机字母和数字
                uuid = "uar_" + timeMillis + "" + StrUtils.getRandomString(18).toLowerCase();
                log("随机生成uuid = " + uuid);
                saveUUID(context, uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //如果app缓存的和sd卡都有值
            if (!TextUtils.isEmpty(uuidCache) && !TextUtils.isEmpty(uuidSDCard)) {
                //不管2个值是否相同，都优先取uuidCache
                log("uuidCache & uuidSDCard not null, get uuidSDCard");
                log("uuidCache = " + uuidCache + "\tuuidSDCard =" + uuidSDCard);
                uuid = uuidCache;
            } else {
                //如果app缓存为空，sd不为空，同步数据到app缓存
                if (TextUtils.isEmpty(uuidCache) && !TextUtils.isEmpty(uuidSDCard)) {
                    log("uuidCache is null, uuidSDCard not null");
                    uuid = uuidSDCard;
                    saveUUID2AppCache(context, uuidSDCard);
                }
                //如果sd为空，app缓存不为空，同步数据到sd
                if (!TextUtils.isEmpty(uuidCache) && TextUtils.isEmpty(uuidSDCard)) {
                    log("uuidCache not null, uuidSDCard is null");
                    uuid = uuidCache;
                    saveUUID2SDCard(context, uuidCache);
                }
            }
            if (uuid == null) {
                uuid = uuidSDCard != null ? uuidSDCard : uuidCache;
            }
        }
        log("getUUID uuid = " + uuid);
        return uuid;
    }

    private static synchronized void saveUUID(Context mContext, String uuid) {
        try {
            saveUUID2AppCache(mContext, uuid);
            saveUUID2SDCard(mContext, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将uuid写入app缓存
     *
     * @param context
     * @param uuid
     */
    private static void saveUUID2AppCache(Context context, String uuid) {
        if (!TextUtils.isEmpty(uuid)) {
            // AES加密
            try {
                String enc = AesUtils.encrypt(AESKEY, uuid);
                FileUtils.write2AppCache(context, FileUtils.UUID_C_DAT, enc);
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
    private static void saveUUID2SDCard(Context context, String uuid) {
        if (!FileUtils.hasPermission(context, FileUtils.PERMISSION_WRITE_EXTERNAL_STORAGE)) {
            log("save uuid failed, no write sd card permission ");
            return;
        }
        if (!TextUtils.isEmpty(uuid)) {
            try {
                String enc = AesUtils.encrypt(AESKEY, uuid);
                FileUtils.writeStringToFile(enc, Environment.getExternalStorageDirectory() + FileUtils.INFO_DIR + FileUtils.UUID_C_DAT, false, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从app缓存读数据
     *
     * @param context
     * @return
     */
    private static String get2AppCache(Context context) {
        String enc = FileUtils.get2AppCache(context, FileUtils.UUID_C_DAT);
        if (!TextUtils.isEmpty(enc)) {
            // AES解密
            try {
                return AesUtils.decrypt(AESKEY, enc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 从sd卡读数据
     *
     * @param context
     * @return
     */
    private static String get2SDCard(Context context) {
        if (!SDCardUtils.isMounted()) {
            return null;
        }
        if (!FileUtils.hasPermission(context, FileUtils.PERMISSION_READ_EXTERNAL_STORAGE)) {
            log("get sd uuid failed, no read sd card permission ");
            return null;
        }
        String fileName = Environment.getExternalStorageDirectory() + FileUtils.INFO_DIR + FileUtils.UUID_C_DAT;
        File file2 = new File(fileName);
        if (!file2.exists()) {
            return null;
        }
        String data = FileUtils.readFile(fileName);
        if (!TextUtils.isEmpty(data)) {
            // AES解密
            try {
                return AesUtils.decrypt(AESKEY, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

package cn.houlang.rvds;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class SDCardUtils {

//    public static boolean externalStorageLegacyEnable() {
//        if (Build.VERSION.SDK_INT >= 29) {
//            final File externalDir = sCurrentUser.getExternalDirs()[0];
//            return Environment.isExternalStorageLegacy(externalDir);
//        } else {
//            return true;
//        }
//    }

    private SDCardUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获取SD卡路径
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }


    /**
     * 判断SD卡是否存在，并且是否具有读写权限
     *
     * @return
     */
    public static boolean isMounted() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }
        if (!checkExternalStorageCanWrite()) {
            return false;
        }
        return true;
    }

    /**
     * 检查sd卡是否可写
     *
     * @return
     */
    public static boolean checkExternalStorageCanWrite() {
        try {
            boolean mouted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            if (mouted) {
                boolean canWrite = new File(Environment.getExternalStorageDirectory().getAbsolutePath()).canWrite();
                if (canWrite) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long getFreeSpace(Context context, String folder) {
//        if (MediaStoreUtils.externalStorageLegacyEnable()) {
//            if (!SDCardUtils.isMounted()) {
//                return 0;
//            }
//            File path = Environment.getExternalStorageDirectory();
//            StatFs stat = new StatFs(path.getPath());
//            long blockSize = stat.getBlockSize();
//            long availableBlocks = stat.getAvailableBlocks();
//            return blockSize * availableBlocks;
//        } else {
//            File path = context.getExternalFilesDir(folder);
//            StatFs stat = new StatFs(path.getPath());
//            long blockSize = stat.getBlockSize();
//            long availableBlocks = stat.getAvailableBlocks();
//            return blockSize * availableBlocks;
//        }

        File path = context.getExternalFilesDir(folder);
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }


    /**
     * 获取SD卡剩余空间
     *
     * @return SD卡剩余空间
     */
    public static long getFreeSpace() {
        if (!SDCardUtils.isMounted()) {
            return 0;
        }
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    public static boolean isApkFileExists(File file) {
        if (file == null) {
            return false;
        }
        return file.exists() && file.isFile() && file.getName().endsWith(".apk");
    }

    public static String getPrivatePath(Context context, String folderName) {
        return context.getExternalFilesDir(folderName).getAbsolutePath() + File.separator;
    }
}

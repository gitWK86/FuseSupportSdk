package cn.houlang.rvds;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.List;

import cn.houlang.rvds.jarvis.Logger;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class AppUtils {
    private AppUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");

    }

    /**
     * 获取应用程序名称
     *
     * @param context 上下文
     */
    public static String getAppName(Context context) {
        if (context == null) {
            return null;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取应用程序版本名称信息
     *
     * @param context 上下文
     */
    public static String getVersionName(Context context) {
        if (context == null) {
            return "1";
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1";
    }

    /**
     * 获取程序的权限
     *
     * @param context 上下文
     */
    public static String[] getPremissions(Context context) {
        if (context == null) {
            return null;
        }
        try {
            PackageInfo packinfo = context.getPackageManager().getPackageInfo(getPackageName(context), PackageManager.GET_PERMISSIONS);
            return packinfo.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取程序包名
     *
     * @param context 上下文
     */
    public static String getPackageName(Context context) {
        if (context == null) {
            return null;
        }
        String packgename = "";
        try {
            packgename = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packgename;
    }

    /**
     * 获得程序版本号
     *
     * @param context 上下文
     * @return 版本号
     */
    public static int getVersionCode(Context context) {
        if (context == null) {
            return 0;
        }
        int versioncode = 0;
        try {
            versioncode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versioncode;
    }

    /**
     * 获取指定文件的版本号
     *
     * @param ctx             上下文
     * @param archiveFilePath 文档路径
     * @return 版本号
     */
    public static int getVersionCode(Context ctx, String archiveFilePath) {
        int versionCode = 0;
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pakinfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        if (pakinfo != null) {
            versionCode = pakinfo.versionCode;
        }
        return versionCode;
    }

    /**
     * 安装 APK。
     *
     * @param context  上下文
     * @param filePath APK 文件路径
     */
    public static void installApk(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 弹框安装apk
     *
     * @param context
     * @param file
     */
    public static void installPackage(Context context, File file) {
        Log.d("rvds", "安装包 ---> " + file);
        if (context == null || file == null) {
            Log.d("rvds", "installPackage context is null");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);

        //判断编译版本是否是6.0以上
        if (context.getApplicationInfo().targetSdkVersion >= 23) {
            Log.d("rvds", "targetSdkVersion >= 23");
            //判断本机系统是否是AndroidN以及更高的版本
            if (Build.VERSION.SDK_INT >= 24) {
                Log.d("rvds", "SDK_INT >= 24");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = RvdsFileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                Logger.d("rvds", "SDK_INT < 24");
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        } else {
            //判断本机系统是否是Android 10以及更高的版本
            if (Build.VERSION.SDK_INT >= 29) {
                Log.d("rvds", "SDK_INT >= 29");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = RvdsFileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                Log.d("rvds", "targetSdkVersion < 23");
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        }
        context.startActivity(intent);
    }

    public static String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am
                .getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

    /**
     * 判断是否已安装
     *
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean isPackageInstalled(Context context, String pkgName) {
        try {
            if (TextUtils.isEmpty(pkgName)) {
                return false;
            } else {
                context.getPackageManager().getPackageInfo(pkgName.trim(), PackageManager.GET_GIDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

package cn.houlang.rvds;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.houlang.rvds.FileUtils;
import cn.houlang.rvds.jarvis.LogRvds;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class PropertiesUtils {
    public static final int ASSETS = 1000;
    public static final int META_INF = 2000;
    private static Map<String, Properties> propertiesMapCache = null;
    /**
     * -1是没有文件，0是默认值
     */
    private static int mMetaInfPackageId = 0;

    public static Properties getProperties(Context context, String fileName, int location) {
        Properties pro_file = null;
        InputStream in = null;
        try {
            pro_file = new Properties();
            switch (location) {
                case ASSETS:
                    in = FileUtils.accessFileFromAssets(context, fileName);
                    break;
                case META_INF:
                    in = FileUtils.accessFileFromMetaInf(context, fileName);
                    break;
                default:
                    LogRvds.d("get Properties obj , param location is error");
                    break;
            }
            if (in != null) {
                pro_file.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pro_file;
    }

//    public static HashMap<String, String> accessProFromAssets(Context context, String fileName, HashMap<String, String> dataMaps) {
//        Properties pro_file = getProperties(context, fileName, ASSETS);
//        for (String key : dataMaps.keySet()) {
//            dataMaps.put(key, pro_file.getProperty(key));
//        }
//        return dataMaps;
//    }
//
//
//    public static HashMap<String, String> accessProFromMetaInf(Context context, String fileName, HashMap<String, String> dataMaps) {
//        Properties pro_file = getProperties(context, fileName, META_INF);
//        for (String key : dataMaps.keySet()) {
//            dataMaps.put(key, pro_file.getProperty(key));
//        }
//        return dataMaps;
//    }

    public static String accessProFromAssets(Context context, String fileName, String key) {
        Properties properties = getProperties(context, fileName, ASSETS);
        if (properties != null) {
            propertiesMapCache.put(fileName, properties);
            return properties.getProperty(key);
        } else {
            return "";
        }
    }

    public static String accessProFromMetaInf(Context context, String fileName, String key) {
        Properties properties = getProperties(context, fileName, META_INF);
        if (properties != null) {
            propertiesMapCache.put(fileName, properties);
            return properties.getProperty(key);
        } else {
            return "";
        }
    }

    public static String getValue4Properties(Context context, String fileName, String key) {
        if (propertiesMapCache == null) {
            propertiesMapCache = new HashMap<>();
        }
        String value = null;

//        //拿包ID，优先从META-INF/里获取拿包id
//        //打包分子包时会创建package_拿包id命名的文件
//        //SDK优先判断META-INF文件夹内有无package_开头的文件，如果有则直接截取文件后缀作为包的拿包id
//        //如果META-INF内未包含上述文件，则还是按正常逻辑走
//        if (key.equals("3KWAN_PackageID")) {
//            if (mMetaInfPackageId > 0) {
//                return mMetaInfPackageId + "";
//            } else if (mMetaInfPackageId == -1) {
//                LogUtils.d("没有参数值，然后从Assets获取...");
//            } else {
//                mMetaInfPackageId = getPackageIdFromMetainf(context);
//                LogUtils.d("从META-INF获取的packageId = " + mMetaInfPackageId);
//                if (mMetaInfPackageId == 0) {
//                    mMetaInfPackageId = -1;
//                }
//                if (mMetaInfPackageId > 0) {
//                    return mMetaInfPackageId + "";
//                }
//                LogUtils.d("没有参数值，然后从Assets获取...");
//            }
//        }

        if (propertiesMapCache.containsKey(fileName)) {
            try {
                value = propertiesMapCache.get(fileName).getProperty(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.d("kkk_tools", "获取缓存数据：" + key + ":" + value);
            return value;
        }


        if (FileUtils.isExistInAssets(context, fileName)) {
            value = accessProFromAssets(context, fileName, key);
        }
//        if (FileUtils.isExistInMetaInf(context, fileName)) {
//            value = accessProFromMetaInf(context, fileName, key);
//        }
        return value;
    }

    /**
     * 从META-INF/里获取拿包id（package_xxx）xxx是拿包ID
     *
     * @param context
     * @return
     */
    public static int getPackageIdFromMetainf(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        String sourceDir = appInfo.sourceDir;
        //LogUtils.d("sourceDir = " + sourceDir);
        ZipFile zipfile = null;
        int packageId = 0;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            a:
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.contains("../")) {
                    break;
                }
                //LogUtils.d("entryName = " + entryName);
                //取META-INF/package_文件
                if (entryName.contains("META-INF/package_")) {
                    // 表示要读取的文件名
                    // 利用ZipInputStream读取文件
                    String packageIdStr = entryName.split("_")[1];
                    //LogUtils.d("packageIdStr = " + packageIdStr);
                    packageId = Integer.parseInt(packageIdStr);
                    break a;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return packageId;
    }

}

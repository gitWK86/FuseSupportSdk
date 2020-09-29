package cn.houlang.support.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * @author #Suyghur.
 * @date 2020/7/8
 */
public class PermissionUtils {


    /**
     * 融合所涉及的权限
     *
     * @return
     */
    public static String[] getFuseSdkDangerousPermissions() {
        String[] permissions = new String[]{
                READ_PHONE_STATE,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
        };
        return permissions;
    }

    /**
     * 融合所涉及的权限，清单有配置才会去申请
     *
     * @return
     */
    public static String[] getFuseSdkDangerousPermissions(Activity activity) {
        //融合默认
        String[] permissions = new String[]{
                READ_PHONE_STATE,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
        };
        String[] temp = null;
        try {
            //清单文件的
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = packageInfo.requestedPermissions;
            temp = filterSamePermissions(permissions, requestedPermissions);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (temp != null && temp.length > 0) {
            return temp;
        }
        return permissions;
    }

    private static String[] filterSamePermissions(String[] a, String[] b) {

        Set<String> same = new HashSet<>();  //用来存放两个数组中相同的元素
        //把数组a中的元素放到Set中，可以去除重复的元素
        //用来存放数组a中的元素
        Set<String> temp = new HashSet<>(Arrays.asList(a));

        for (String s : b) {
            //把数组b中的元素添加到temp中
            //如果temp中已存在相同的元素，则temp.add（b[j]）返回false
            if (!temp.add(s))
                same.add(s);
        }
        int i = 0;
        String[] same2 = new String[same.size()];
        for (String str : same) {
            same2[i] = str;
            i++;
        }
        return same2;
    }

    /**
     * Dialog
     *
     * @param activity
     */
    public static void goSetting(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.getApplicationContext());
        builder.setTitle("设置权限");
        builder.setMessage("权限被拒绝,可能影响功能使用,请尽量在权限管理处授权权限");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", activity.getPackageName(), (String) null));
                } else {
                    intent.setAction("android.intent.action.VIEW");
                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    intent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
                }
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();
    }
}

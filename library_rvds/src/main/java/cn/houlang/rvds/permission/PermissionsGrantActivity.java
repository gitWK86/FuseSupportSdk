package cn.houlang.rvds.permission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import cn.houlang.rvds.ReflectUtils;
import cn.houlang.rvds.jarvis.Logger;

/**
 * @author #Suyghur.
 * @date 2020/7/8
 */
public class PermissionsGrantActivity extends Activity {

    private final static String ACTION_PERMISSION_GRANT_RESULT = "action.permissions.grant.result";
    private final static String EXTRA_REQ_HANDLER_ID = "handler.id"; // 用于记录PermissionHandler的ID，在广播接收器中找回
    private final static String EXTRA_REQ_PERMISSIONS = "permissions";
    private final static String EXTRA_RET_PERMISSIONS_GRANTED = "permissions.granted";
    private final static String EXTRA_RET_PERMISSIONS_DENIED = "permissions.denied";
    private final static String ACTION_MANAGER_WRITE_SETTINGS = "android.settings.action.MANAGE_WRITE_SETTINGS";

    private boolean mResumed;
    private String[] mGrantPermissions; // 待授权的权限
    private String mHandlerId;
    private int mReqCode; // 记录请求码
    private int[] mGrantResults;

    /**
     * 申请授予权限
     *
     * @param context     上下文
     * @param permissions 需要申请的权限
     * @param callback    权限申请回调监听器
     * @throws Exception 申请时产生的异常信息
     */
    public static void grantPermissions(Context context, String[] permissions, PermissionGrantedListener callback) throws Exception {
        if (context == null) {
            throw new Exception("context is null");
        }
        if (permissions == null || permissions.length <= 0) {
            throw new Exception("permissions is null");
        }
        int targetSDKVer = context.getApplicationInfo().targetSdkVersion;
        //只有目标SDK大于等于23的才会有授权
        if (targetSDKVer < 23) {
            Logger.d("targetSdkVersion < 23,不用动态申请");
            callback.onPermissionsResult(permissions, null);
            //低于Android M版的调用直接当权限被授予
            return;
        }
        String[] denied_permissions = getGrantPermissions(context, permissions, PackageManager.PERMISSION_DENIED);
        // 全部被授权，直接回调listener
        if (denied_permissions == null) {
            callback.onPermissionsResult(permissions, null);
            return;
        }
        permissions = denied_permissions;
        String handlerId = generateHandlerId();
        PermissionGrantReceiver receiver = new PermissionGrantReceiver(context, callback, handlerId);
        IntentFilter filter = new IntentFilter(ACTION_PERMISSION_GRANT_RESULT);
        context.registerReceiver(receiver, filter);
        Intent intent = getGrantPermissionsIntent(context, permissions, handlerId);
        context.startActivity(intent);
        if (containWriteSettingsPermission(denied_permissions)) {
            grantSystemWriteSettings(context, callback); //以便让权限界面盖在最上面
        }
    }

    private static Intent getGrantPermissionsIntent(Context context, String[] permissions, String handlerId) {
        Intent intent = new Intent(context, PermissionsGrantActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(EXTRA_REQ_PERMISSIONS, permissions);
        intent.putExtra(EXTRA_REQ_HANDLER_ID, handlerId);
        return intent;
    }

    public static void grantSystemWriteSettings(Context context, PermissionGrantedListener callback) {
        //只有目标SDK大于等于23的才会有授权
        int targetSDKVer = context.getApplicationInfo().targetSdkVersion;
        if (targetSDKVer < 23 || systemCanWrite(context)) {
            // 低于Android M版的调用直接当允许设置
            callback.onPermissionsResult(new String[]{Manifest.permission.WRITE_SETTINGS}, null);
            return;
        }
        Logger.d("bbbbbb");
        Intent intent = new Intent(context, PermissionsGrantActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(ACTION_MANAGER_WRITE_SETTINGS);
        String handlerId = generateHandlerId();
        intent.putExtra(EXTRA_REQ_HANDLER_ID, handlerId);
        context.startActivity(intent);
    }

    private static boolean systemCanWrite(Context context) {
        Object obj = ReflectUtils.callStaticMethod(Settings.System.class, "canWrite", new Class<?>[]{Context.class}, new Object[]{context});
        if (obj != null && obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        } else {
            // 一旦出现反射结果不正常，就当可以写，以免出现后续流程不正常
            return true;
        }
    }

    public static boolean containPermission(String[] permissions, String perm) {
        if (permissions == null) {
            return false;
        }
        for (String s : permissions) {
            if (s.equals(perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查权限是否被允许
     *
     * @param context     上下文
     * @param permissions 权限数组
     * @return true = 全部允许，false = 权限组里有一个或者多个被拒绝
     */
    public static boolean checkAllPermissionsGranted(Context context, String[] permissions) {
        if (context == null) {
            return false;
        }
        int targetSDK = context.getApplicationInfo().targetSdkVersion;
        if (targetSDK < 23) {
            //低于23不用检查权限
            Logger.d("targetSdkVersion < 23,不检查");
            return true;
        } else if (permissions != null && permissions.length > 0) {
            for (String s : permissions) {
                if (isWriteSettingsPermission(s)) {
                    if (checkWriteSettingsResult(context, s, PackageManager.PERMISSION_DENIED)) {
                        return false;
                    }
                } else if (checkSelfPermissionCompat(context, s, PackageManager.PERMISSION_DENIED)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isWriteSettingsPermission(String permission) {
        return Manifest.permission.WRITE_SETTINGS.equals(permission);
    }

    private static boolean containWriteSettingsPermission(String[] permissions) {
        for (String s : permissions) {
            if (isWriteSettingsPermission(s)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkWriteSettingsResult(Context context, String permission, int permission_result) {
        if (!isWriteSettingsPermission(permission)) {
            return false;
        }
        if (permission_result == PackageManager.PERMISSION_GRANTED) {
            return systemCanWrite(context);
        } else {
            return !systemCanWrite(context);
        }
    }


    /**
     * 生成PermissionHandler的ID
     *
     * @return
     */
    private static String generateHandlerId() {
        Random rand = new Random(System.currentTimeMillis());
        long now = System.currentTimeMillis();
        String id = String.format("%x-%x", now, rand.nextLong());
        return id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        //不能全透，否会会出现onRequestPermissionsResult不回调
        lp.alpha = 0.3f;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(0));

        View dummyView = new View(this);
        dummyView.setBackgroundResource(Color.TRANSPARENT);
        setContentView(dummyView);
        final Intent intent = getIntent();
        String action = intent.getAction();
        mHandlerId = intent.getStringExtra(EXTRA_REQ_HANDLER_ID);
        if (ACTION_MANAGER_WRITE_SETTINGS.equals(action)) {
            requestSystemWriteSettings(this);
        } else {
            mGrantPermissions = intent.getStringArrayExtra(EXTRA_REQ_PERMISSIONS);
            if (mGrantPermissions == null || mGrantPermissions.length == 0) {
                finish();
            } else {
                Random rand = new Random();
                mReqCode = rand.nextInt() & 0x7fffffff;
                String[] req_permissions = getShouldShowRequestPermissionRationale(mGrantPermissions);
                if (req_permissions == null || req_permissions.length == 0) { // 不需要显示授权，直接当已授权
                    mGrantResults = new int[mGrantPermissions.length];
                    Arrays.fill(mGrantResults, PackageManager.PERMISSION_GRANTED);
                    finish();
                } else {
                    for (String permission : req_permissions) {//fixed for Meizu OS Version under 23
                        if (Manifest.permission.CAMERA.equals(permission)) {
                            String brand = Build.BRAND;
                            if ("Meizu".equalsIgnoreCase(brand)) {
                                finish();
                                return;
                            }
                        }
                    }
                    requestPermissionsCompat(req_permissions, mReqCode);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Intent intent = getIntent();
        if (!mResumed) {
            mResumed = true;
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        final Intent intent = getIntent();
        if (ACTION_MANAGER_WRITE_SETTINGS.equals(intent.getAction())) {
            Intent resultIntent = new Intent(ACTION_MANAGER_WRITE_SETTINGS);
            resultIntent.setPackage(getPackageName());
            resultIntent.putExtra(EXTRA_REQ_HANDLER_ID, mHandlerId);
            sendBroadcast(resultIntent);
        } else {
            String[] granted_permissions = null; // 已授权的
            String[] denied_permissions = null; // 被拒绝的
            if (mGrantResults == null && mGrantPermissions != null) {
                mGrantResults = new int[mGrantPermissions.length];
                String s;
                for (int k = 0; k < mGrantPermissions.length; k++) {
                    s = mGrantPermissions[k];
                    if (isWriteSettingsPermission(s)) {
                        if (checkWriteSettingsResult(this, s, PackageManager.PERMISSION_GRANTED)) {
                            mGrantResults[k] = PackageManager.PERMISSION_GRANTED;
                        } else {
                            mGrantResults[k] = PackageManager.PERMISSION_DENIED;
                        }
                    } else {
                        if (checkSelfPermissionCompat(this, s, PackageManager.PERMISSION_GRANTED)) {
                            mGrantResults[k] = PackageManager.PERMISSION_GRANTED;
                        } else {
                            mGrantResults[k] = PackageManager.PERMISSION_DENIED;
                        }
                    }
                }
            }
            if (mGrantResults != null) {
                granted_permissions = getGrantPermissions(mGrantPermissions, mGrantResults,
                        PackageManager.PERMISSION_GRANTED);
                denied_permissions = getGrantPermissions(mGrantPermissions, mGrantResults,
                        PackageManager.PERMISSION_DENIED);
            } else {
                denied_permissions = mGrantPermissions;
            }
            Intent resultIntent = new Intent(ACTION_PERMISSION_GRANT_RESULT);
            resultIntent.setPackage(getPackageName());
            if (granted_permissions != null) {
                resultIntent.putExtra(EXTRA_RET_PERMISSIONS_GRANTED, granted_permissions);
            }
            if (denied_permissions != null) {
                resultIntent.putExtra(EXTRA_RET_PERMISSIONS_DENIED, denied_permissions);
            }
            resultIntent.putExtra(EXTRA_REQ_HANDLER_ID, mHandlerId);
            sendBroadcast(resultIntent);
        }
        super.finish();
    }

    private String[] getShouldShowRequestPermissionRationale(String[] permissions) {
        List<String> rets = new ArrayList<String>();
        for (String s : permissions) {
            if (checkSelfPermissionCompat(this, s, PackageManager.PERMISSION_DENIED)) {
                rets.add(s);
            }
        }
        if (rets.size() > 0) {
            String[] sa = new String[rets.size()];
            rets.toArray(sa);
            return sa;
        } else {
            return null;
        }
    }

    /**
     * 这个方法用于判断是否勾选“不再显示“，在permission被PackageManager.PERMISSION_DENIED时，若返回true表示用户主动拒绝的，
     * 若返回false，则表示由用户上次勾选了”不再显示“所引起的
     */
    public static boolean shouldShowRequestPermissionRationaleCompat(Context context, String permission) {
        if (context == null || TextUtils.isEmpty(permission)) {
            return false;
        }
        Object obj = ReflectUtils.callMethod(context, "shouldShowRequestPermissionRationale",
                new Class<?>[]{String.class}, new Object[]{permission});
        if (obj == null) {
            PackageManager pm = context.getPackageManager();
            obj = ReflectUtils.callMethod(pm, "shouldShowRequestPermissionRationale",
                    new Class<?>[]{String.class}, new Object[]{permission});
        }
        if (obj != null && obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        } else {
            return false;
        }
    }

    /**
     * 判断指定的权限是否全部被“不再显示”所拒绝的
     */
    public static boolean shouldAllPermissionAutoDenied(Context context, String[] permissions) {
        if (permissions == null) {
            return false;
        }
        for (String p : permissions) {
            if (shouldShowRequestPermissionRationaleCompat(context, p)) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissionsCompat(String[] permissions, int reqCode) {
        ReflectUtils.callMethod(this, "requestPermissions", new Class<?>[]{String[].class, int.class},
                new Object[]{permissions, reqCode});
    }

    private static boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

    private static boolean checkSelfPermissionCompat(Context context, String permission, int permission_result) {
        Object obj = ReflectUtils.callMethod(context, "checkSelfPermission", new Class<?>[]{String.class},
                new Object[]{permission});
        if (obj != null) {
            if (obj instanceof Integer) {
                return ((Integer) obj).intValue() == permission_result;
            }
        } else {
            if (Manifest.permission.CAMERA.equals(permission)) {//fixed for Meizu OS Version under 23
                String brand = Build.BRAND;
                if ("Meizu".equalsIgnoreCase(brand)) {
                    boolean inCanUse = cameraIsCanUse();
                    if (permission_result == PackageManager.PERMISSION_DENIED && !inCanUse) {
                        return true;
                    } else if (permission_result == PackageManager.PERMISSION_GRANTED && inCanUse) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String[] getGrantPermissions(String[] permissions, int[] grant_results, int check_result) {
        ArrayList<String> rets = new ArrayList<String>();
        int k = 0;
        for (k = 0; k < grant_results.length; k++) {
            if (isWriteSettingsPermission(permissions[k])) {
                if (checkWriteSettingsResult(this, permissions[k], check_result)) {
                    rets.add(permissions[k]);
                }
            } else if (grant_results[k] == check_result) {
                if (k < permissions.length) {
                    rets.add(permissions[k]);
                }
            }
        }
        if (rets.size() > 0) {
            String[] sa = new String[rets.size()];
            rets.toArray(sa);
            return sa;
        } else {
            return null;
        }
    }

    private static String[] getGrantPermissions(Context context, String[] permissions, int check_result) {
        ArrayList<String> rets = new ArrayList<>();
        for (String s : permissions) {
            if (isWriteSettingsPermission(s)) {
                if (checkWriteSettingsResult(context, s, check_result)) {
                    rets.add(s);
                }
            } else {
                if (checkSelfPermissionCompat(context, s, check_result)) {
                    rets.add(s);
                }
            }
        }
        if (rets.size() > 0) {
            String[] sa = new String[rets.size()];
            rets.toArray(sa);
            return sa;
        } else {
            return null;
        }
    }


    /**
     * 权限回调，不能被混淆
     */
    @SuppressLint("LongLogTag")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != mReqCode) {
            Logger.d("onRequestPermissionsResult return, requestCode=" + requestCode + " not equal to " + mReqCode);
        } else {
            try {
                mGrantResults = grantResults;
            } catch (Exception e) {
                Logger.d("onRequestPermissionsResult call error, reason=" + e);
            }
        }
        if (!isFinishing()) {
            finish();
        }
    }

    private static void requestSystemWriteSettings(Context context) {
        Uri selfPackageUri = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS", selfPackageUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static class PermissionGrantReceiver extends BroadcastReceiver implements Runnable {
        private Context mRegisterContext;
        private PermissionGrantedListener mCallback;
        private String mHandlerId;

        private PermissionGrantReceiver(Context context, PermissionGrantedListener callback, String handlerId) {
            mRegisterContext = context;
            mCallback = callback;
            mHandlerId = handlerId;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            String handlerId = intent.getStringExtra(EXTRA_REQ_HANDLER_ID);
            if (handlerId == null) { // 不可能进到这个逻辑
                unRegisterMe();
            } else if (mHandlerId.equals(handlerId)) {
                if (ACTION_MANAGER_WRITE_SETTINGS.equals(action)) {
                } else if (ACTION_PERMISSION_GRANT_RESULT.equals(action)) {
                    final String[] granted_permissions = intent.getStringArrayExtra(EXTRA_RET_PERMISSIONS_GRANTED);
                    final String[] denied_permissions = intent.getStringArrayExtra(EXTRA_RET_PERMISSIONS_DENIED);
                    Handler handler = new Handler(mRegisterContext.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onPermissionsResult(granted_permissions, denied_permissions);
                        }
                    });
                }
                unRegisterMe();
            }
        }

        private void unRegisterMe() {
            Handler handler = new Handler(mRegisterContext.getMainLooper());
            handler.post(this);
        }

        @Override
        public void run() {
            try {
                mRegisterContext.unregisterReceiver(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

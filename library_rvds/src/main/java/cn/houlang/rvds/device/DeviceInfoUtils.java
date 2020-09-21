package cn.houlang.rvds.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.LocaleList;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import cn.houlang.rvds.emulator.EmulatorFiles;
import cn.houlang.rvds.jarvis.LogRvds;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class DeviceInfoUtils {

    private static final int IMEI = 1000;
    private static final int IMSI = 2000;
    private static final int SIM = 3000;
    private static final int SIM_OPERATOR = 4000;
    private static final int SIM_OPERATOR_NAME = 5000;
    private static final int NETWORK_OPERATOR = 6000;
    private static final int NETWORK_OPERATOR_NAME = 7000;
    private static final int DEVICE_SOFTWARE_VERSION = 8000;
    private static final int PHONE_NUMBER = 9000;

    // wifi network
    private static final int NETWORK_WIFI = 1;
    // 4G networks
    private static final int NETWORK_4G = 4;
    // 3G networks
    private static final int NETWORK_3G = 3;
    // 2G networks
    private static final int NETWORK_2G = 2;
    // unknown network
    private static final int NETWORK_UNKNOWN = 5;
    // no network
    private static final int NETWORK_NO = -1;

    /**
     * 获取手机号码
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getPhoneNumber(Context context) {
        return getTelephoneInfo(context, PHONE_NUMBER);
    }

    /**
     * 获取手机厂商
     *
     * @return
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取imei号
     *
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        String imei = getTelephoneInfo(context, IMEI);
        return TextUtils.isEmpty(imei) ? "0" : imei;
    }

    /**
     * 获取imsi号
     *
     * @param context
     * @return
     */
    public static String getImsi(Context context) {
        return getTelephoneInfo(context, IMSI);
    }

    /**
     * 获得手机sim号
     *
     * @param context
     * @return
     */
    public static String getSim(Context context) {
        return getTelephoneInfo(context, SIM);
    }

    /**
     * 获取手机序列号
     *
     * @return 手机序列号
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    /**
     * 获取安卓设备ID
     *
     * @param context 上下文
     */
    public static String getAndroidDeviceId(Context context) {
        if (context == null) {
            return null;
        }
        String id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(id)) {
            id = "";
        }
        return id;
    }

    /**
     * 获取设备mac地址
     * <p>
     * 此方法在6.0以上只能获取到默认值
     * 建议使用{@linkplain DeviceInfoUtils#getMacAddress(Context)}
     * </p>
     *
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        if (context == null) {
            return null;
        }
        String mac = "02:00:00:00:00:00";
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            WifiInfo info = manager.getConnectionInfo();
            if (info != null) {
                mac = info.getMacAddress();
            }
        }
        return mac;
    }

    /**
     * 获取运营商CODE
     *
     * @param context
     * @return
     */
    public static String getSimOperatorCode(Context context) {
        return getTelephoneInfo(context, SIM_OPERATOR);
    }

    /**
     * 获取运营商名称
     *
     * @param context
     * @return
     */
    public static String getSimOperatorName(Context context) {
        return getTelephoneInfo(context, SIM_OPERATOR_NAME);
    }

    public static String getSimOperator2(Context context) {
        String code = getSimOperator(context);
        String operator = null;
        if (code.equals("1")) {
            operator = "中国移动";
        } else if (code.equals("2")) {
            operator = "中国联通";
        } else if (code.equals("3")) {
            operator = "中国电信";
        } else {
            operator = "其他";
        }
        return operator;
    }

    /**
     * 获取运营商
     * 1、移动；2、联通；3、电信；4、其他
     *
     * @param context
     * @return
     */
    public static String getSimOperator(Context context) {
        String code = getSimOperatorCode(context);
        if (code.length() > 0) {
            if (code.equals("46000") || code.equals("46002")
                    || code.equals("46007")) {
                // 中国移动
                //LogUtils.d("中国移动");
                return "1";
            } else if (code.equals("46001") || code.equals("46006")) {
                // 中国联通
                //LogUtils.d("中国联通");
                return "2";
            } else if (code.equals("46003") || code.equals("46005")) {
                // 中国电信
                //LogUtils.d("中国电信");
                return "3";
            } else {
                //LogUtils.d("无或其他");
                return "4";
            }
        }
        return "4";
    }


    /**
     * 获取网络运营商类型
     *
     * @param context
     * @return
     */
    public static String getNetworkOperator(Context context) {
        return getTelephoneInfo(context, NETWORK_OPERATOR);
    }

    /**
     * 获取网络运营商类型名称
     *
     * @param context 上下文
     * @return
     */
    public static String getNetworkOperatorName(Context context) {
        return getTelephoneInfo(context, NETWORK_OPERATOR_NAME);
    }

    /**
     * 获取系统版本
     *
     * @param context 上下文
     * @return
     */
    public static String getDeviceSoftWareVersion(Context context) {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取系统版本API级别
     */
    public static int getSDKAPI() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机型号
     */
    public static String getModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取网络类型
     * -1 = 没网、1 = WIFI、2 = 2G、3 = 3G、4 = 4G、5 = 未知网络
     *
     * @param context
     * @return
     */
    public static int getNetworkType(Context context) {
        if (context == null) {
            return -1;
        }
        int netType = NETWORK_NO;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                int networkType = info.getType();
                if (networkType == ConnectivityManager.TYPE_WIFI) {
                    netType = NETWORK_WIFI;
                } else if (networkType == ConnectivityManager.TYPE_MOBILE) {
                    switch (info.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
//                        case TelephonyManager.NETWORK_TYPE_GSM:
                            netType = NETWORK_2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
//                        case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                            netType = NETWORK_3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
//                        case TelephonyManager.NETWORK_TYPE_IWLAN:
                            netType = NETWORK_4G;
                            break;
                        default:
                            String subtypeName = info.getSubtypeName();
                            if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                    || subtypeName.equalsIgnoreCase("WCDMA")
                                    || subtypeName.equalsIgnoreCase("CDMA2000")) {
                                netType = NETWORK_3G;
                            } else {
                                netType = NETWORK_UNKNOWN;
                            }
                            break;
                    }
                } else {
                    netType = NETWORK_UNKNOWN;
                }
            }
        }


        return netType;
    }

    /**
     * 判断正在使用的网络类型 1、2G；2、3G；3、wifi；4、其他；5、4G
     * 网络类型：0、WiFi；1、移动网络；2、无网络
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getNet(Context context) {
        try {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // 获取网络服务
            // 为空则认为无网络
            if (null == connManager) {
                return "2";
            }
            // 获取网络类型，如果为空，返回无网络
            NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
            if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
                return "2";
            }
            // 判断是否为WIFI
            NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (null != wifiInfo) {
                NetworkInfo.State state = wifiInfo.getState();
                if (null != state) {
                    if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                        return "0";
                    }
                }
            }
//            // 若不是WIFI，则去判断是2G、3G、4G网
//            //TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            int networkType = connManager.getActiveNetworkInfo().getSubtype();
//
//            String subtypeName = connManager.getActiveNetworkInfo().getSubtypeName();
//
//            switch (networkType) {
//                /*
//                 GPRS : 2G(2.5) General Packet Radia Service 114kbps
//                 EDGE : 2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
//                 UMTS : 3G WCDMA 联通3G Universal Mobile Telecommunication System 完整的3G移动通信技术标准
//                 CDMA : 2G 电信 Code Division Multiple Access 码分多址
//                 EVDO_0 : 3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
//                 EVDO_A : 3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
//                 1xRTT : 2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,
//                 HSDPA : 3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps
//                 HSUPA : 3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
//                 HSPA : 3G (分HSDPA,HSUPA) High Speed Packet Access
//                 IDEN : 2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
//                 EVDO_B : 3G EV-DO Rev.B 14.7Mbps 下行 3.5G
//                 LTE : 4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
//                 EHRPD : 3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
//                 HSPAP : 3G HSPAP 比 HSDPA 快些
//                 */
//                // 2G网络
//                case TelephonyManager.NETWORK_TYPE_GPRS:
//                case TelephonyManager.NETWORK_TYPE_CDMA:
//                case TelephonyManager.NETWORK_TYPE_EDGE:
//                case TelephonyManager.NETWORK_TYPE_1xRTT:
//                case TelephonyManager.NETWORK_TYPE_IDEN:
//                    // 3G网络
//                case TelephonyManager.NETWORK_TYPE_EVDO_A:
//                case TelephonyManager.NETWORK_TYPE_UMTS:
//                case TelephonyManager.NETWORK_TYPE_EVDO_0:
//                case TelephonyManager.NETWORK_TYPE_HSDPA:
//                case TelephonyManager.NETWORK_TYPE_HSUPA:
//                case TelephonyManager.NETWORK_TYPE_HSPA:
//                case TelephonyManager.NETWORK_TYPE_EVDO_B:
//                case TelephonyManager.NETWORK_TYPE_EHRPD:
//                case TelephonyManager.NETWORK_TYPE_HSPAP:
//                    // 4G网络
//                case TelephonyManager.NETWORK_TYPE_LTE:
//                default:
//                    if (subtypeName.equalsIgnoreCase("TD-SCDMA") || subtypeName.equalsIgnoreCase("WCDMA") || subtypeName.equalsIgnoreCase("CDMA2000")) {
//                        return "1";
//                    } else if (subtypeName.equalsIgnoreCase("LTE") || subtypeName.equalsIgnoreCase("IWLAN") || subtypeName.equalsIgnoreCase("LTE_CA")) {
//                        return "1";
//                    }
//                    return "1";
//
//            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            //获取网络信息异常，返回默认值2
            return "2";
        }

    }


    @SuppressLint("MissingPermission")
    private static String getTelephoneInfo(Context context, int type) {
        String info = null;
        try {
            if (context == null) {
                return null;
            }
            info = "";
            TelephonyManager phone = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (phone != null) {
                switch (type) {
                    case IMEI:
                        info = phone.getDeviceId();
                        break;
                    case IMSI:
                        info = phone.getSubscriberId();
                        break;
                    case SIM:
                        info = phone.getSimSerialNumber();
                        break;
                    case SIM_OPERATOR:
                        info = phone.getSimOperator();
                        break;
                    case SIM_OPERATOR_NAME:
                        info = phone.getSimOperatorName();
                        break;
                    case NETWORK_OPERATOR:
                        info = phone.getNetworkOperator();
                        break;
                    case NETWORK_OPERATOR_NAME:
                        info = phone.getNetworkOperatorName();
                        break;
                    case DEVICE_SOFTWARE_VERSION:
                        info = phone.getDeviceSoftwareVersion();
                        break;
                    case PHONE_NUMBER:
                        info = phone.getLine1Number();
                        break;
                }
                if (TextUtils.isEmpty(info)) {
                    info = "";
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("3KTools", "get phone info error: " + e.getCause());
        }
        return info;
    }

    /**
     * 判断网络是否可用
     *
     * @param context 上下文
     */
    public static boolean isAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    /**
     * 判断网络是否已连接或正在连接
     *
     * @param context 上下文
     */
    public static boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    public static String getHostIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // 判断wifi是否开启
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            String ip = intToIp(ipAddress);
            return ip;
        } else {// 3g 2g 地址
            return getLocalIpAddress();
        }

    }

    /**
     * 判断网络是否是4G
     *
     * @param context 上下文
     */
    public static boolean is4G(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info.isAvailable() && info.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE;
    }

    /**
     * 判断WIFI是否已连接
     */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 获取蓝牙mac
     */
    @SuppressLint("MissingPermission")
    public static String getBluetoothMac() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String bluetoothMac = "";
        if (bluetoothAdapter != null) {
            bluetoothMac = bluetoothAdapter.getAddress();
            if (TextUtils.isEmpty(bluetoothMac)) {
                bluetoothMac = "";
            }
        }
        return bluetoothMac;
    }

    /**
     * 获取蓝牙名称
     */
    public static String getBluetoothName() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String bluetoothName = "";
        if (bluetoothAdapter != null) {
            bluetoothName = bluetoothAdapter.getName();
            if (TextUtils.isEmpty(bluetoothName)) {
                bluetoothName = "";
            }
        }
        return bluetoothName;
    }

    /**
     * 获取屏幕分辨率
     */
    public static String getDisplay(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int nowWidth = metrics.widthPixels; // 当前屏幕像素
        int nowHeigth = metrics.heightPixels; // 当前屏幕像素

        return nowWidth + "*" + nowHeigth;
    }

    /**
     * 判断是否是模拟器，二次加强判断
     *
     * @return
     */
    private static boolean isEmulator20200326() {
        Log.e("3KTools", "Build.FINGERPRINT: " + Build.FINGERPRINT
                + ", Build.MODEL: " + Build.MODEL
                + ", Build.MANUFACTURER: " + Build.MANUFACTURER
                + ", Build.BRAND: " + Build.BRAND
                + ", Build.DEVICE: " + Build.DEVICE
                + ", Build.PRODUCT: " + Build.PRODUCT);
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
//                || Build.FINGERPRINT.startsWith("unknown") // 魅族MX4: unknown
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MODEL.toLowerCase().contains("mumu")
                || Build.MODEL.toLowerCase().contains("virtual")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    /**
     * 判断是不是模拟器
     */
    public static boolean isEmulator(Context context) {
        // readSysProperty 和 hasEmulatorAdb 会对真机造成误伤
//        boolean b = readSysProperty();
//        Log.e("3KTools", "readSysProperty: " + b);
//        if (!b) {
//            Log.e("3KTools", "FindEmulator.hasEmulatorAdb(): " + FindEmulator.hasEmulatorAdb());
//            if (FindEmulator.hasEmulatorAdb()) {
//                return true;
//            }
//        }


//        boolean b = isEmulator20200326();
        LogRvds.d("新的模拟器方法");
        boolean b = EmulatorFiles.hasEmulatorFile();
        if (!b) {
            return DeviceInfoUtils.isPcKernel();
        }
        return b;
    }

    /**
     * cat /proc/cpuinfo
     * 从cpuinfo中读取cpu架构，检测CPU是否是PC端
     */
    public static boolean isPcKernel() {
        String str = "";
        try {
            Process start = new ProcessBuilder(new String[]{"/system/bin/cat", "/proc/cpuinfo"}).start();
            StringBuffer stringBuffer = new StringBuffer();
            String str2 = "";
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(start.getInputStream(), "utf-8"));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                stringBuffer.append(readLine);
            }
            bufferedReader.close();
            str = stringBuffer.toString().toLowerCase();
            //Log.d("kkk_tools", "检测CPU：\n" +  stringBuffer.toString());
        } catch (IOException e) {
        }
        if (str.contains("intel") || str.contains("amd")) {
            return true;
        }
        return false;
    }


    /**
     * 判断是不是已经root
     */
    public static boolean isRooted() {

        // get from build info
        String buildTags = Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }

        // check if /system/app/Superuser.apk is present
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e1) {
            // ignore
        }

        // try executing commands
        return canExecuteCommand("/system/xbin/which su") || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su") || canExecuteCommand("busybox which su");
    }

    // executes a command on the system
    private static boolean canExecuteCommand(String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String info = in.readLine();
            if (info != null)
                return true;
            return false;
        } catch (Exception e) {
            //do noting
        } finally {
            if (process != null)
                process.destroy();
        }
        return false;
    }

    /**
     * 实时获取CPU当前频率
     *
     * @return
     */
    public static String getCpuFreq() {
        String result = "N/A";
        try {
            FileReader fr = new FileReader(
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取cpu 架构
     *
     * @return
     */
    public static String getCpuAbi() {
        String[] abis = new String[]{};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
        StringBuilder abiStr = new StringBuilder();
        for (String abi : abis) {
            abiStr.append(abi);
            abiStr.append(',');
        }
        return abiStr.toString();
    }

    /**
     * 获取cpu核数
     *
     * @return
     */
    public static String getCpuCount() {
        return Runtime.getRuntime().availableProcessors() + "";
    }


    /**
     * 获取手机运行内存 ram
     *
     * @return
     */
    public static String getRam() {
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstLine != null) {
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }
        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }


    /**
     * 获取所有非系统应用
     * 可以把代码中的判断去掉，获取所有的APP
     */
    public static String getAllApps(Activity activity) {

        String result = "";
        PackageManager pManager = activity.getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //判断是否为非系统预装的应用程序
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                // customs applications
                if (result.isEmpty()) {
                    result = pak.applicationInfo.loadLabel(pManager).toString();
                } else {
                    result = result + "|" + pak.applicationInfo.loadLabel(pManager).toString();
                }
            }
        }
        return result;
    }

    /**
     * 获得手机MAC
     *
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {
        String s = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            s = getMacFromWifiInfo(context);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            s = getMacFromFile();
        } else {
            s = getMachineHardwareAddress();
        }
        if (TextUtils.isEmpty(s)) {
            s = "";
        }
        return s.toLowerCase();
    }

    /**
     * 6.0以上 7.0 以下获取mac
     *
     * @return
     */
    private static String getMacFromFile() {
        String str = "";
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if ("".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return macSerial;
    }

    /**
     * 6.0以下获取mac
     *
     * @param context
     * @return
     */
    private static String getMacFromWifiInfo(Context context) {
        try {
            WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            return info.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 7.0以上获取mac
     * 获取设备HardwareAddress地址
     *
     * @return
     */
    public static String getMachineHardwareAddress() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String hardWareAddress = null;
        NetworkInterface iF = null;
        if (interfaces == null) {
            return null;
        }
        while (interfaces.hasMoreElements()) {
            iF = interfaces.nextElement();
            try {
                if (!iF.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }
                hardWareAddress = bytesToString(iF.getHardwareAddress());
                if (hardWareAddress != null) {
                    break;
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        return hardWareAddress;

    }


    /***
     * byte转为String
     *
     * @param bytes
     * @return
     */
    private static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X:", b));
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    private static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private static String intToIp(int i) {

        return (i & 0xFF) + "." +

                ((i >> 8) & 0xFF) + "." +

                ((i >> 16) & 0xFF) + "." +

                (i >> 24 & 0xFF);

    }


    /**
     * 获取手机语言,兼容7.0以上系统
     *
     * @return String
     */
    public static String getLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else locale = Locale.getDefault();

        return locale.getLanguage();
    }

    /**
     * 获取国家，兼容7.0以上版本
     */
    public static String getCountry() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }

        return locale.getCountry();
    }


}

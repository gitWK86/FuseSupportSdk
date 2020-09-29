package cn.houlang.support.msa;

import android.content.Context;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.supplier.IIdentifierListener;

/**
 * @author #Suyghur.
 * Created on 9/29/20
 */
class MdidHandler {

    static void init(Context context, MsaInitCallback callback, IIdentifierListener iIdentifierListener) {
        int code = MdidSdkHelper.InitSdk(context, true, iIdentifierListener);
        switch (code) {
            case ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT:
                //不支持的设备
                callback.error(ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT, "不支持的设备");
                break;
            case ErrorCode.INIT_ERROR_LOAD_CONFIGFILE:
                //加载配置文件出错
                callback.error(ErrorCode.INIT_ERROR_LOAD_CONFIGFILE, "加载配置文件出错");
                break;
            case ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                //不支持的厂商
                callback.error(ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT, "加载配置文件出错");
                break;
            case ErrorCode.INIT_ERROR_RESULT_DELAY:
                //信息将会延迟返回，获取数据可能在异步线程，取决于设备
                callback.error(ErrorCode.INIT_ERROR_RESULT_DELAY, "信息将会延迟返回，获取数据可能在异步线程，取决于设备");
                break;
            case ErrorCode.INIT_HELPER_CALL_ERROR:
                //反射调用出错
                callback.error(ErrorCode.INIT_HELPER_CALL_ERROR, "反射调用出错");
                break;
        }

    }
}

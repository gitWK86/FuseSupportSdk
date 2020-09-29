package cn.houlang.support.msa;

import android.app.Application;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.supplier.IIdentifierListener;
import com.bun.supplier.IdSupplier;

import cn.houlang.support.jarvis.LogRvds;

/**
 * @author #Suyghur.
 * Created on 9/29/20
 */
public class MsaHandler {

    private static final String VERSION = "1.0.13";

    private static MsaHandler instance;

    public String oaid;
    public String vaid;
    public String aaid;

    public static MsaHandler getInstance() {
        if (instance == null) {
            instance = new MsaHandler();
        }
        return instance;
    }

    private MsaHandler() {
        oaid = "";
        vaid = "";
        aaid = "";
    }

    public void init(Application application, final MsaInitCallback callback) {
        LogRvds.i("初始化MSA , version : " + VERSION);
        JLibraryHandler.init(application);
        MdidHandler.init(application, callback, new IIdentifierListener() {
            @Override
            public void OnSupport(boolean b, IdSupplier idSupplier) {
                if (idSupplier == null) {
                    return;
                }
                if (!idSupplier.isSupported()) {
                    if (callback != null) {
                        callback.error(ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT, "不支持的设备");
                    }
                    return;
                }
                oaid = idSupplier.getOAID();
                vaid = idSupplier.getVAID();
                aaid = idSupplier.getAAID();
                if (callback != null) {
                    callback.success("获取补充设备参数成功");
                }
            }
        });
    }

}

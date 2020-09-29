package cn.houlang.support.msa;

/**
 * @author #Suyghur.
 * Created on 9/29/20
 */
public interface MsaInitCallback {
    void success(String msg);

    void error(int code, String msg);
}

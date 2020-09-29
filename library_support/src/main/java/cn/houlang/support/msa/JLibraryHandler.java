package cn.houlang.support.msa;

import android.app.Application;

import com.bun.miitmdid.core.JLibrary;

/**
 * @author #Suyghur.
 * Created on 9/29/20
 */
class JLibraryHandler {

    static void init(Application application) {
        try {
            JLibrary.InitEntry(application);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

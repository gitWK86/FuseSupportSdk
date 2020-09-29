package cn.houlang.support.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.File;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownloadHelper {

    /**
     * 下载目录
     */

    public static boolean isFileExit(File file) {
        return file.exists() && file.isFile();
    }

    public static HttpClient getHttpClient(Context ctx) {
        String networkTypeName = getNetworkTypeName(ctx);
        if (networkTypeName == null) {
            return null;
        }
        HttpClient client = null;
        HttpParams httpParams = new BasicHttpParams();
        if (isCmwapType(ctx)) {
            HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 60 * 1000);
            HttpConnectionParams.setSocketBufferSize(httpParams, 8 * 1024);
            HttpClientParams.setRedirecting(httpParams, true);
            HttpHost host = new HttpHost("10.0.0.172", 80);
            httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
        } else {
            HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 60 * 1000);
            HttpConnectionParams.setSocketBufferSize(httpParams, 8 * 1024);

        }
        client = new DefaultHttpClient(httpParams) {
            @Override
            protected HttpContext createHttpContext() {
                HttpContext context = new BasicHttpContext();
                context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY, getAuthSchemes());
                context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, getCookieSpecs());
                context.setAttribute(ClientContext.CREDS_PROVIDER, getCredentialsProvider());
                return context;
            }
        };
        return client;
    }

    private static boolean isCmwapType(Context ctx) {
        ConnectivityManager mgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = mgr.getActiveNetworkInfo();
        String extraInfo = activeNetworkInfo.getExtraInfo();
        if (extraInfo == null) {
            return false;
        }
        return "cmwap".equalsIgnoreCase(extraInfo) || "3gwap".equalsIgnoreCase(extraInfo) || "uniwap".equalsIgnoreCase(extraInfo);
    }

    public static String getNetworkTypeName(Context ctx) {
        ConnectivityManager mgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = mgr.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return null;
        }
        String extraInfo = activeNetworkInfo.getExtraInfo();
        if (extraInfo != null && extraInfo.length() > 0) {
            return extraInfo;
        }
        return activeNetworkInfo.getTypeName();
    }

    public static long getFileLength(File file) {
        if (file != null && file.exists() && file.isFile()) {
            return file.length();
        }
        return 0;
    }

    public static boolean deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return true;
    }
//    public static void log(Object obj) {
//        if (DEBUG) {
//            android.util.Log.d(TAG, obj == null ? "null" : obj.toString());
//        }
//    }

    public static boolean checkForDirector(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        File parentFile = file.getParentFile();
        if (parentFile.exists() && parentFile.isDirectory()) {
            return true;
        } else {
            return parentFile.mkdirs();
        }
    }
}

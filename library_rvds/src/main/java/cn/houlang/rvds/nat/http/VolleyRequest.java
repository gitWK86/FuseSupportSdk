package cn.houlang.rvds.nat.http;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import cn.houlang.rvds.JsonUtils;
import cn.houlang.rvds.StrUtils;
import cn.houlang.rvds.encryption.aes.AesUtils;
import cn.houlang.rvds.encryption.rsa.RsaUtils;
import cn.houlang.rvds.jarvis.Logcat;
import cn.houlang.rvds.nat.entity.ResultInfo;
import cn.houlang.rvds.nat.volley.source.AuthFailureError;
import cn.houlang.rvds.nat.volley.source.DefaultRetryPolicy;
import cn.houlang.rvds.nat.volley.source.Request;
import cn.houlang.rvds.nat.volley.source.Response;
import cn.houlang.rvds.nat.volley.source.VolleyError;
import cn.houlang.rvds.nat.volley.source.toolbox.StringRequest;

/**
 * @author #Suyghur.
 * Created on 2020/7/30
 */
public class VolleyRequest {

    /**
     * 超时时间
     */
    private static final int MAX_TIMEOUT = 10 * 1000;

    public static void post(Context context, String url, JSONObject jsonData, IRequestCallback callback) {
        try {
            String time = System.currentTimeMillis() + "";

            //A. 随机产生一个128位(16字节)的AES钥匙，使用AES对数据进行加密得到加密的数据。
            //B. 使用RSA公钥对上面的随机AES钥匙进行加密，得到加密后的AES钥匙。
            //String aesKey = "1234567890abcdef";
            String aesKey = StrUtils.getRandomString(16);
            String content = jsonData.toString();

            String p = AesUtils.encrypt(aesKey, content);
            String ts = RsaUtils.encryptByPublicKey(aesKey);

            String logTag = "logTag " + time + " : ";
            Logcat.logHandler("请求地址 : " + url + "\n");
            Logcat.logHandler("请求参数 : " + jsonData.toString() + "\n");
            Logcat.d("请求参数 : " + jsonData.toString());

            StringBuffer param = new StringBuffer();
            param.append("?p=" + URLEncoder.encode(p));
            param.append("&ts=" + URLEncoder.encode(ts));
            Logcat.d(logTag + " url = " + url + param.toString());
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put("p", p);
            requestMap.put("ts", ts);
            postByVolley(context, url, requestMap, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void get(Context context, String url, final IRequestCallback callback) {
        Logcat.d("url : " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ResultInfo resultInfo = new ResultInfo();
                resultInfo.code = 0;
                resultInfo.msg = "";
                resultInfo.data = response;
                if (null != callback) {
                    callback.onResponse(resultInfo);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (null != callback) {
                    callback.onResponse(getResultInfo(volleyError));
                }
            }
        });
        //关闭缓存策略
        request.setShouldCache(false);
        //设置超时时间
        request.setRetryPolicy(new DefaultRetryPolicy(MAX_TIMEOUT, 1, 1.0f));
        VolleySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    private static void postByVolley(Context context, String url, final HashMap<String, String> requestMap, final IRequestCallback callback) {

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ResultInfo resultInfo = new ResultInfo();
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jsonResult = new JSONObject(response);
                        resultInfo.code = jsonResult.getInt("code");
                        resultInfo.msg = jsonResult.getString("msg");
                        if (JsonUtils.hasJsonKey(jsonResult, "data")) {
                            resultInfo.data = decodeResult(jsonResult.getJSONObject("data"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        resultInfo.code = -1;
                        resultInfo.msg = "解析数据异常";
                    }
                } else {
                    resultInfo.code = -1;
                    resultInfo.msg = "请求接口出错";
                }
                Logcat.logHandler("\n返回内容 : " + resultInfo.toString());
                Logcat.d("postByVolley : " + resultInfo.toString());
                if (null != callback) {
                    callback.onResponse(resultInfo);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (null != callback) {
                    callback.onResponse(getResultInfo(volleyError));
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return requestMap;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(MAX_TIMEOUT, 1, 1.0f));
        VolleySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    private static String decodeResult(JSONObject jsonObject) throws Exception {
        String p = jsonObject.getString("p");
        String ts = jsonObject.getString("ts");
        String aesKey = RsaUtils.decryptByPublicKey(ts);

        return AesUtils.decrypt(aesKey, p);
    }

    private static ResultInfo getResultInfo(VolleyError volleyError) {

        ResultInfo resultInfo = new ResultInfo();
        JSONObject object = new JSONObject();
        try {
            if (volleyError.networkResponse != null) {
                Logcat.d("postByApplicationJson onErrorResponse = " + volleyError.networkResponse.statusCode);
                try {
                    object.put("status_code", volleyError.networkResponse.statusCode);
                    object.put("msg", volleyError.getMessage());
                    object.put("data", "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                object.put("status_code", "400");
                object.put("msg", "网络异常");
                object.put("data", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultInfo.msg = object.toString();
        resultInfo.code = -1;
        return resultInfo;
    }
}

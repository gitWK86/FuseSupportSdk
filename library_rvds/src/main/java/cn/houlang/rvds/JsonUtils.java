package cn.houlang.rvds;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author #Suyghur.
 * Created on 2020/7/30
 */
public class JsonUtils {
    public static boolean hasJsonKey(JSONObject jsonObject, String key) throws JSONException {
        return jsonObject.has(key) && !jsonObject.getString(key).equals("[]");
    }
}

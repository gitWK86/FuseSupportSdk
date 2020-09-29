package cn.houlang.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class StrUtils {

    private StrUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获取字符串3的数位
     *
     * @param s
     * @return
     */
    public static String getEvenStr(String s) {
        try {
            String newStr = new String();
            // get length of string
            int temp = s.length();
            for (int i = 0; i <= (temp - 1) && newStr.length() < 16; i += 3) {
                // rebuild string
                newStr += s.charAt(i);
            }

            return newStr;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static String decodeResult(String result) {
//        // TODO Auto-generated method stub
//        if (TextUtils.isEmpty(result)) {
//            return "";
//        }
//        try {
//            JSONObject object = new JSONObject(result);
//            String p = object.getString("d");
//            String ts = object.getString("ts");
//            String tsMD5 = Md5Utils.encodeByMD5(ts);
//            String key = StrUtils.getEvenStr(tsMD5 + tsMD5);
//            return AesUtils.decrypt(p, key);
//
//        } catch (JSONException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        return "";
//    }

    public static String changeInputStream(InputStream _inputStream, String _encode) {
        ByteArrayOutputStream ops = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        String result = "";
        try {

            if (_inputStream != null) {
                while ((len = _inputStream.read(data)) != -1) {
                    data.toString();
                    ops.write(data, 0, len);
                }
                result = new String(ops.toByteArray(), _encode);
                ops.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 生成随机数字和字母,
     */
    public static String getRandomString(int length) {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val.append((char) (random.nextInt(26) + temp));
            } else {
                val.append(random.nextInt(10));
            }
        }
        return val.toString();
    }

    /**
     * String位数不够后面补0
     *
     * @param str
     * @param strLength
     * @return
     */
    public static String getStringAppendLength(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                //sb.append("0").append(str);//左补0
                sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }

        return str;
    }


    /**
     * 获取url的文件名（包后缀）
     *
     * @param url
     * @return
     */
    public static String getUrlFileName(String url) {
        if (url == null) {
            return null;
        }
        if (url.lastIndexOf("/") != -1) {
            return url.substring(url.lastIndexOf("/") + 1, url.length());
        } else if (url.lastIndexOf("\\") != -1) {
            return url.substring(url.lastIndexOf("\\") + 1, url.length());
        } else {
            return null;
        }
    }
}

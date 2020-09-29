package cn.houlang.support;

import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author #Suyghur.
 * Created on 2020/8/4
 */
public class EditTextUtils {

    public static void filterSpace(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(" ") || source.toString().contentEquals("\n")) {
                    return "";
                } else {
                    return null;
                }
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    /**
     * 设置手机号格式格式
     */
    public static void setPhoneNumberFormat(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        String digits = "0123456789xyzXYZ";
        editText.setKeyListener(DigitsKeyListener.getInstance(digits));
    }

    /**
     * 设置身份证格式
     */
    public static void setIdCardFormat(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
//        String digits = "0123456789xyzXYZ";
//        editText.setKeyListener(DigitsKeyListener.getInstance(digits));
    }

    /*
     * 判断字符串中是否有特殊字符
     * 有返回true 没有false
     *
     * */
    public static boolean specialCharacters(String stb) {
        String regEx = "[ `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(stb);
        return m.find();
    }

    /*
     * 判断身份证是否合法
     * 有返回true 没有false
     *
     * */
    public static boolean filterIdNumber(String stb) {
        String regEx = "^\\d{15}$|^\\d{17}[0-9Xx]$";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(stb);
        return m.find();
    }

}

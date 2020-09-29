package cn.houlang.support;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author #Suyghur.
 * Created on 2020/8/11
 */
public class DateUtils {

    private DateUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 日期格式：yyyy-MM-dd HH:mm:ss
     **/
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式：yyyy-MM-dd HH:mm
     **/
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    /**
     * 日期格式：yyyy-MM-dd
     **/
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * 日期格式：HH:mm:ss
     **/
    public static final String HH_MM_SS = "HH:mm:ss";

    /**
     * 日期格式：HH:mm
     **/
    public static final String HH_MM = "HH:mm";

    /**
     * 获取系统时间
     *
     * @param formate 格式,默认:yyyy-MM-dd HH:mm:ss
     */
    public static String getDate(String formate) {
        if (TextUtils.isEmpty(formate)) {
            formate = YYYY_MM_DD_HH_MM_SS;
        }
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = (calendar.get(Calendar.MONTH) + 1);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String time = year + "/" + (month < 10 ? "0" + month : month) + "/" + (day < 10 ? "0" + day : day) + "  " + (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
        switch (formate) {
            case YYYY_MM_DD_HH_MM_SS:
                time = time;
                break;
            case YYYY_MM_DD_HH_MM:
                time = year + "/" + (month < 10 ? "0" + month : month) + "/" + (day < 10 ? "0" + day : day) + "  " + (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
                break;
            case YYYY_MM_DD:
                time = year + "/" + (month < 10 ? "0" + month : month) + "/" + (day < 10 ? "0" + day : day);
                break;
            case HH_MM_SS:
                time = (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
                break;
            case HH_MM:
                time = (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
                break;
        }
        return time;
    }

    /**
     * 日期转换成Java字符串
     *
     * @param date    需要转换的date
     * @param formate 格式，默认格式：yyyy-MM-dd HH:mm:ss
     */
    public static String dateToStr(Date date, String formate) {
        if (date == null) {
            return null;
        }
        if (TextUtils.isEmpty(formate)) {
            formate = YYYY_MM_DD_HH_MM_SS;
        }
        SimpleDateFormat format = new SimpleDateFormat(formate);
        return format.format(date);
    }

    /**
     * 日期转换成Java字符串
     * 默认格式：yyyy-MM-dd HH:mm:ss
     *
     * @param date 需要转换的date
     */
    public static String dateToStr(Date date) {
        return dateToStr(date, null);
    }

    public static Date parseDate(String s) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = null;
        try {
            d1 = df.parse(s);
        } catch (Exception e) {

        }
        return d1;
    }

    /**
     * 格式化时间，字符串转时间
     *
     * @param dataStr 需要转换的字符串
     * @param format  格式，null则默认为：'yyyy-MM-dd HH:mm:ss'
     * @return 转换的Date
     */
    public static Date getStrByDataTime(String dataStr, String format) {
        if (dataStr == null)
            return new Date();
        if (format == null)
            format = "yyyy-MM-dd HH:mm:ss";
        DateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(dataStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 格式化时间，时间转字符串
     *
     * @param date   null则为当前系统时间
     * @param format 格式，null则默认为：'yyyy-MM-dd HH:mm:ss'
     * @return 字符串格式的日期
     */
    public static String getDateTimeByStr(Date date, String format) {
        if (date == null)
            date = new Date();
        if (format == null)
            format = "yyyy-MM-dd HH:mm:ss";
        return new SimpleDateFormat(format).format(date);
    }
}

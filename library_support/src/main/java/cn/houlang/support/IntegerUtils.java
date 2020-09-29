package cn.houlang.support;

/**
 * @author #Suyghur.
 * Created on 9/29/20
 */
public class IntegerUtils {

    public static int[] integerArray2Int(Integer[] data) {
        int[] result = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i];
        }
        return result;
    }

    public static Integer[] intArray2Integer(int[] data) {
        Integer[] result = new Integer[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i];
        }
        return result;
    }
}

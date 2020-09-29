package cn.houlang.support.download;

import org.json.JSONObject;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class OperateType {
    /**
     * a : 操作符 1-增；2-改；3-查；4-删
     */
    public int crud;

    /**
     * b : 扩展参数
     */
    public String attach0;

    @Override
    public String toString() {
        return "OperateType{" +
                "crud=" + crud +
                ", attach0='" + attach0 + '\'' +
                '}';
    }

    public interface JsonParseInterface {
        JSONObject buildJson();

        void parseJson(JSONObject json);

        String getShortName();
    }
}

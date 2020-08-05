package cn.houlang.rvds.nat.entity;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class ResultInfo {

    public ResultInfo() {
        code = -1;
    }

    /**
     * 0是成功，其他是错误。
     */
    public int code;
    /**
     * 返回信息
     */
    public String msg;
    /**
     * 返回的数据
     */
    public String data;

    @Override
    public String toString() {
        return "ResultInfo{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}

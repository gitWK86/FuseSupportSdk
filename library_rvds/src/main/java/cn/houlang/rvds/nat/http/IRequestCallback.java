package cn.houlang.rvds.nat.http;

import cn.houlang.rvds.nat.entity.ResultInfo;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public interface IRequestCallback {

    void onResponse(ResultInfo resultInfo);
}

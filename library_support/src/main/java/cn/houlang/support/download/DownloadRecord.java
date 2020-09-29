package cn.houlang.support.download;

import java.util.Arrays;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownloadRecord {
    int id;
    String name;
    String url;
    String file;
    long totalSize;
    long downloadedSize;
    /**
     * 下载状态：0 到 4
     */
    int state;
    /**
     * 下载模式：0->覆写 ， 1->续传
     */
    int mode;
    String eTag;
    byte[] image;
    int type;
    String attach1;
    String attach2;
    String attach3;

    @Override
    public String toString() {
        return "DownloadRecord{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", file='" + file + '\'' +
                ", totalSize=" + totalSize +
                ", downloadedSize=" + downloadedSize +
                ", state=" + state +
                ", mode=" + mode +
                ", eTag='" + eTag + '\'' +
                ", image=" + Arrays.toString(image) +
                ", type=" + type +
                ", attach1='" + attach1 + '\'' +
                ", attach2='" + attach2 + '\'' +
                ", attach3='" + attach3 + '\'' +
                '}';
    }
}

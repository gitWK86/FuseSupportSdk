package cn.houlang.support.download;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public interface DownloadJobListener {

    void onDownloadStateChanged(DownloadJob job, int state);

    void onDownloading(DownloadJob job, long downSpeed);
}

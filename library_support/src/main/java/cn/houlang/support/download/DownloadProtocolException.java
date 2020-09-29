package cn.houlang.support.download;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownloadProtocolException extends Exception {
    private static final long serialVersionUID = -2366271190572797882L;

    public DownloadProtocolException() {
        super();
    }

    public DownloadProtocolException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DownloadProtocolException(String detailMessage) {
        super(detailMessage);
    }

    public DownloadProtocolException(Throwable throwable) {
        super(throwable);
    }
}

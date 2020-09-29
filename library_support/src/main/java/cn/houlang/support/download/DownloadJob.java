package cn.houlang.support.download;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.houlang.support.jarvis.LogRvds;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownloadJob {

    public static final int TYPE_APK = 1;
    public static final int TYPE_PIC = 2;

    public static final int MODE_REWRITE = 0;
    public static final int MODE_APPEND = 1;

    /**
     * 初始化
     */
    public static final int STATE_INIT = 0;
    /**
     * 等待网络连接
     */
    public static final int STATE_WAITING = 1;
    /**
     * 开始写文件
     */
    public static final int STATE_START = 2;
    /**
     * 下载暂停
     */
    public static final int STATE_SUSPEND = 3;
    public static final int STATE_COMPLETED = 4;
    /**
     * 下载失败
     */
    public static final int STATE_ABORT = 5;
    /**
     * 取消下载
     */
    public static final int STATE_CANCEL = 6;

    /**
     * 下载模式， 续传为1，默认采取续传功能
     */
    private int mode = 1;

    private String url;
    private File file;
    private List<DownloadJobListener> mListeners = new CopyOnWriteArrayList<DownloadJobListener>();
    private DownloadManager mDownloadManager;

    int state;

    /**
     * 值为0到100
     */
    private int mProgress = -1;
    private long mTotalSize = -1;
    private long mDownloadedSize;
    private String mETag = "";
    /**
     * 3次下载失败重连
     */
//	private int reconnectCount = 0;


    private int id;
    private String name;
    /**
     * 保存图标信息,apk下载在开始前设置对应的游戏的图标
     */
    private byte[] image;

    private int modImage;

    /**
     * 下载文件的类型：apk或图片或其它
     */
    private int type = TYPE_PIC;

    private HttpClient client = null;
    private HttpGet get = null;
    private HttpResponse response = null;

    private Object tag;
    private DownloadTask mTask;

    //扩展参数 图标url
    private String iconUrl;
    //扩展参数 包名
    private String attach2;
    private String attach3;
    private Context context;

    private int postion;//listview中的下载位置
    private long mDownSpeed = 0;

    /**
     * @return the postion
     */
    public int getPostion() {
        return postion;
    }

    /**
     * @param postion the postion to set
     */
    public void setPostion(int postion) {
        this.postion = postion;
    }


    public DownloadJob(Context context, String url, File file, int mode) {
        this.context = context;
        this.url = url;
        this.file = file;
        this.mode = mode;
        state = STATE_INIT;
        mDownloadedSize = 0;
        mTotalSize = -1;
        mDownloadManager = DownloadManager.getInstance(context.getApplicationContext());
    }


    public DownloadJob(Context context, String url, File file, int mode, int type, String string, String packName) {
        this.context = context;
        this.url = url;
        this.file = file;
        this.mode = mode;
        state = STATE_INIT;
        mDownloadedSize = 0;
        mTotalSize = -1;
        this.setName(string);
        this.setType(type);
        this.setAttach2(packName);
        mDownloadManager = DownloadManager.getInstance(context.getApplicationContext());
    }

    public DownloadJob(Context context, DownloadManager mgr, DownloadRecord record) {
        this.context = context;
        this.url = record.url;
        this.file = new File(record.file);
        this.mode = record.mode;
        this.state = record.state;
        this.id = record.id;
        this.name = record.name;
        this.image = record.image;
        this.type = record.type;
        this.mDownloadedSize = record.downloadedSize;
        this.mTotalSize = record.totalSize;
        this.mETag = record.eTag;
        this.mDownloadManager = mgr;
        this.iconUrl = record.attach1;
        this.attach2 = record.attach2;
        this.attach3 = record.attach3;

        if (type == TYPE_APK) {
            addApkListener();
        }
    }

    public Context getContext() {
        return context;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(byte[] data) {
        this.image = data;
        modImage++;
    }

    public int getModImage() {
        return modImage;
    }

    public byte[] getImage() {
        return image;
    }

    public void setType(int type) {
        this.type = type;
        if (type == TYPE_APK) {
            addApkListener();
        }
    }

    public int getType() {
        return type;
    }

    public int getState() {
        return state;
    }

    public void setState(int s) {
        if (this.state == STATE_CANCEL) {
            return;
        }
        if (this.state != s || s == STATE_COMPLETED || s == STATE_ABORT) {
            this.state = s;
            //下载状态发生改变，回调监听器
            if (mListeners.size() > 0) {
                for (DownloadJobListener l : mListeners) {
                    l.onDownloadStateChanged(this, s);
                }
            }
            mDownloadManager.getDownloadDatabase().updateRecord(getDownloadRecord());
            if (type == TYPE_APK) {
                mDownloadManager.notifyObservers();
            }
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public String getUrl() {
        return url;
    }

    public File getFile() {
        return file;
    }

    public void setTag(Object obj) {
        tag = obj;
    }

    public Object getTag() {
        return tag;
    }

    public String getAttach1() {
        return iconUrl;
    }

    public void setAttach1(String attach1) {
        this.iconUrl = attach1;
    }

    public String getAttach2() {
        return attach2;
    }

    public void setAttach2(String attach2) {
        this.attach2 = attach2;
    }

    public String getAttach3() {
        return attach3;
    }

    public void setAttach3(String attach3) {
        this.attach3 = attach3;
    }

    public void setTotalSize(long size) {
        if (size == 0) {
            return;
        }
        this.mTotalSize = size;
    }

    public long getTotalSize() {
        if (mTotalSize == -1) {
            if (response != null) {
                setTotalSize(response.getEntity().getContentLength());
            }
        }
        return mTotalSize;
    }

    public int getProgress() {
        if (mProgress == -1) {
            if (mTotalSize == -1) {
                mTotalSize = getTotalSize();
            }
            mProgress = (int) (mDownloadedSize * 100 / mTotalSize);
        }
        return Math.max(mProgress, 0);
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }

    public void setDownloadedSize(long size, long downSpeed) {
        mDownloadedSize = size;
        int oldProgress = mProgress;
        mProgress = (int) (size * 100 / getTotalSize());
        if (mProgress != oldProgress) {
            if (mListeners.size() > 0) {
                for (DownloadJobListener listener : mListeners) {
                    listener.onDownloading(this, downSpeed);
                }
            }
            if (type == TYPE_APK) {
                mDownloadManager.notifyObservers();
            }
        }
    }

    public long getDownloadedSize() {
        return mDownloadedSize;
    }

    /**
     * @return the downSpeed
     */
    public long getDownSpeed() {
        return mDownSpeed;
    }

    /**
     * @param downSpeed the downSpeed to set
     */
    public void setDownSpeed(int downSpeed) {
        this.mDownSpeed = downSpeed;
    }

    protected DownloadRecord getDownloadRecord() {
        DownloadRecord record = new DownloadRecord();
        record.id = id;
        record.name = name;
        record.mode = mode;
        record.url = url;
        record.file = file.getAbsolutePath();
        record.totalSize = mTotalSize;
        record.downloadedSize = mDownloadedSize;
        record.state = state;
        record.eTag = mETag;
        record.image = image;
        record.type = type;
        record.attach1 = iconUrl;
        record.attach2 = attach2;
        record.attach3 = attach3;
        return record;
    }


    private void addApkListener() {
        for (DownloadJobListener l : mListeners) {
            if (l.getClass() == DownApkListenerImpl.class) {
                return;
            }
        }
        setDownloadJobListener(new DownApkListenerImpl(getContext()));
    }

    /**
     * 开启下载任务
     */
    public void start() {
        if (file == null || url == null) {
            return;
        }
        if (file.exists()) {
            if (type == TYPE_APK) {
                mDownloadManager.getDownloadDatabase().queueDownload(this);
            }
            setState(STATE_COMPLETED);
            return;
        }
        if (mDownloadManager == null) {
            return;
        }
        DownloadJob oldJob = mDownloadManager.containDownloadJob(this);
        if (oldJob != null) {
            int oldState = oldJob.getState();
            switch (oldState) {
                case STATE_WAITING:
                case STATE_START:
                    if (this.mListeners.size() > 0) {
                        for (DownloadJobListener l : mListeners) {
                            oldJob.setDownloadJobListener(l);
                        }
                    }
                    if (tag != null) {
                        oldJob.setTag(tag);
                    }
                    break;
                case STATE_ABORT:
                case STATE_COMPLETED:
                case STATE_SUSPEND:
                    if (mListeners.size() > 0) {
                        for (DownloadJobListener l : mListeners) {
                            oldJob.setDownloadJobListener(l);
                        }
                    }
                    oldJob.state = STATE_ABORT;
                    oldJob.setMode(this.mode);
                    oldJob.setTag(tag);
                    oldJob.restart();
                    break;
            }
        } else {
            if (type == TYPE_APK) {
                if (mDownloadManager.getDownloadDatabase().queueDownload(this)) {
                    execute();
                }
            } else {
                mDownloadManager.addDownloadJob(this);
                execute();
            }
        }
    }


    private void execute() {
        if (!isMounted()) {
            LogRvds.d("请挂载sd卡");
            return;
        }
        setState(STATE_WAITING);
        mTask = new DownloadTask(type, url, file);
        mDownloadManager.addDownloadJob(mTask);
    }

    /**
     * 重新开启任务
     */
    public void restart() {
        switch (state) {
            case STATE_INIT:
            case STATE_CANCEL:
                state = STATE_INIT;
                start();
                break;
            case STATE_SUSPEND:
            case STATE_ABORT:
                state = STATE_INIT;
                execute();
                break;
            case STATE_COMPLETED:
                if (file.exists()) {
                    setState(STATE_COMPLETED);
                } else {
                    if (client != null) {
                        client.getConnectionManager().shutdown();
                    }
                    state = STATE_INIT;
                    execute();
                }
                break;
            case STATE_WAITING:
            case STATE_START:
                break;
        }
    }

    /**
     * 提供给用户暂停下载
     */
    public void pause() {
        switch (state) {
            case STATE_WAITING:
                //在等待队列中
                mDownloadManager.removeDownloadJobFromQueue(mTask);
                break;
            case STATE_START:
                if (client != null) {
                    client.getConnectionManager().shutdown();
                }
                break;
        }
        setState(STATE_SUSPEND);
    }

    /**
     * 取消下载
     */
    public void cancel() {
        switch (state) {
            case STATE_WAITING:
                //在等待队列中
                mDownloadManager.removeDownloadJobFromQueue(mTask);
                break;
            case STATE_START:
                if (client != null) {
                    client.getConnectionManager().shutdown();
                }
                break;
        }
        mDownloadManager.deleteDownload(this);
        setState(STATE_CANCEL);
    }

    public void setDownloadJobListener(DownloadJobListener l) {
        if (l == null || mListeners.contains(l)) {
            return;
        }
        mListeners.add(l);
    }

    public void removeDownloadJobListener(DownloadJobListener l) {
        if (l == null) {
            return;
        }
        if (!mListeners.contains(l)) {
            return;
        }
        mListeners.remove(l);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof DownloadJob) {
            return ((DownloadJob) o).url.equals(this.url) && this.file.equals(((DownloadJob) o).file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode() + file.hashCode();
    }


    @Override
    public String toString() {
        return "DownloadJob [name=" + name + "]";
    }


    /**
     * 后台下载任务
     *
     * @author Administrator
     */
    class DownloadTask implements Runnable {
        private File file;
        private int mType;
        private String url;

        DownloadTask(int type, String url, File file) {
            this.mType = type;
            this.url = url;
            this.file = file;
        }

        public int getType() {
            return mType;
        }

        @Override
        public void run() {
            setState(STATE_START);
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                setState(STATE_ABORT);
                return;
            }
            boolean result = doInBackground();
            //判断是否完成下载
            if (result) {
                setState(STATE_COMPLETED);

            } else {
                setState(STATE_ABORT);
            }
        }

        @Override
        public int hashCode() {
            return url.hashCode() + file.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o instanceof DownloadTask) {
                return this.url.equals(((DownloadTask) o).url) && this.file.equals(((DownloadTask) o).file);
            }
            return false;
        }

        protected Boolean doInBackground() {
            boolean ignoreClose = false;

            if (!DownloadHelper.checkForDirector(file)) {
                return false;
            }
            InputStream in = null;
            FileOutputStream out = null;
            try {
                get = new HttpGet(url);
                client = DownloadHelper.getHttpClient(mDownloadManager.getApplicationContext());
                if (client == null) {
                    return false;
                }
                File tmpFile = new File(file.getAbsolutePath() + ".tmp");
                if (!tmpFile.exists()) {
                    if (!tmpFile.createNewFile()) {
                        throw new DownloadProtocolException("创建文件失败");
                    }
                }
                long tmpFileLength = DownloadHelper.getFileLength(tmpFile);
                if (mode == MODE_APPEND) {
                    //追加模式
                    out = new FileOutputStream(tmpFile, true);
                    setDownloadedSize(tmpFileLength, mDownSpeed);
                    get.setHeader("Range", "bytes=" + tmpFileLength + "-");
                } else {
                    DownloadHelper.deleteFile(tmpFile);
                    if (!tmpFile.createNewFile()) {
                        throw new DownloadProtocolException("创建文件失败");
                    }
                    out = new FileOutputStream(tmpFile);
                }
                response = client.execute(get);
                int statusCode = response.getStatusLine().getStatusCode();
                if (!("" + statusCode).startsWith("2")) {
                    get.abort();
                    if (tmpFile.exists()) {
                        tmpFile.delete();
                    }
                    return false;
                }
                Header[] allHeaders = response.getAllHeaders();
                Map<String, String> headerMaps = new HashMap<String, String>();
                for (Header header : allHeaders) {
                    headerMaps.put(header.getName(), header.getValue());
                }
                if (mode == MODE_APPEND) {
                    // 判断是否支持断点续传
                    String acceptRanges = headerMaps.get("Accept-Ranges");
                    if ("bytes".equals(acceptRanges)) {
                        //网络支持断点
                        String contentRange = headerMaps.get("Content-Range");
                        if (contentRange == null) {
                            throw new DownloadProtocolException("ContentRange为null，终止下载");
                        }
                        String[] result = contentRange.substring(6).split("[-/]");
                        if (result.length == 3 && Long.parseLong(result[0]) == tmpFileLength) {

                            long totoalSize = Long.parseLong(result[2].trim());
                            setTotalSize(totoalSize);
                        } else {
                            throw new DownloadProtocolException("服务器续传协议出错，终止下载");
                        }
                    }
                }

                in = response.getEntity().getContent();
                long contentLength = response.getEntity().getContentLength();
                long startTime = System.currentTimeMillis();
                //只对apk类型进行空间检查
                if (type == TYPE_APK) {
                    //检查sd卡空间
                    long bytesAilable = getAvailableSize();
                    if (bytesAilable <= contentLength) {
                        //忽略关闭流操作
                        ignoreClose = true;
                        throw new DownloadProtocolException("sd卡空间不足");
                    }
                }
                if (mode == MODE_REWRITE) {
                    setDownloadedSize(0, mDownSpeed);
                    setTotalSize(contentLength);
                }

                byte[] buf = new byte[1024 * 4];
                int len = 0;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    long currentTime = System.currentTimeMillis();
                    long usedTime = ((currentTime - startTime) / 1000);
                    if (usedTime > 0) {
                        mDownSpeed = ((mDownloadedSize + len) / usedTime) / 1024;
                    }
                    setDownloadedSize(mDownloadedSize + len, mDownSpeed);
                }

                if (DownloadHelper.getFileLength(tmpFile) != getTotalSize()) {
                    throw new DownloadProtocolException("下载文件不完整");
                }
                //下载完成后重命名文件
                tmpFile.renameTo(file);

                out.flush();

                buf = null;
                return true;
            } catch (DownloadProtocolException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (!ignoreClose && in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (get != null) {
                    get.abort();
                }
                if (client != null) {
                    client.getConnectionManager().shutdown();
                }
            }
            return false;
        }

        private long getAvailableSize() {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            return ((long) stat.getBlockSize()) * (long) stat.getAvailableBlocks();
        }

    }

    public static boolean isMounted() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


}

package cn.houlang.support.download;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.houlang.support.jarvis.LogRvds;
import cn.houlang.support.thread.ThreadManager;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownloadManager {
    private static DownloadManager instance;

    private Context mContext;
    private DownloadDatabase mDb;

    private ArrayList<DownloadObserver> mObservers;

    //保存图片下载记录
    private List<DownloadJob> mDownloadJob = new CopyOnWriteArrayList<DownloadJob>();

    /**
     * 最大并发数
     */
    private int mMaxConcurrentNum = 4;

    private int mMaxConcurrentNumPic = 3;

    private List<DownloadJobQueue> mQueueList = new ArrayList<>(mMaxConcurrentNumPic);

    private ConcurrentLinkedQueue<DownloadJob.DownloadTask> mWaitingQueueApk = new ConcurrentLinkedQueue<DownloadJob.DownloadTask>();
    private ConcurrentLinkedQueue<DownloadJob.DownloadTask> mWaitingQueuePic = new ConcurrentLinkedQueue<DownloadJob.DownloadTask>();

    private DownloadJobQueue mQueueApk1 = new DownloadJobQueue(DownloadJob.TYPE_APK);
    private DownloadJobQueue mQueueApk2 = new DownloadJobQueue(DownloadJob.TYPE_APK);

    private DownloadManager(Context ctx) {
        mContext = ctx;
        mObservers = new ArrayList<>();
        mDb = new DownloadDatabase(ctx, this);

        DownloadJobQueue tmp;
        for (int i = 0; i < mMaxConcurrentNumPic; i++) {
            tmp = new DownloadJobQueue(DownloadJob.TYPE_PIC);
            mQueueList.add(tmp);
        }
    }


    public Context getApplicationContext() {
        return mContext;
    }

    /**
     * 在应用启动的Application中初始化
     *
     * @param ctx
     * @return
     */
    public synchronized static DownloadManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new DownloadManager(ctx);
        }
        return instance;
    }

    public void setMaxconcurrentNum(int num) {
        if (num > 1) {
            this.mMaxConcurrentNum = num;
        }
    }

    public int getMaxconcurrentNum() {
        return mMaxConcurrentNum;
    }

    public DownloadDatabase getDownloadDatabase() {
        return mDb;
    }

    public void addDownloadJob(DownloadJob job) {
        mDownloadJob.add(job);
    }

    /**
     * @param job
     * @return
     */
    public DownloadJob containDownloadJob(DownloadJob job) {
        if (job.getType() == DownloadJob.TYPE_PIC) {
            for (DownloadJob j : mDownloadJob) {
                if (j.equals(job)) {
                    return j;
                }
            }
        } else {
            List<DownloadJob> allDownloadJobs = mDb.getAllDownloadJobs();
            for (DownloadJob j : allDownloadJobs) {
                if (j.equals(job)) {
                    return j;
                }
            }
        }
        return null;
    }

    /**
     * 恢复所有失败的记录
     */
    public void resumeAll() {
        ArrayList<DownloadJob> queuedDownloads = getQueuedDownloads();

        for (DownloadJob job : queuedDownloads) {
            int state = job.getState();
            //如果任务已经暂停或是失败，则需要重新开始
            if (state == DownloadJob.STATE_ABORT || state == DownloadJob.STATE_SUSPEND) {
                job.restart();
            }
        }
    }

    public void resumeSingleJob(DownloadJob job) {
        int state = job.getState();
        if (state == DownloadJob.STATE_ABORT || state == DownloadJob.STATE_SUSPEND) {
            job.restart();
        }
    }

    /**
     * Returns all download jobs.
     *
     * @return
     */
    public List<DownloadJob> getAllDownloads() {
        List<DownloadJob> allJobs = mDb.getAllDownloadJobs();
        return allJobs;
    }

    public DownloadJob getDownloadJob(DownloadJob job) {
        List<DownloadJob> allJobs = mDb.getAllDownloadJobs();
        int len = allJobs.size();
        for (int i = 0; i < len; i++) {
            DownloadJob j = allJobs.get(i);
            if (j.equals(job)) {
                return j;
            }
        }
        return null;
    }

    /**
     * Returns completed download jobs.
     *
     * @return
     */
    public ArrayList<DownloadJob> getCompletedDownloads() {
        return mDb.getCompletedDownloads();
    }

    /**
     * Returns queued download jobs.
     *
     * @return
     */
    public ArrayList<DownloadJob> getQueuedDownloads() {
        return mDb.getQueueDownloads();
    }

    /**
     * Deletes the download job and related files.
     *
     * @param job
     */
    public void deleteDownload(DownloadJob job) {
        //TODO 删除下载任务与对应的文件
        int state = job.getState();
        if (state == DownloadJob.STATE_START || state == DownloadJob.STATE_WAITING) {
            job.pause();
        }
        mDb.removeDownloadJob(job);
        deleteFileFromDisk(job);
        //更新
        notifyObservers();
    }

    /**
     * 删除sd卡上的下载文件
     *
     * @param job
     */
    void deleteFileFromDisk(DownloadJob job) {
        File file = job.getFile();
        File tmpFile = new File(file.getPath() + ".tmp");
        if (tmpFile.exists() && tmpFile.isFile()) {
            tmpFile.delete();
        }
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     * Adds passed object to the download observers list
     *
     * @param observer
     */
    public synchronized void registerDownloadObserver(DownloadObserver observer) {
        mObservers.add(observer);
    }

    /**
     * Removes passed object to the download observers list
     *
     * @param observer
     */
    public synchronized void deregisterDownloadObserver(DownloadObserver observer) {
        mObservers.remove(observer);
    }

    /**
     * Notifies all observers that the state of the downloads has changed.
     */
    public synchronized void notifyObservers() {
        for (DownloadObserver observer : mObservers) {
            observer.onDownloadChanged(this);
        }
    }

    void removeDownloadJobFromQueue(DownloadJob.DownloadTask job) {
        boolean result = false;
        if (job.getType() == DownloadJob.TYPE_APK && mWaitingQueueApk.contains(job)) {
            result = mWaitingQueueApk.remove(job);
        } else if (job.getType() == DownloadJob.TYPE_PIC && mWaitingQueuePic.contains(job)) {
            result = mWaitingQueuePic.remove(job);
        }
        if (result) {
            System.out.println("取消任务成功");

        } else {
            System.out.println("取消任务失败");
        }
    }

    void addDownloadJob(DownloadJob.DownloadTask task) {
        if (task.getType() == DownloadJob.TYPE_APK) {
            //apk
            if (mQueueApk1.size() == 0) {
                mQueueApk1.add(task);
            } else if (mQueueApk2.size() == 0) {
                mQueueApk2.add(task);
            } else {
                mWaitingQueueApk.add(task);
            }
        } else {
            //图片
            boolean addSuccess = false;
            DownloadJobQueue q = null;
            for (int i = 0; i < mMaxConcurrentNumPic; i++) {
                q = mQueueList.get(i);
                if (q.size() == 0) {
                    q.add(task);
                    addSuccess = true;
                    break;
                }
            }
            if (!addSuccess) {
                mWaitingQueuePic.add(task);
            }
        }
        logQueueSize();
    }

    /**
     * 线程安全的拥塞队列
     *
     * @author Administrator
     */
    public class DownloadJobQueue extends ArrayBlockingQueue<DownloadJob.DownloadTask> {
        private int type;

        public DownloadJobQueue(int type) {
            //创建容量为3
            super(3);
            this.type = type;
        }

        private static final long serialVersionUID = -6208360809143168257L;

        @Override
        public boolean add(DownloadJob.DownloadTask e) {
            super.add(e);
            if (size() == 1) {
                ThreadManager.getInstance().execute(new Runnable() {

                    @Override
                    public void run() {
                        DownloadJob.DownloadTask task = null;
                        while ((task = peek()) != null) {
                            task.run();
                            poll();
                            logQueueSize();
                        }
                        if (type == DownloadJob.TYPE_APK) {
                            while ((task = mWaitingQueueApk.poll()) != null) {
                                offer(task);
                                task.run();
                                poll();
                                logQueueSize();
                            }
                        } else {
                            while ((task = mWaitingQueuePic.poll()) != null) {
                                offer(task);
                                task.run();
                                poll();
                                logQueueSize();
                            }
                        }
                    }
                });
            }
            return true;
        }

        @Override
        public String toString() {
            return "[" + DownloadJobQueue.class.getSimpleName() + "]";
        }
    }

    private void logQueueSize() {
        LogRvds.d("apk waiting size -> " + mWaitingQueueApk.size() + " | pic waiting size -> " + mWaitingQueuePic.size());
    }
}

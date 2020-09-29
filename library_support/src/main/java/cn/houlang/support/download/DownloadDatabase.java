package cn.houlang.support.download;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownloadDatabase {
    /**
     * 表名
     */
    private static final String TABLE_NAME = DownloadRecordBuilder.TABLE_NAME_DOWNLOAD;
    private SQLiteDatabase mDb;
    private DownloadManager mDownloadManager;
    /**
     * 数据库版本
     */
    private List<DownloadJob> mJobs = new ArrayList<>();

    public DownloadDatabase(Context context, DownloadManager mgr) {
        mDownloadManager = mgr;
        mDb = new DownloadDatabaseHelper(context).getWritableDatabase();
        loadOldDownloads(context);
    }

    /**
     * 加载数据库中的数据
     */
    private void loadOldDownloads(Context context) {
        if (mDb == null) {
            return;
        }
        Cursor query = mDb.query(TABLE_NAME, null, null, null, null, null, null);
        if (query.moveToFirst()) {
            while (!query.isAfterLast()) {
                DownloadJob job = new DownloadJob(context, mDownloadManager, new DownloadRecordBuilder().build(query));
                switch (job.getState()) {
                    case DownloadJob.STATE_INIT:
                    case DownloadJob.STATE_WAITING:
                    case DownloadJob.STATE_START:
                        job.state = DownloadJob.STATE_ABORT;
                        break;
                }
                mJobs.add(job);
                query.moveToNext();
            }
        }
        query.close();
    }

    /**
     * 添加下载队列
     *
     * @param downloadJob
     * @return 添加成功返回true
     */
    public boolean queueDownload(DownloadJob downloadJob) {
        if (downloadJob.getType() == DownloadJob.TYPE_PIC) {
            mJobs.add(downloadJob);
            return true;
        } else if (addToLibrary(downloadJob.getDownloadRecord())) {
            mJobs.add(downloadJob);
            return true;
        } else {
            return false;
        }
    }


    private boolean addToLibrary(DownloadRecord record) {
        if (mDb == null) {
            // database was not created
            return false;
        }
        // put playlistentry data the table
        ContentValues values = new DownloadRecordBuilder().deconstruct(record);

        String[] whereArgs = {record.url, record.file};
        long row_count = mDb.update(TABLE_NAME, values, "url=? and file=? ",
                whereArgs);
        if (row_count == 0) {
            long row_id = mDb.insert(TABLE_NAME, null, values);
            return row_id != -1l;
        } else {
            return false;
        }
    }

    protected boolean updateRecord(DownloadRecord record) {
        if (mDb == null) {
            return false;
        }
        ContentValues values = new DownloadRecordBuilder().deconstruct(record);
        String[] whereArgs = {record.url, record.file};
        long row_count = mDb.update(TABLE_NAME, values, "url=? and file=? ", whereArgs);
        return row_count >= 1;
    }

    public boolean downloadJobAvailable(DownloadJob job) {
        return false;
    }

    public List<DownloadJob> getAllDownloadJobs() {
        return mJobs;
    }

    /**
     * 清除下载任务
     *
     * @param job
     */
    public void removeDownloadJob(DownloadJob job) {
        if (mDb == null) {
            return;
        }
        mJobs.remove(job);
        String[] whereArgs = {job.getUrl(), job.getFile().getPath()};
        int rows = mDb.delete(TABLE_NAME, "url=? and file=? ", whereArgs);
        if (rows < 1) {
            return;
        }
    }


    public ArrayList<DownloadJob> getQueueDownloads() {
        ArrayList<DownloadJob> list = new ArrayList<DownloadJob>();
        for (DownloadJob job : mJobs) {
            if (job.getState() != DownloadJob.STATE_COMPLETED) {
                list.add(job);
            }
        }
        return list;
    }

    public ArrayList<DownloadJob> getCompletedDownloads() {
        ArrayList<DownloadJob> list = new ArrayList<DownloadJob>();
        for (DownloadJob job : mJobs) {
            if (job.getState() == DownloadJob.STATE_COMPLETED) {
                list.add(job);
            }
        }
        return list;
    }
}

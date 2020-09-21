package cn.houlang.rvds.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.houlang.rvds.jarvis.LogRvds;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownloadDatabaseHelper extends SQLiteOpenHelper {
    /**
     * 数据库名称
     */
    public static final String DATABASE_NAME = "dounload_db";

    public DownloadDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建下载任务表
        db.execSQL("create table if not exists " + DownloadRecordBuilder.TABLE_NAME_DOWNLOAD
                + " ( _id integer primary key autoincrement , "
                + DownloadRecordBuilder.ID + " integer, "
                + DownloadRecordBuilder.NAME + " String, "
                + DownloadRecordBuilder.URL + " String , "
                + DownloadRecordBuilder.FILE + " String , "
                + DownloadRecordBuilder.TOTALSIZE + " long , "
                + DownloadRecordBuilder.DOWNLOADEDSIZE + " long , "
                + DownloadRecordBuilder.STATE + " integer , "
                + DownloadRecordBuilder.MODE + " integer, "
                + DownloadRecordBuilder.ETAG + " String,"
                + DownloadRecordBuilder.IMAGE + " blob,"
                + DownloadRecordBuilder.TYPE + " int, "
                + DownloadRecordBuilder.ATTACH1 + " String, "
                + DownloadRecordBuilder.ATTACH2 + " String, "
                + DownloadRecordBuilder.ATTACH3 + " String "
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogRvds.d("Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DownloadRecordBuilder.TABLE_NAME_DOWNLOAD);
        onCreate(db);
    }
}

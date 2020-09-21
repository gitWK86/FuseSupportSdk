package cn.houlang.rvds.download;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownloadRecordBuilder extends DatabaseBuilder<DownloadRecord> {

    public static final String TABLE_NAME_DOWNLOAD = "download";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String FILE = "file";
    public static final String DOWNLOADEDSIZE = "downloadedsize";
    public static final String TOTALSIZE = "totalsize";
    public static final String STATE = "state";
    public static final String MODE = "mode";
    public static final String ETAG = "eTag";
    public static final String IMAGE = "image";
    public static final String TYPE = "type";
    public static final String ATTACH1 = "attach1";
    public static final String ATTACH2 = "attach2";
    public static final String ATTACH3 = "attach3";


    @Override
    public DownloadRecord build(Cursor cursor) {
        int columnTotalSize = cursor.getColumnIndex(TOTALSIZE);
        int columnUrl = cursor.getColumnIndex(URL);
        int columnFile = cursor.getColumnIndex(FILE);
        int columnDownloaded = cursor.getColumnIndex(STATE);
        int columnDownloadSize = cursor.getColumnIndex(DOWNLOADEDSIZE);
        int columnMode = cursor.getColumnIndex(MODE);
        int columnETag = cursor.getColumnIndex(ETAG);
        int columnId = cursor.getColumnIndex(ID);
        int columnName = cursor.getColumnIndex(NAME);
        int columnImage = cursor.getColumnIndex(IMAGE);
        int columnType = cursor.getColumnIndex(TYPE);

        int columnAttach1 = cursor.getColumnIndex(ATTACH1);
        int columnAttach2 = cursor.getColumnIndex(ATTACH2);
        int columnAttach3 = cursor.getColumnIndex(ATTACH3);

        DownloadRecord record = new DownloadRecord();
        record.url = cursor.getString(columnUrl);
        record.file = cursor.getString(columnFile);
        record.state = cursor.getInt(columnDownloaded);
        record.downloadedSize = cursor.getLong(columnDownloadSize);
        record.totalSize = cursor.getLong(columnTotalSize);
        record.mode = cursor.getInt(columnMode);
        record.eTag = cursor.getString(columnETag);
        record.id = cursor.getInt(columnId);
        record.name = cursor.getString(columnName);
        record.image = cursor.getBlob(columnImage);
        record.type = cursor.getInt(columnType);

        record.attach1 = cursor.getString(columnAttach1);
        record.attach2 = cursor.getString(columnAttach2);
        record.attach3 = cursor.getString(columnAttach3);

        return record;
    }

    @Override
    public ContentValues deconstruct(DownloadRecord record) {
        ContentValues values = new ContentValues();
        values.put(STATE, record.state);
        values.put(TOTALSIZE, record.totalSize);
        values.put(URL, record.url);
        values.put(FILE, record.file);
        values.put(DOWNLOADEDSIZE, record.downloadedSize);
        values.put(MODE, record.mode);
        values.put(ETAG, record.eTag);
        values.put(ID, record.id);
        values.put(NAME, record.name);
        values.put(IMAGE, record.image);
        values.put(TYPE, record.type);
        values.put(ATTACH1, record.attach1);
        values.put(ATTACH2, record.attach2);
        values.put(ATTACH3, record.attach3);
        return values;
    }
}

package cn.houlang.rvds.download;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public abstract class DatabaseBuilder<T> {

    public abstract T build(Cursor cursor);

    public abstract ContentValues deconstruct(T t);
}

package cn.ismiss.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/24
 * sqlite帮助类
 * <p/>
 */
public class MyDBOpenHelper extends SQLiteOpenHelper {


    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     *数据库第一次创建时被调用
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.out.println("第一次创建");
    }

    /**
     * 软件版本号发生改变时调用
     * @param sqLiteDatabase
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        System.out.println("版本号变更");
    }
}

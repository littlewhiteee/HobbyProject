package cn.ismiss.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/22
 * <p/>
 */
public class FileUtils {
    /**
     * 获取根目录
     * @param context
     * @return
     */
    public static String getRootDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 优先获取SD卡根目录[/storage/sdcard0]
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            // 应用缓存目录[/data/data/应用包名/cache]
            return context.getCacheDir().getAbsolutePath();
        }

    }
    public static String getPath(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            try {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return path;
    }
}

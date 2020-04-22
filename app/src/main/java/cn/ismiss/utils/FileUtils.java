package cn.ismiss.utils;

import android.content.Context;
import android.os.Environment;

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
}

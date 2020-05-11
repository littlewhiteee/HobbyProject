package cn.ismiss.utils;

import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.lzy.okgo.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/5/6
 * <p/>
 */
public class HttpDataHelp {

    private static final String TAG = HttpUtils.class.getSimpleName();

    private static final String CharsetName = "UTF-8";
    private static final int TimeOut = 15 * 1000;
    private static AtomicInteger ConnectSize = new AtomicInteger(0);

    public static String getContent(String url, HashMap<String, Object> params) {
        byte[] bs = getBinary(url, params);
        if (bs != null) {
            return new String(bs);
        }
        return null;
    }


    /**
     * 读取数据
     *
     * @param url
     * @param params
     * @return
     */
    public static byte[] getBinary(String url, HashMap<String, Object> params) {
        ConnectSize.getAndIncrement();
        long t = System.currentTimeMillis();
        HttpURLConnection httpUrlConnection = null;
        InputStream inputStream = null;
        byte[] result = null;
        try {
            url = DecodeToUrl(url, params);
            URL urlConnect = new URL(url);
            httpUrlConnection = (HttpURLConnection) urlConnect.openConnection();
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setReadTimeout(TimeOut);
            httpUrlConnection.setConnectTimeout(TimeOut);
            httpUrlConnection.connect();

            if (httpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpUrlConnection.getInputStream();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[24 * 1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                outStream.size();
                result = outStream.toByteArray();//网页的二进制数据
                outStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectSize.getAndDecrement();
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (httpUrlConnection != null) {
                    httpUrlConnection.disconnect();
                }
            } catch (Exception ignored) {
            }
            if (result != null && result.length > 0) {
                float tf = (System.currentTimeMillis() - t) / ((float) 1000);
                Log.v(TAG, "链接：" + url + "  --- 请求总时长 = " + tf + " s");
            }
        }
        return result;
    }


    /**
     * Url转换
     *
     * @param url
     * @param params
     * @return
     */
    public static String DecodeToUrl(String url, HashMap<String, Object> params) {
        String curl = url;
        if ((!TextUtils.isEmpty(curl)) && params != null) {
            curl = url + "?";
            for (String key : params.keySet()) {
                String value = String.valueOf(params.get(key));
                value = getEncode(value);
                curl += (key + "=" + value + "&");
            }
            if (curl.endsWith("&")) {
                curl = curl.substring(0, curl.length() - 1);
            }
        }

        return curl;
    }
    /**
     * 编码转换
     *
     * @param name
     * @return
     */
    public static String getEncode(String name) {
        if (!TextUtils.isEmpty(name)) {
            try {
                name = URLEncoder.encode(name, CharsetName);
            } catch (Exception ignored) {
            }
        }
        return name;
    }


    //发送JSON字符串 如果成功则返回成功标识。
    public static String doJsonPost(String urlPath, String Json) {
        String result = "";
        BufferedReader reader = null;
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            conn.setRequestProperty("Content-Type","application/json;charset=utf-8");
            // 设置接收类型否则返回415错误
            //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
            conn.setRequestProperty("accept","*/*");
            // 往服务器里面发送数据
            if (Json != null && !TextUtils.isEmpty(Json)) {
                byte[] writebytes = Json.getBytes();
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes("UTF-8"));
                outwritestream.flush();
                outwritestream.close();
            }
            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}

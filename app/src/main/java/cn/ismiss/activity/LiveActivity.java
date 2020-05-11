package cn.ismiss.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.tencent.tic.TEduBuild;
import com.tencent.tic.demo.activities.TICVideoRootView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import cn.ismiss.R;
import cn.ismiss.base.BaseActivity;
import cn.ismiss.constant.MyConstant;
import cn.ismiss.utils.HttpDataHelp;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/5/6
 * <p/>
 */
public class LiveActivity extends BaseActivity {

    private String sig = "";
    private TICVideoRootView trtc_root_view;
    private Timer mTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_live_mooc);

        initView();
        initLive();
        initHandler();
        initTimer();
    }


    /**
     * 循环执行
     */
    private void initTimer() {

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                count++;
                System.out.println("我执行了"+count+"次");
            }
        },1000,1000*3);
    }

    /**
     * 延迟任务
     */
    private void initHandler() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(toastRun, 1000*10);
    }

    private Runnable toastRun = new Runnable() {
        @Override
        public void run() {
            System.out.println("定时任务我执行了");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTimer != null){
            mTimer.cancel();
            // 一定设置为null，否则定时器不会被回收
            mTimer = null;
        }
    }

    private void initView() {
        trtc_root_view = findViewById(R.id.trtc_root_view);
    }

    private void initLive() {
        //1.初始化SDK
        TEduBuild.init(this);
        showProgress("正在开启直播间...");
        //2.获取签名
        HashMap<String, Object> baseParams = new HashMap<>();
        HashMap<String, Object> headParams = new HashMap<>();
        HashMap<String, String> bodyParams = new HashMap<>();
        baseParams.clear();
        headParams.clear();
        bodyParams.clear();

        /**
         * 请求头
         */
        headParams.put("deviceType", "03");
        headParams.put("userToken", "");
        headParams.put("deviceId", String.valueOf(System.currentTimeMillis()));
        headParams.put("timestamp", String.valueOf(System.currentTimeMillis()));

        /**
         * 请求参数
         *teacher_3f1a5accdda54b06ab28950ae9fb1e17
         */
        bodyParams.put("account", "teacher_3f1a5accdda54b06ab28950ae9fb1e17");

        baseParams.put("headers", headParams);
        baseParams.put("data", bodyParams);

        /**
         * map转json
         */
        String s = new Gson().toJson(baseParams);

        /**
         * 请求接口,获取sig
         */
        String useSig = HttpDataHelp.doJsonPost(MyConstant.useSigUrl, s);

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(useSig);
            if (jsonObject.getInt("returnCode") == 0) {
                JSONObject returnData = jsonObject.getJSONObject("returnData");
                sig = returnData.getString("data");
                if (!TextUtils.isEmpty(sig)) {
                    /**
                     * 签名不为空,创建房间并加入直播
                     */

                }
            } else {
                showToast(this, "获取签名失败,请检查!");
            }
        } catch (JSONException e) {
            showToast(this, "获取签名失败,请检查!");
            e.printStackTrace();
        } finally {
            dismissProgress();
        }

    }
}

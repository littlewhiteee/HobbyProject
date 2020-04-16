package cn.ismiss.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.moxun.tagcloudlib.view.TagCloudView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

import cn.ismiss.MyRecyclerviewProject;
import cn.ismiss.R;
import cn.ismiss.adapter.ViewTagsAdapter;
import cn.ismiss.bean.GirlBean;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/16
 * <p/>
 */
public class Girl3DViewActivity extends Activity {

    private TagCloudView tagCloudView, tagCloudView2, tagCloudView3, tagCloudView4;
    private ViewTagsAdapter viewTagsAdapter;
    private int page = 1;
    private GirlBean mGirlBean;
    private int allPage;
    private List<GirlBean.DataBean> girlData;
    private SmartRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_3d_view);
        initView();
    }

    private void initView() {
        refresh = (SmartRefreshLayout) findViewById(R.id.refresh);
        tagCloudView = (TagCloudView) findViewById(R.id.tag_cloud);
        tagCloudView2 = (TagCloudView) findViewById(R.id.tag_cloud_2);
        tagCloudView3 = (TagCloudView) findViewById(R.id.tag_cloud_3);
        tagCloudView4 = (TagCloudView) findViewById(R.id.tag_cloud_4);
        initGirlList1(page);

        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initGirlList1(page);
                refreshLayout.finishRefresh();
            }
        });
    }

    private void initGirlList1(final int page) {
        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            if (jsonObject.getInt("status") == 100) {
                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
                                allPage = mGirlBean.getPage_count();
                                girlData = mGirlBean.getData();
                                viewTagsAdapter = new ViewTagsAdapter(girlData);
                                tagCloudView.setAdapter(viewTagsAdapter);
                                Random r = new Random();
                                int page2 = r.nextInt(allPage) + 1;
                                initGirlList2(page2);
                                tagCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                    @Override
                                    public void onItemClick(ViewGroup parent, View view, int position) {
                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });
    }

    private void initGirlList2(final int page) {
        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            if (jsonObject.getInt("status") == 100) {
                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
                                allPage = mGirlBean.getPage_count();
                                girlData = mGirlBean.getData();
                                viewTagsAdapter = new ViewTagsAdapter(girlData);
                                tagCloudView2.setAdapter(viewTagsAdapter);
                                Random r = new Random();
                                int page3 = r.nextInt(allPage) + 1;
                                initGirlList3(page3);
                                tagCloudView2.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                    @Override
                                    public void onItemClick(ViewGroup parent, View view, int position) {
                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });
    }

    private void initGirlList3(final int page) {
        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            if (jsonObject.getInt("status") == 100) {
                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
                                allPage = mGirlBean.getPage_count();
                                girlData = mGirlBean.getData();
                                viewTagsAdapter = new ViewTagsAdapter(girlData);
                                tagCloudView3.setAdapter(viewTagsAdapter);
                                Random r = new Random();
                                int page4 = r.nextInt(allPage) + 1;
                                initGirlList4(page4);
                                tagCloudView3.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                    @Override
                                    public void onItemClick(ViewGroup parent, View view, int position) {
                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });
    }

    private void initGirlList4(final int page) {
        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            if (jsonObject.getInt("status") == 100) {
                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
                                allPage = mGirlBean.getPage_count();
                                girlData = mGirlBean.getData();
                                viewTagsAdapter = new ViewTagsAdapter(girlData);
                                tagCloudView4.setAdapter(viewTagsAdapter);
                                tagCloudView4.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                    @Override
                                    public void onItemClick(ViewGroup parent, View view, int position) {
                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });
    }

    private long mLastClickTime = 0;

    @Override
    public void onBackPressed() {
        long timeMillis = System.currentTimeMillis();
        if (timeMillis - mLastClickTime >= 2000) {
            mLastClickTime = timeMillis;
            Toast.makeText(this, "再次点击退出", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }


}
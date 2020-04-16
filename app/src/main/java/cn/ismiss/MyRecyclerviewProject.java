package cn.ismiss;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.ismiss.adapter.GirlAdapter;
import cn.ismiss.bean.GirlBean;
import cn.ismiss.utils.SpaceItemDecoration;

public class MyRecyclerviewProject extends AppCompatActivity {

    private int page = 1;
    private GirlBean mGirlBean;
    private int allPage;
    private List<GirlBean.DataBean> girlData;
    private SmartRefreshLayout sfGirl;
    private RecyclerView rvGirl;
    private GirlAdapter mGirlAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initView();
        initGirlList(page);

    }

    private void initView() {
        sfGirl = findViewById(R.id.sf_girl);
        rvGirl = findViewById(R.id.rv_girl);
        rvGirl.setLayoutManager(new GridLayoutManager(this, 3));
        rvGirl.addItemDecoration(new SpaceItemDecoration(10, 3));
        /**
         * 分割线,按需添加
         */
//        rvGirl.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));
//        rvGirl.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL));

        sfGirl.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (page < allPage) {
                    page++;
                    initGirlList(page);
                    refreshLayout.finishLoadMore();
                } else {
                    refreshLayout.finishLoadMoreWithNoMoreData();
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                initGirlList(page);
                refreshLayout.finishRefresh();
            }
        });
    }


    private void initGirlList(final int page) {
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
                                if (page == 1) {
                                    girlData = mGirlBean.getData();
                                    bindAdapter(girlData);
                                } else {
                                    mGirlAdapter.addData(mGirlBean.getData());
                                }
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

    private void bindAdapter(final List<GirlBean.DataBean> girlData) {
        mGirlAdapter = new GirlAdapter(R.layout.item_girl, girlData);
        rvGirl.setAdapter(mGirlAdapter);
        mGirlAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Toast.makeText(MyRecyclerviewProject.this,"查看大图",Toast.LENGTH_SHORT).show();

            }
        });
    }

}

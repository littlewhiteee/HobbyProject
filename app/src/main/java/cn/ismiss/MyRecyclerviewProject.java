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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cn.ismiss.adapter.GirlAdapter;
import cn.ismiss.bean.GirlBean;
import cn.ismiss.bean.JsoupImageVO;
import cn.ismiss.utils.DBOpenHelper;
import cn.ismiss.utils.JsoupBaiduPic;
import cn.ismiss.utils.SpaceItemDecoration;

public class MyRecyclerviewProject extends AppCompatActivity {

    private int page = 1;
    private GirlBean mGirlBean;
    private boolean haveMore = true;
    private List<GirlBean.DataBean> girlData;
    private SmartRefreshLayout sfGirl;
    private RecyclerView rvGirl;
    private GirlAdapter mGirlAdapter;
    private int i = 1;
    private PreparedStatement ps = null;//操作整合sql语句的对象
    private List<String> URLS = new ArrayList<>();
    private List<String> moreUrl = new ArrayList<>();
    private List<String> worksList;
    private int startLine = 0;
    private int pageSize = 12;
    private int jsoupPage = 1;
    private int allPage;

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
        initUrl();
        //initGirlList(page);
        /**
         * 0-10
         * 11-10
         * 21-10
         *
         * 0-12
         * 13-12
         * 25-12
         *
         */
        QueryMysql(startLine, pageSize);


    }

    private void initUrl() {
        OkGo.<String>get("http://121.36.55.198/info.txt")
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Toast.makeText(MyRecyclerviewProject.this, "服务器路径不正确", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        worksList = new ArrayList<>();
                        String[] lines = response.body().split("\n");
                        if (lines.length > 0) {
                            for (String line : lines) {
                                worksList.add(line);

                            }


                        }

                    }
                });

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
                if (haveMore) {
                    /**
                     * 0-12
                     * 13-12
                     * 25-12
                     * 37-12
                     */
                    page++;
                    moreUrl.clear();
                    QueryMysql(pageSize * (page - 1) + 1, pageSize);
                    refreshLayout.finishLoadMore();

                } else {
                    refreshLayout.finishLoadMoreWithNoMoreData();
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                haveMore = true;
                moreUrl.clear();
                QueryMysql(startLine, pageSize);
                refreshLayout.finishRefresh();
            }
        });
    }


    /**
     * 数据来源干货集中营,小姐姐不符合我的审美,废弃掉
     *
     * @param
     */
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
                                    // bindAdapter(girlData);
                                } else {
                                    //  mGirlAdapter.addData(mGirlBean.getData());
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

    private void bindAdapter(final List<String> girlData) {
        mGirlAdapter = new GirlAdapter(R.layout.item_girl, girlData);
        rvGirl.setAdapter(mGirlAdapter);
        mGirlAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<JsoupImageVO> jennie = JsoupBaiduPic.findImage("Jennie", jsoupPage);
                        insertUserData(jennie);

                    }
                }).start();

            }
        });
    }

    /**
     * 查询数据库
     */
    private void QueryMysql(final int startLine, final int pageSIze) {
        if (startLine == 0) {
            URLS.clear();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                conn = (Connection) DBOpenHelper.getConn();
                String sql = "select url from tb_little_sister LIMIT " + startLine + ',' + pageSIze;
                System.out.println("sql语句" + sql);
                Statement st;
                try {
                    st = (Statement) conn.createStatement();
                    final ResultSet rs = st.executeQuery(sql);
                    while (rs.next()) {
                        if (startLine == 0) {
                            URLS.add(rs.getString(1));
                        } else {
                            moreUrl.add(rs.getString(1));
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (startLine == 0) {
                                haveMore = true;
                                bindAdapter(URLS);
                            } else {
                                if (moreUrl.size() == 0) {
                                    haveMore = false;
                                } else {
                                    if (mGirlAdapter != null) {
                                        mGirlAdapter.addData(moreUrl);
                                    }
                                }

                            }

                        }
                    });
                    st.close();
                    conn.close();
                } catch (SQLException e) {
                    haveMore = false;
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 插入数据到数据库
     */
    private void insertUserData(final List<JsoupImageVO> jennie) {
        //连接数据库进行操作需要在主线程操作
        System.out.println("爬取百度图库第" + jsoupPage + "页");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //连接数据库进行操作需要在主线程操作
                Connection conn = null;
                conn = (Connection) DBOpenHelper.getConn();
                String sql = "INSERT INTO tb_little_sister (id,name,url) VALUES (?,?,?)";
                try {
                    boolean closed = conn.isClosed();
                    if ((conn != null) && (!closed)) {
                        for (i = 0; i < jennie.size(); i++) {
                            ps = (PreparedStatement) conn.prepareStatement(sql);
                            String id = null;
                            String name = jennie.get(i).getName();
                            String url = jennie.get(i).getUrl();
                            ps.setString(1, id);//第一个参数 name 规则同上
                            ps.setString(2, name);//第二个参数 phone 规则同上
                            ps.setString(3, url);//第三个参数 content 规则同上
                            //      int result = ps.executeUpdate();//返回1 执行成功
                        }
                        conn.close();
                        jsoupPage++;
                        List<JsoupImageVO> jennie = JsoupBaiduPic.findImage("Jennie", jsoupPage);
                        insertUserData(jennie);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

}

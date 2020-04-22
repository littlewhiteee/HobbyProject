package cn.ismiss.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.moxun.tagcloudlib.view.TagCloudView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.ismiss.MyRecyclerviewProject;
import cn.ismiss.R;
import cn.ismiss.adapter.ViewTagsAdapter;
import cn.ismiss.bean.GirlBean;
import cn.ismiss.permissions.CheckPermissionsListener;
import cn.ismiss.utils.DBOpenHelper;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/16
 * <p/>
 */
public class Girl3DViewActivity extends Activity implements CheckPermissionsListener {

    private TagCloudView tagCloudView, tagCloudView2, tagCloudView3, tagCloudView4, tagCloudView5;
    private ViewTagsAdapter viewTagsAdapter;
    private int page = 1;
    private GirlBean mGirlBean;
    private int allPage;
    private List<GirlBean.DataBean> girlData;
    private List<String> URLS = new ArrayList<>();
    private SmartRefreshLayout refresh;
    private int startLine = 0;
    private int pageSize = 12;
    private static final int REQUEST_CODE = 2333;
    private CheckPermissionsListener mListener;
    protected final String[] neededPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

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
        askPermissions();
    }

    /**
     * 权限申请
     */
    private void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(this, neededPermissions, this);
        }
    }

    /**
     * 申请动态权限
     *
     * @param activity
     * @param permissions
     * @param listener
     */
    public void requestPermissions(Activity activity, String[] permissions, CheckPermissionsListener listener) {
        if (activity == null) return;
        mListener = listener;
        List<String> deniedPermissions = findDeniedPermissions(activity, permissions);
        if (!deniedPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        } else {
            mListener.onGranted();
        }
    }

    /**
     * 查找未通过权限
     *
     * @param activity
     * @param permissions
     * @return
     */
    private List<String> findDeniedPermissions(Activity activity, String... permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    private void initView() {
        refresh = (SmartRefreshLayout) findViewById(R.id.refresh);
        tagCloudView = (TagCloudView) findViewById(R.id.tag_cloud);
        tagCloudView2 = (TagCloudView) findViewById(R.id.tag_cloud_2);
        tagCloudView3 = (TagCloudView) findViewById(R.id.tag_cloud_3);
        tagCloudView4 = (TagCloudView) findViewById(R.id.tag_cloud_4);
        tagCloudView5 = (TagCloudView) findViewById(R.id.tag_cloud_5);
        QueryMysql(startLine,pageSize);

        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                QueryMysql(startLine,pageSize);
                refreshLayout.finishRefresh();
            }
        });
    }

//    private void initGirlList1(final int page) {
//        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
//                .execute(new StringCallback() {
//
//                    @Override
//                    public void onStart(Request<String, ? extends Request> request) {
//                        super.onStart(request);
//                    }
//
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response.body());
//                            if (jsonObject.getInt("status") == 100) {
//                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
//                                allPage = mGirlBean.getPage_count();
//                                girlData = mGirlBean.getData();
//                                viewTagsAdapter = new ViewTagsAdapter(girlData);
//                                tagCloudView.setAdapter(viewTagsAdapter);
//                                Random r = new Random();
//                                int page2 = r.nextInt(allPage) + 1;
//                                initGirlList2(page2);
//                                tagCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
//                                    @Override
//                                    public void onItemClick(ViewGroup parent, View view, int position) {
//                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
//                                    }
//                                });
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });
//    }
//
//    private void initGirlList2(final int page) {
//        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
//                .execute(new StringCallback() {
//
//                    @Override
//                    public void onStart(Request<String, ? extends Request> request) {
//                        super.onStart(request);
//                    }
//
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response.body());
//                            if (jsonObject.getInt("status") == 100) {
//                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
//                                allPage = mGirlBean.getPage_count();
//                                girlData = mGirlBean.getData();
//                                viewTagsAdapter = new ViewTagsAdapter(girlData);
//                                tagCloudView2.setAdapter(viewTagsAdapter);
//                                Random r = new Random();
//                                int page3 = r.nextInt(allPage) + 1;
//                                initGirlList3(page3);
//                                tagCloudView2.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
//                                    @Override
//                                    public void onItemClick(ViewGroup parent, View view, int position) {
//                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
//                                    }
//                                });
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });
//    }
//
//    private void initGirlList3(final int page) {
//        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
//                .execute(new StringCallback() {
//
//                    @Override
//                    public void onStart(Request<String, ? extends Request> request) {
//                        super.onStart(request);
//                    }
//
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response.body());
//                            if (jsonObject.getInt("status") == 100) {
//                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
//                                allPage = mGirlBean.getPage_count();
//                                girlData = mGirlBean.getData();
//                                viewTagsAdapter = new ViewTagsAdapter(girlData);
//                                tagCloudView3.setAdapter(viewTagsAdapter);
//                                Random r = new Random();
//                                int page4 = r.nextInt(allPage) + 1;
//                                initGirlList4(page4);
//                                tagCloudView3.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
//                                    @Override
//                                    public void onItemClick(ViewGroup parent, View view, int position) {
//                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
//                                    }
//                                });
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });
//    }
//
//    private void initGirlList4(final int page) {
//        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
//                .execute(new StringCallback() {
//
//                    @Override
//                    public void onStart(Request<String, ? extends Request> request) {
//                        super.onStart(request);
//                    }
//
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response.body());
//                            if (jsonObject.getInt("status") == 100) {
//                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
//                                allPage = mGirlBean.getPage_count();
//                                girlData = mGirlBean.getData();
//                                viewTagsAdapter = new ViewTagsAdapter(girlData);
//                                tagCloudView4.setAdapter(viewTagsAdapter);
//                                Random r = new Random();
//                                int page5 = r.nextInt(allPage) + 1;
//                                initGirlList5(page5);
//                                tagCloudView4.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
//                                    @Override
//                                    public void onItemClick(ViewGroup parent, View view, int position) {
//                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
//                                    }
//                                });
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });
//    }
//
//    private void initGirlList5(final int page) {
//        OkGo.<String>get("https://gank.io/api/v2/data/category/Girl/type/Girl/page/" + page + "/count/12")
//                .execute(new StringCallback() {
//
//                    @Override
//                    public void onStart(Request<String, ? extends Request> request) {
//                        super.onStart(request);
//                    }
//
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response.body());
//                            if (jsonObject.getInt("status") == 100) {
//                                mGirlBean = new Gson().fromJson(response.body(), GirlBean.class);
//                                allPage = mGirlBean.getPage_count();
//                                girlData = mGirlBean.getData();
//                                viewTagsAdapter = new ViewTagsAdapter(girlData);
//                                tagCloudView5.setAdapter(viewTagsAdapter);
//                                tagCloudView5.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
//                                    @Override
//                                    public void onItemClick(ViewGroup parent, View view, int position) {
//                                        startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
//                                    }
//                                });
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });
//    }

    private void QueryMysql(final int startLine, final int pageSize ) {
        //连接数据库进行操作需要在主线程操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                //连接数据库进行操作需要在主线程操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "select url from tb_little_sister LIMIT " + startLine + ',' + pageSize;
                        Statement st;
                        try {
                            URLS.clear();
                            st = (Statement) conn.createStatement();
                            final ResultSet rs = st.executeQuery(sql);
                            while (rs.next()) {
                                URLS.add(rs.getString(1));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewTagsAdapter = new ViewTagsAdapter(URLS);
                                    tagCloudView.setAdapter(viewTagsAdapter);
                                    Random r = new Random();
                                    int page2 = r.nextInt(5) + 1;
                                    int page2StartLine = page2 * pageSize;
                                    QueryMysql2(page2StartLine,pageSize);
                                    tagCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                        @Override
                                        public void onItemClick(ViewGroup parent, View view, int position) {
                                            startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                        }
                                    });
                                }
                            });
                            st.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }).start();
    }
    private void QueryMysql2(final int startLine, final int pageSize ) {
        //连接数据库进行操作需要在主线程操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                //连接数据库进行操作需要在主线程操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "select url from tb_little_sister LIMIT " + startLine + ',' + pageSize;
                        Statement st;
                        try {
                            URLS.clear();
                            st = (Statement) conn.createStatement();
                            final ResultSet rs = st.executeQuery(sql);
                            while (rs.next()) {
                                URLS.add(rs.getString(1));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewTagsAdapter = new ViewTagsAdapter(URLS);
                                    tagCloudView2.setAdapter(viewTagsAdapter);
                                    Random r = new Random();
                                    int page3 = r.nextInt(5) + 1;
                                    int page3StartLine = page3 * pageSize;
                                    QueryMysql3(page3StartLine,pageSize);


                                    tagCloudView2.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                        @Override
                                        public void onItemClick(ViewGroup parent, View view, int position) {
                                            startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                        }
                                    });
                                }
                            });
                            st.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }).start();
    }
    private void QueryMysql3(final int startLine, final int pageSize ) {
        //连接数据库进行操作需要在主线程操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                //连接数据库进行操作需要在主线程操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "select url from tb_little_sister LIMIT " + startLine + ',' + pageSize;
                        Statement st;
                        try {
                            URLS.clear();
                            st = (Statement) conn.createStatement();
                            final ResultSet rs = st.executeQuery(sql);
                            while (rs.next()) {
                                URLS.add(rs.getString(1));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewTagsAdapter = new ViewTagsAdapter(URLS);
                                    tagCloudView3.setAdapter(viewTagsAdapter);
                                    Random r = new Random();
                                    int page4 = r.nextInt(5) + 1;
                                    int page4StartLine = page4 * pageSize;
                                    QueryMysql4(page4StartLine,pageSize);


                                    tagCloudView3.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                        @Override
                                        public void onItemClick(ViewGroup parent, View view, int position) {
                                            startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                        }
                                    });
                                }
                            });
                            st.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }).start();
    }
    private void QueryMysql4(final int startLine, final int pageSize ) {
        //连接数据库进行操作需要在主线程操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                //连接数据库进行操作需要在主线程操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "select url from tb_little_sister LIMIT " + startLine + ',' + pageSize;
                        Statement st;
                        try {
                            URLS.clear();
                            st = (Statement) conn.createStatement();
                            final ResultSet rs = st.executeQuery(sql);
                            while (rs.next()) {
                                URLS.add(rs.getString(1));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewTagsAdapter = new ViewTagsAdapter(URLS);
                                    tagCloudView4.setAdapter(viewTagsAdapter);
                                    Random r = new Random();
                                    int page5 = r.nextInt(5) + 1;
                                    int page5StartLine = page5 * pageSize;
                                    QueryMysql5(page5StartLine,pageSize);


                                    tagCloudView4.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                        @Override
                                        public void onItemClick(ViewGroup parent, View view, int position) {
                                            startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                        }
                                    });
                                }
                            });
                            st.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }).start();
    }
    private void QueryMysql5(final int startLine, final int pageSize ) {
        //连接数据库进行操作需要在主线程操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                //连接数据库进行操作需要在主线程操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "select url from tb_little_sister LIMIT " + startLine + ',' + pageSize;
                        Statement st;
                        try {
                            URLS.clear();
                            st = (Statement) conn.createStatement();
                            final ResultSet rs = st.executeQuery(sql);
                            while (rs.next()) {
                                URLS.add(rs.getString(1));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewTagsAdapter = new ViewTagsAdapter(URLS);
                                    tagCloudView5.setAdapter(viewTagsAdapter);
                                    Random r = new Random();
                                    int page2 = r.nextInt(5) + 1;
                                    int page2StartLine = page2 * pageSize;


                                    tagCloudView5.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                        @Override
                                        public void onItemClick(ViewGroup parent, View view, int position) {
                                            startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                        }
                                    });
                                }
                            });
                            st.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }).start();
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


    @Override
    public void onGranted() {

    }

    @Override
    public void onDenied(List<String> permissions) {
        Toast.makeText(this, "权限被禁用，请到设置里打开", Toast.LENGTH_SHORT).show();
    }
}
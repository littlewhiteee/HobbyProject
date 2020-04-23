package cn.ismiss.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.moxun.tagcloudlib.view.TagCloudView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.ismiss.MyRecyclerviewProject;
import cn.ismiss.R;
import cn.ismiss.adapter.ViewTagsAdapter;
import cn.ismiss.base.BaseActivity;
import cn.ismiss.bean.GirlBean;
import cn.ismiss.bean.JsoupImageVO;
import cn.ismiss.permissions.CheckPermissionsListener;
import cn.ismiss.utils.DBOpenHelper;
import cn.ismiss.utils.JsoupBaiduPic;
import cn.ismiss.view.MyDialog;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/16
 * <p/>
 */
public class Girl3DViewActivity extends BaseActivity implements CheckPermissionsListener {

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
    private PreparedStatement ps = null;//操作整合sql语句的对象
    private int jsoupPage = 1;
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
    private EditText etKey;
    private Dialog adminDialog;

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
        QueryMysql(startLine, pageSize);

        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                showAdminDialog();
                refreshLayout.finishRefresh();
            }
        });
    }


    /**
     * 管理端
     */
    private void showAdminDialog() {
        View view = View.inflate(this, R.layout.dialog_admin, null);
        adminDialog = new MyDialog(this, 0, 0, view, R.style.DialogTheme);
        adminDialog.setCanceledOnTouchOutside(false);
        etKey = view.findViewById(R.id.et_key);
        view.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QueryMysql(startLine, pageSize);
                adminDialog.dismiss();
            }
        });
        view.findViewById(R.id.tv_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etKey.getText().toString().trim())) {
                    Toast.makeText(Girl3DViewActivity.this, "请输入爬取关键字", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<JsoupImageVO> jennie = JsoupBaiduPic.findImage(etKey.getText().toString().trim(), jsoupPage);
                            if (jennie.size() > 0) {
                                insertUserData(jennie);    //图片地址插入到数据库
                            } else {
                                System.out.println("没有抓取到数据");
                            }
                        }
                    }).start();
                    adminDialog.dismiss();
                }

            }
        });
        view.findViewById(R.id.tv_clean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(Girl3DViewActivity.this)
                        .setTitle("不可逆操作")
                        .setMessage("您确定以及肯定要清空当前数据库吗")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dropTable();
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });
        adminDialog.setContentView(view);
        Window dialogWindow = adminDialog.getWindow();
        WindowManager mm = this.getWindowManager();
        Display d = mm.getDefaultDisplay(); // 获取屏幕宽、高度
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.50); // 高度设置为屏幕的0.6，根据实际情况调整
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.65，根据实际情况调整
        dialogWindow.setAttributes(p);
        adminDialog.show();
    }

    /**
     * 清空表数据
     */
    private void dropTable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress("正在删除数据...");
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                //连接数据库进行操作需要在主线程操作
                Connection conn = null;
                conn = (Connection) DBOpenHelper.getConn();
                String sql = "delete from tb_little_sister";
                try {
                    boolean closed = conn.isClosed();
                    if ((conn != null) && (!closed)) {
                        ps = (PreparedStatement) conn.prepareStatement(sql);
                        ps.executeUpdate();//返回1 执行成功
                        conn.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Girl3DViewActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                dismissProgress();
                                if (adminDialog!=null){
                                    adminDialog.dismiss();
                                }
                                QueryMysql(startLine, pageSize);
                            }
                        });
                    }
                } catch (SQLException e) {
                    System.out.println("清空失败");
                    e.printStackTrace();
                }

            }
        }).start();
    }


    /**
     * 插入数据
     *
     * @param jennie
     */
    private void insertUserData(final List<JsoupImageVO> jennie) {
        //连接数据库进行操作需要在主线程操作
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress("爬取" + etKey.getText().toString().trim() + "第" + jsoupPage + "页");
            }
        });

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
                        for (int i = 0; i < jennie.size(); i++) {
                            ps = (PreparedStatement) conn.prepareStatement(sql);
                            String id = "ID_" + System.currentTimeMillis();
                            String name = jennie.get(i).getName();
                            String url = jennie.get(i).getUrl();
                            ps.setString(1, id);//第一个参数 name 规则同上
                            ps.setString(2, name);//第二个参数 phone 规则同上
                            ps.setString(3, url);//第三个参数 content 规则同上
                            ps.executeUpdate();//返回1 执行成功
                        }
                        conn.close();
                        jsoupPage++;
                        List<JsoupImageVO> jennie = JsoupBaiduPic.findImage(etKey.getText().toString().trim(), jsoupPage);
                        if (jennie.size() > 0) {
                            insertUserData(jennie);
                        } else {
                            dismissProgress();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


    /**
     * 查询数据库
     *
     * @param startLine
     * @param pageSize
     */
    private void QueryMysql(final int startLine, final int pageSize) {
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
                                    if (URLS.size() <= 0) {
                                        showAdminDialog();
                                        viewTagsAdapter = new ViewTagsAdapter(URLS);
                                        tagCloudView.setAdapter(viewTagsAdapter);
                                        Random r = new Random();
                                        int page2 = r.nextInt(5) + 1;
                                        int page2StartLine = page2 * pageSize;
                                        QueryMysql2(page2StartLine, pageSize);
                                        tagCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                            @Override
                                            public void onItemClick(ViewGroup parent, View view, int position) {
                                                startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                            }
                                        });
                                    } else {
                                        viewTagsAdapter = new ViewTagsAdapter(URLS);
                                        tagCloudView.setAdapter(viewTagsAdapter);
                                        Random r = new Random();
                                        int page2 = r.nextInt(5) + 1;
                                        int page2StartLine = page2 * pageSize;
                                        QueryMysql2(page2StartLine, pageSize);
                                        tagCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                                            @Override
                                            public void onItemClick(ViewGroup parent, View view, int position) {
                                                startActivity(new Intent(Girl3DViewActivity.this, MyRecyclerviewProject.class));
                                            }
                                        });
                                    }

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

    private void QueryMysql2(final int startLine, final int pageSize) {
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
                                    QueryMysql3(page3StartLine, pageSize);


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

    private void QueryMysql3(final int startLine, final int pageSize) {
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
                                    QueryMysql4(page4StartLine, pageSize);


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

    private void QueryMysql4(final int startLine, final int pageSize) {
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
                                    QueryMysql5(page5StartLine, pageSize);


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

    private void QueryMysql5(final int startLine, final int pageSize) {
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
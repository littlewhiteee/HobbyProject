package cn.ismiss.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import byc.imagewatcher.ImageWatcher;
import byc.imagewatcher.ImageWatcherHelper;
import cn.ismiss.R;
import cn.ismiss.adapter.GirlAdapter;
import cn.ismiss.base.BaseActivity;
import cn.ismiss.bean.GirlBean;
import cn.ismiss.utils.DBOpenHelper;
import cn.ismiss.utils.GlideSimpleLoader;
import cn.ismiss.utils.SpaceItemDecoration;
import cn.ismiss.view.CustomDotIndexProvider;
import cn.ismiss.view.CustomLoadingUIProvider;

public class MyRecyclerviewProject extends BaseActivity implements  ImageWatcher.OnPictureLongPressListener {

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
    private ImageWatcherHelper iwHelper;//方式二
    private int allPage;
    private static final int SAVE_SUCCESS = 0;//保存图片成功
    private static final int SAVE_FAILURE = 1;//保存图片失败
    private static final int SAVE_BEGIN = 2;//开始保存图片
    boolean isTranslucentStatus = true; //是不是全屏
    private List<Uri> urlList = new ArrayList<>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SAVE_BEGIN:
                    showProgress("正在保存");
                    break;
                case SAVE_SUCCESS:
                    dismissProgress();
                    Toast.makeText(MyRecyclerviewProject.this, "保存成功", Toast.LENGTH_SHORT).show();
                    break;
                case SAVE_FAILURE:
                    Toast.makeText(MyRecyclerviewProject.this, "保存失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private TextView save;
    private TextView cancel;
    private PopupWindow popupWindow;

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
//        initUrl();
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
                     * 数据库翻页规则
                     * 0-12
                     * 13-12
                     * 25-12
                     * 37-12
                     */
                    page++;
                    moreUrl.clear();
                    QueryMysql(pageSize * (page - 1) + 1, pageSize);
                    refreshLayout.finishLoadMore(3000);

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
                refreshLayout.finishRefresh(2000);
            }
        });
    }


    private void bindAdapter(final List<String> girlData) {
        mGirlAdapter = new GirlAdapter(R.layout.item_girl, girlData);
        rvGirl.setAdapter(mGirlAdapter);
        iwHelper = ImageWatcherHelper.with(this, new GlideSimpleLoader()) // 一般来讲， ImageWatcher 需要占据全屏的位置
                .setTranslucentStatus(0) // 如果不是透明状态栏，你需要给ImageWatcher标记 一个偏移值，以修正点击ImageView查看的启动动画的Y轴起点的不正确
                .setErrorImageRes(R.mipmap.error_picture) // 配置error图标 如果不介意使用lib自带的图标，并不一定要调用这个API
                .setOnPictureLongPressListener(this)
                .setOnStateChangedListener(new ImageWatcher.OnStateChangedListener() {
                    @Override
                    public void onStateChangeUpdate(ImageWatcher imageWatcher, ImageView clicked, int position, Uri uri, float animatedValue, int actionTag) {
                        Log.e("IW", "onStateChangeUpdate [" + position + "][" + uri + "][" + animatedValue + "][" + actionTag + "]");
                    }

                    @Override
                    public void onStateChanged(ImageWatcher imageWatcher, int position, Uri uri, int actionTag) {
                        if (actionTag == ImageWatcher.STATE_ENTER_DISPLAYING) {
                        } else if (actionTag == ImageWatcher.STATE_EXIT_HIDING) {

                        }
                    }
                })
                .setIndexProvider(new CustomDotIndexProvider())//自定义页码指示器（默认数字）
                .setLoadingUIProvider(new CustomLoadingUIProvider()); // 自定义LoadingUI


     //   Utils.fitsSystemWindows(isTranslucentStatus, findViewById(R.id.v_fit));
        mGirlAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                urlList.clear();
                for (int i = 0; i <URLS.size() ; i++) {
                    urlList.add(Uri.parse(URLS.get(i)));
                }
                iwHelper.show(urlList,position);
            }
        });
        mGirlAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                return false;
            }
        });
    }


    private void showSavePopwindow(final String url) {
        System.out.println("拿到的url:"+url);
        View view = LayoutInflater.from(MyRecyclerviewProject.this).inflate(R.layout.pop_upload_save, null);
        save = (TextView) view.findViewById(R.id.save);
        cancel = (TextView) view.findViewById(R.id.cancel);

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
        popupWindow.setOutsideTouchable(false);
        View parent = LayoutInflater.from(MyRecyclerviewProject.this).inflate(R.layout.activity_main, null);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        //popupWindow在弹窗的时候背景半透明
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.5f;
        getWindow().setAttributes(params);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                      saveToLocal(url);
                    }
                }).start();
                popupWindow.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }


    /**
     * 保存照片到本地
     *
     * @param
     */
    private void saveToLocal(String url) {
        /**
         * 第一步,把公网地址转成bitmap对象
         */

        //(一)把网络图片转为bitmap
        Bitmap bitmap = returnBitMap(url);
        saveImageToPhotos(this, bitmap);

        //(二)把Uri转为bitmap,根据情况选择
//        Bitmap bitmap = null;
//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), url);
//
//            //第二步,保存bitmap到本地
//            saveImageToPhotos(this, bitmap);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


    private void saveImageToPhotos(MyRecyclerviewProject myRecyclerviewProject, Bitmap bitmap) {
        System.out.println("保存照片");
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "jennie");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(myRecyclerviewProject.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mHandler.obtainMessage(SAVE_FAILURE).sendToTarget();
            return;
        }
        // 最后通知图库更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);
        mHandler.obtainMessage(SAVE_SUCCESS).sendToTarget();
    }


    /**
     * 将URL转化成bitmap形式
     *
     * @param url
     * @return bitmap type
     */
    public final static Bitmap returnBitMap(String url) {
        URL myFileUrl;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
            HttpURLConnection conn;
            conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
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

    @Override
    public void onBackPressed() {
        if (!iwHelper.handleBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPictureLongPress(ImageView v, final Uri uri, int pos) {
        showSavePopwindow(URLS.get(pos));
    }
}

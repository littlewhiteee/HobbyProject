package cn.ismiss.base;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.ismiss.view.MyDialogView;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/22
 * <p/>
 */
public class BaseActivity extends AppCompatActivity {
    private boolean isRunning = false;
    private MyDialogView progressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isRunning = true;
        super.onCreate(savedInstanceState);
    }

    /**
     * 显示自定义消息
     *
     * @param msg
     */
    public void showProgress(String msg) {
        if (!isRunning) {
            return;
        }
        if (progressDialog == null) {
            progressDialog = new MyDialogView(BaseActivity.this);
            progressDialog.setCancelable(false);
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (!TextUtils.isEmpty(msg)) {
            progressDialog.setMessage(msg);
        } else {
            progressDialog.setMessage("正在加载中...");
        }
        progressDialog.show();
    }


    /**
     * 弹窗消失
     */
    public void dismissProgress() {
        if (!isRunning) {
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }
}

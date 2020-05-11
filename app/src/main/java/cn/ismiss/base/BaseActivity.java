package cn.ismiss.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import cn.ismiss.permissions.CheckPermissionsListener;
import cn.ismiss.view.MyDialogView;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/22
 * <p/>
 */
public class BaseActivity extends AppCompatActivity implements CheckPermissionsListener {
    private boolean isRunning = false;
    private MyDialogView progressDialog;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isRunning = true;
        super.onCreate(savedInstanceState);
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

    public void showToast(Context context,String Message) {
        if (!isRunning) {
            return;
        }
        Toast.makeText(context,Message,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public void onGranted() {

    }

    @Override
    public void onDenied(List<String> permissions) {
        Toast.makeText(this, "权限被禁用，请到设置里打开", Toast.LENGTH_SHORT).show();
    }
}

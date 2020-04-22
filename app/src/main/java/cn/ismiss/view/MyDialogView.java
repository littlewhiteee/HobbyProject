package cn.ismiss.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import cn.ismiss.R;


/**
 * 自定义加载ing提示框
 * yupmisss@gmail.com
 * Created by ʞɔnlǝlʇʇıl. on 2018/12/17
 * <p/>
 */
public class MyDialogView extends Dialog {
    private TextView messageTxv;
    private String message;

    public MyDialogView(Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_lay);
        messageTxv = (TextView) findViewById(R.id.message);
    }

    public void setMessage(String message) {
        this.message = message;
        if (messageTxv != null) messageTxv.setText(message);
    }

    @Override
    public void show() {
        super.show();
        if (messageTxv != null && message != null) messageTxv.setText(message);
    }
}

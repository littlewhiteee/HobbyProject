package cn.ismiss.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.ismiss.R;
import cn.ismiss.base.BaseActivity;
import cn.ismiss.helper.MyDBOpenHelper;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/24
 * <p/>
 */
public class TestSQLiteActivity extends BaseActivity implements View.OnClickListener {

    private TextView dbUpdate, dbSelect, dbAdd, dbCreate, dbDelete, tvStartLive;
    private MyDBOpenHelper myDBHelper;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql);
        initView();
    }

    private void initView() {
        dbCreate = findViewById(R.id.db_create);
        dbAdd = findViewById(R.id.db_add);
        dbSelect = findViewById(R.id.db_select);
        dbUpdate = findViewById(R.id.db_update);
        dbDelete = findViewById(R.id.db_delete);
        tvStartLive = findViewById(R.id.tv_start_live);
        dbCreate.setOnClickListener(this);
        dbAdd.setOnClickListener(this);
        dbSelect.setOnClickListener(this);
        dbUpdate.setOnClickListener(this);
        dbDelete.setOnClickListener(this);
        tvStartLive.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.db_create:
                db = myDBHelper.getWritableDatabase();
                showToast(this, "创建数据库");
                myDBHelper = new MyDBOpenHelper(TestSQLiteActivity.this, "tb_little_sister.db", null, 1);
                break;
            case R.id.db_add:
                db = myDBHelper.getWritableDatabase();
                showToast(this, "插入一条数据");
                break;
            case R.id.db_select:
                db = myDBHelper.getWritableDatabase();
                showToast(this, "查询数据库");
                break;
            case R.id.db_update:
                db = myDBHelper.getWritableDatabase();
                showToast(this, "更新一条数据");
                break;
            case R.id.db_delete:
                db = myDBHelper.getWritableDatabase();
                showToast(this, "删除一条数据");
                break;
            case R.id.tv_start_live:
                startActivity(new Intent(this, LiveActivity.class));
                break;
        }
    }
}

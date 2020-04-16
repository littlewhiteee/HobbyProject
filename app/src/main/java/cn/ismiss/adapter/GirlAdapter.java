package cn.ismiss.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.ismiss.R;
import cn.ismiss.base.MyApp;
import cn.ismiss.bean.GirlBean;
import cn.ismiss.utils.ScreenUtils;

/**
 * yupmisss@gmail.com
 * Created by littlewhite. on 2020/4/16
 * <p/>
 */
public class GirlAdapter extends BaseQuickAdapter<GirlBean.DataBean, BaseViewHolder> {
    public GirlAdapter(int layoutResId, @Nullable List<GirlBean.DataBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, GirlBean.DataBean item) {
        ViewGroup.LayoutParams params = helper.itemView.findViewById(R.id.iv_girl).getLayoutParams();
        int screenWidth = ScreenUtils.getScreenWidth(mContext);
        params.width = screenWidth / 3;
        params.height = screenWidth / 3 * 5 / 3;
        helper.itemView.findViewById(R.id.iv_girl).setLayoutParams(params);
        Glide.with(MyApp.getContext()).load(item.getUrl()).into((ImageView) helper.itemView.findViewById(R.id.iv_girl));
    }
}

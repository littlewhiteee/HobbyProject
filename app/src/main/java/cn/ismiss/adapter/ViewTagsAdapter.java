package cn.ismiss.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.moxun.tagcloudlib.view.TagsAdapter;

import java.util.List;

import cn.ismiss.R;
import cn.ismiss.base.MyApp;

/**
 * Created by moxun on 16/3/4.
 */
public class ViewTagsAdapter extends TagsAdapter {

    private List<String> girlData;
    private ImageView ivGirl;

    @Override
    public int getCount() {
        return girlData.size();
    }

    public ViewTagsAdapter(List<String> girlData) {
        this.girlData = girlData;
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.tag_item_view, parent, false);
        ivGirl = view.findViewById(R.id.iv);
        Glide.with(MyApp.getContext()).load(girlData.get(position).trim()).into(ivGirl);
        return view;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getPopularity(int position) {
        return position % 5;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {
//        view.findViewById(R.id.android_eye).setBackgroundColor(themeColor);
//
//        int color = Color.argb((int) ((1 - themeColor) * 255), 255, 255, 255);
//        ((ImageView) view.findViewById(R.id.iv)).setColorFilter(color);

    }

}

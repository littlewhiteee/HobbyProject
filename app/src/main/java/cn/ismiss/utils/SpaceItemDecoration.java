package cn.ismiss.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

// 自定义条目修饰类
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int column;
    private final int space;

    public SpaceItemDecoration(int space, int column) {
        this.space = space;
        this.column = column;
    }

    /**
     * 三列布局评分
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) % 3 == 0) {
            outRect.left = 0; //第一列左边贴边
        } else {
            outRect.left = space;//第二列移动一个位移间距

        }

        if (parent.getChildAdapterPosition(view) >= 3) {
            outRect.top = 10;
        } else {
            outRect.top = 0;
        }
    }
}
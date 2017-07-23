package net.ddns.paolo7297.musicdownloader.ui;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by paolo on 22/07/17.
 */

public class SquaredLayout extends RelativeLayout {
    public SquaredLayout(Context context) {
        super(context);
    }

    public SquaredLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquaredLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquaredLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}

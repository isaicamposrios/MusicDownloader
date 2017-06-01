package net.ddns.paolo7297.musicdownloader.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created by paolo on 17/04/17.
 */

public class DisablingImageButton extends android.support.v7.widget.AppCompatImageButton {
    public DisablingImageButton(Context context) {
        super(context);
    }

    public DisablingImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DisablingImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        Drawable background = getBackground();
        Drawable drawable   = getDrawable();

        if (enabled) {
            if (background != null)
                background.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            if (drawable != null)
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }
        else {
            if (background != null)
                background.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            if (drawable != null)
                drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
    }
}

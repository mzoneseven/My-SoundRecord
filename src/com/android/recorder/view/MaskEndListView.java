package com.android.recorder.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.recorder.ImageHelper;
import com.android.recorder.R;

public class MaskEndListView extends ListView {
    Bitmap startBitmap = null;
    Bitmap endBitmap = null;
    Paint paint = null;
    private Drawable mHeaderMaskDrawable;
    private Drawable mFooterMaskDrawable;
    private int mHeaderMaskHeight;
    private int mFooterMaskHeight;
    private View mBlankFooter;
    private static final int SCROLL_UP = 1;
    private static final int SCROLL_DOWN = -1;

    private int mFooterMaskHeight_value = 0;;

    public MaskEndListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MaskEndListView);
        mFooterMaskDrawable = a
                .getDrawable(R.styleable.MaskEndListView_lv_footer_mask);
        mHeaderMaskDrawable = a
                .getDrawable(R.styleable.MaskEndListView_lv_header_mask);
        mHeaderMaskHeight = a.getDimensionPixelSize(
                R.styleable.MaskEndListView_lv_header_mask_height, -1);
        mFooterMaskHeight = a.getDimensionPixelSize(
                R.styleable.MaskEndListView_lv_footer_mask_height, -1);
        mFooterMaskHeight_value = mFooterMaskHeight;
        a.recycle();

        mBlankFooter = LayoutInflater.from(getContext()).inflate(
                R.layout.v_transpant_footer, null);
        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mFooterMaskHeight);
        mBlankFooter.setLayoutParams(layoutParams);
        mBlankFooter.setFocusable(false);
        mBlankFooter.setFocusableInTouchMode(false);
        mBlankFooter.setClickable(false);
        mBlankFooter.setLongClickable(false);
        mBlankFooter.setOnClickListener(null);
        // super.addFooterView(mBlankFooter);
    }

    protected void dispatchDraw(Canvas canvas) {
        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(),
                null, Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                        | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                        | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                        | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

        super.dispatchDraw(canvas);
        if (canScrollVertically(SCROLL_UP)) {
            mFooterMaskHeight = mFooterMaskHeight_value;
        } else {
            mFooterMaskHeight = mFooterMaskHeight_value;
        }

        drawMask(canvas);

        canvas.restoreToCount(sc);
    }

    private void drawMask(Canvas canvas) {
        if (startBitmap == null && mHeaderMaskDrawable != null) {
            startBitmap = ImageHelper.drawableToBitmap(mHeaderMaskDrawable,
                    canvas.getWidth(), mHeaderMaskHeight);
        }

        if (mHeaderMaskDrawable != null) {
            canvas.drawBitmap(startBitmap, 0, 0, paint);
        }

        if (endBitmap == null && mFooterMaskDrawable != null) {
            endBitmap = ImageHelper.drawableToBitmap(mFooterMaskDrawable,
                    canvas.getWidth(), mFooterMaskHeight);
        }

        if (mFooterMaskDrawable != null) {
            canvas.drawBitmap(endBitmap, 0, canvas.getHeight()
                    - mFooterMaskHeight, paint);
        }
    }

    public int getFooterMaskHeight() {
        return mFooterMaskHeight;
    }
}

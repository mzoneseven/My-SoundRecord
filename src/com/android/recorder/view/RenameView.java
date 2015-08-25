package com.android.recorder.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.recorder.R;

public class RenameView extends LinearLayout {

    public RenameView(Context context) {
        super(context);
        init();
    }

    public RenameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
    }

    public RenameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    ImageButton imageButton;
    TextView tv;

    private void init() {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.renameview, null);
        imageButton = (ImageButton) view.findViewById(R.id.btn_rename_delete);
        tv = (TextView) view.findViewById(R.id.tv_rename_delete);
        addView(view);
    }

    public void setText(int id) {
        // TODO Auto-generated method stub
        tv.setText(id);
        invalidate();
    }

    public void setDrawable(int id) {
        imageButton.setImageResource(id);
        invalidate();
    }
}

package com.android.recorder.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
    private boolean canScroll = true;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CustomViewPager(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public void setScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        // TODO Auto-generated method stub

        if (canScroll) {
            return super.onTouchEvent(arg0);
        } else {
            return false;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        // TODO Auto-generated method stub
        Log.d("mhyuan", "onInterceptTouchEvent canScroll=" + canScroll);
        if (canScroll) {
            return super.onInterceptTouchEvent(arg0);
        } else {
            return false;
        }

    }

}

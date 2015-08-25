package com.android.recorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class WaveSurfaceView extends View {

    // private Path mWavePath = null;//new Path();
    // private Paint mPaint;

    private final static int SCREEN_REFRESH_TIME = 50; // 刷新时间，每200毫秒刷新一次
    private final static int SCREEN_MOVE_SPEED = 0; // move speed,every 200ms
                                                    // move 10 pixes

    private final static int DATA_REFRESH_TIME = 50;

    private int mMoveOffsetX = 0;// x move offset, must less than screen width
    private int mDataRefreshLeftTime = 0;//
    private int mWave_Step = 0;//

    // private int mScreenWidth = 100;
    private WaveSmooth mWave1 = null;
    private WaveAverage mWave2 = null;

    @SuppressLint("ResourceAsColor")
    private void initView() {

        mWave1 = new WaveSmooth();
        mWave2 = new WaveAverage();
        // mWave1.setColor(R.color.wave_color_1);
        // mWave2.setColor(R.color.wave_color_2);
        mWave2.setColor(Color.argb(57, 0x5a, 0xe0, 0));
        mWave1.setColor(Color.argb(57, 0x24, 0xff, 0));
    }

    public WaveSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        // TODO Auto-generated constructor stub
    }

    Runnable mLeftRefreshRunnable = new Runnable() {

        @Override
        public void run() {

            if (mMoveOffsetX >= getWidth()) {
                mMoveOffsetX = 0;
            }
            mMoveOffsetX += SCREEN_MOVE_SPEED;

            mDataRefreshLeftTime += SCREEN_REFRESH_TIME;

            // if(mDataRefreshLeftTime>=DATA_REFRESH_TIME){
            // mDataRefreshLeftTime = 0;
            // mWave1.preProcessSound( getWidth(), getHeight());
            // mWave2.preProcessSound( getWidth(), getHeight());
            // }

            mWave_Step++;
            if (mWave_Step >= Wave.WAVE_PERIOD) {
                mWave_Step = 0;
                mWave1.preProcessSound(getWidth(), getHeight());
                mWave2.preProcessSound(getWidth(), getHeight());
            }
            invalidate();
            postDelayed(mLeftRefreshRunnable, SCREEN_REFRESH_TIME);
        }

    };

    @Override
    protected void onDraw(Canvas canvas) {

        int oriY = canvas.getHeight() / 2;
        int oriX = 0 + mMoveOffsetX;
        // int setpX = canvas.getWidth() /Wave.SCREEN_SAMPLE_COUNT / 2 ;

        // canvas.drawColor(Color.argb(100, 0xff, 0xff, 0xff));

        // mWave1.calculatePath(canvas.getWidth(), canvas.getHeight(),
        // mMoveOffsetX);
        // mWave2.calculatePath(canvas.getWidth(), canvas.getHeight(),
        // mMoveOffsetX+setpX);

        mWave1.calculatePath(canvas.getWidth(), canvas.getHeight(),
                mMoveOffsetX, mWave_Step);
        mWave2.calculatePath(canvas.getWidth(), canvas.getHeight(),
                mMoveOffsetX, mWave_Step);

        // mPaint的Style是FILL，会填充整个Path区域
        canvas.drawPath(mWave1.getPath(), mWave1.getPaint());
        canvas.drawPath(mWave2.getPath(), mWave2.getPaint());

    }

    // init data
    public void doStart() {

    }

    // start timer
    public void doResume() {
        postDelayed(mLeftRefreshRunnable, SCREEN_REFRESH_TIME);
    }

    // stop timer
    public void doPause() {
        mWave2.clearPath();
        mWave1.clearPath();
        this.removeCallbacks(mLeftRefreshRunnable);
        invalidate();

    }

    // destory data
    public void doStop() {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        doPause();
    }
}

package com.android.recorder;

import java.util.Arrays;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class Wave {
    protected int[] mSampleDataArray = null; // 画图点的数据
    protected int[] mSampleDataPreArray = null; // 缓存数据
    protected short[] mDrawBuffer = null;// new short[4800];

    // protected ArrayList<Point> mPointsList;
    private Paint mPaint;
    protected Path mWavePath = null;
    protected int SCREEN_SAMPLE_COUNT = 4; // 屏幕关键点个数，（波峰个数）
    protected long mLastMaxSound = 100; // 选取曾经得到过得最大值，
    public final static int WAVE_PERIOD = 5;

    @SuppressLint("ResourceAsColor")
    public Wave() {
        mWavePath = new Path();
        mPaint = new Paint();
        // mPointsList = new ArrayList<Point>();

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(R.color.wave_color_1);
        mPaint.setAlpha(57);
        SCREEN_SAMPLE_COUNT = getSampleCount();

        if (mSampleDataArray == null) {
            mSampleDataArray = new int[SCREEN_SAMPLE_COUNT + 1];
        }
        if (mSampleDataPreArray == null) {
            mSampleDataPreArray = new int[SCREEN_SAMPLE_COUNT + 1];
        }

    }

    public void setColor(int color) {
        if (mPaint != null) {
            mPaint.setColor(color);
        }
    }

    public void setAlpha(int alpha) {
        if (mPaint != null) {
            mPaint.setAlpha(alpha);
        }
    }

    protected int getDrawBufferSize() {
        // int sizeCount = 0;
        // int maxZeroCount = 20;
        // synchronized (DrawView.sDrawBuffer) {
        // for (int i = 0, j = 0; i < DrawView.sDrawBuffer.length - 1; i++) {
        //
        // Short soundvalue = DrawView.sDrawBuffer[i];
        // if (soundvalue < 0) {
        // DrawView.sDrawBuffer[i] = (short) (0 - soundvalue);
        // }
        // if (0 == soundvalue) {
        // sizeCount++;
        // if (sizeCount >= maxZeroCount) {
        // return i - maxZeroCount;
        // }
        // } else {
        // sizeCount = 0;
        // }
        // }
        // return DrawView.sDrawBuffer.length;
        // }
        return 2047;

    }

    protected void dataToPositive(short[] drawbuffer) {
        if (drawbuffer == null) {
            return;
        }
        for (int i = 0; i < drawbuffer.length; i++) {

            short soundvalue = drawbuffer[i];
            if (soundvalue < 0) {
                if (soundvalue == -32768) {
                    soundvalue = 0x7fff;
                } else {
                    soundvalue = (short) (0 - soundvalue);
                }
                drawbuffer[i] = soundvalue;
            }
        }
    }

    public Short getValueAtPos(int DrawPos, int count, int bufferLength) {

        return mDrawBuffer[DrawPos];
    }

    // return maxValue
    private int copyDataToSampleBuffer(int[] sampleDataArray) {

        int dataSampleCount = SCREEN_SAMPLE_COUNT;
        int drawBufferLength = 2047;// getDrawBufferSize();
        int maxValue = 0;

        if (sampleDataArray == null || mDrawBuffer == null) {
            return 0;
        }

        drawBufferLength = mDrawBuffer.length;
        dataSampleCount = sampleDataArray.length;

        int dataStep = drawBufferLength / dataSampleCount;

        for (int i = 0, j = 0; i < drawBufferLength - 1; i += dataStep, j++) {

            Short soundvalue = getValueAtPos(i, dataStep, drawBufferLength);

            if (soundvalue > maxValue) {
                maxValue = soundvalue;
            }

            if (j < dataSampleCount) {
                sampleDataArray[j] = (int) soundvalue;
            }
        }
        return maxValue;
    }

    private void copyDataLocal() {

        int drawBufferLength = getDrawBufferSize();

        if (mDrawBuffer == null || mDrawBuffer.length < drawBufferLength) {
            mDrawBuffer = new short[drawBufferLength];
        }

        synchronized (DrawView.sDataBuffer) {
            System.arraycopy(DrawView.sDataBuffer, 0, mDrawBuffer, 0,
                    drawBufferLength);
        }
        dataToPositive(mDrawBuffer);
    }

    private void makeDataSmallThanHeight(int max, int MaxHeight,
            int[] dataSampleArray) {

        if (dataSampleArray == null) {
            return;
        }
        int soundScaleY = 0;

        if (mLastMaxSound < max) {
            mLastMaxSound = max;
        }

        if (max != 0) {
            soundScaleY = max / MaxHeight;// (float)((float)maxScreenY/(float)maxValue);
        } else {
            soundScaleY = 1;
        }

        if (soundScaleY == 0) {
            soundScaleY = 1;
        }

        if (soundScaleY < 20) {
            soundScaleY = 20;
        }

        for (int i = 0; i < dataSampleArray.length; i++) {

            dataSampleArray[i] = (int) (dataSampleArray[i] / soundScaleY);

        }
    }

    public void preProcessSound(int screen_width, int screen_height) {

        screen_height = screen_height / 4 + screen_height / 8;

        if (mSampleDataArray == null) {
            mSampleDataArray = new int[SCREEN_SAMPLE_COUNT + 1];
        }
        if (mSampleDataPreArray == null) {
            mSampleDataPreArray = new int[SCREEN_SAMPLE_COUNT + 1];
        }

        System.arraycopy(mSampleDataArray, 0, mSampleDataPreArray, 0,
                SCREEN_SAMPLE_COUNT);

        // copy DrawView.sDataBuffer[48000] to mDrawBuffer[2047]
        copyDataLocal();
        // copy mDrawBuffer[2047] to mSampleDataArray[SCREEN_SAMPLE_COUNT]
        int maxValue = copyDataToSampleBuffer(mSampleDataArray);

        makeDataSmallThanHeight(maxValue, screen_height, mSampleDataArray);

    }

    public void calculatePath(int width, int height, int moveOffset, int setp) {

    }

    public Path getPath() {
        return mWavePath;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void clearPath() {

        if (mSampleDataArray != null && mSampleDataArray.length > 0) {
            Arrays.fill(mSampleDataArray, 0);
        }

        if (mSampleDataPreArray != null && mSampleDataPreArray.length > 0) {
            Arrays.fill(mSampleDataPreArray, 0);
        }

    }

    public int getSampleCount() {
        return 6;
    }
}

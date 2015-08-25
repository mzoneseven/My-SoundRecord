package com.android.recorder;


public class WaveAverage extends WaveSmooth {

    @Override
    public Short getValueAtPos(int DrawPos, int count, int bufferLength) {
        // TODO Auto-generated method stub
        // return super.getValueAtPos();
        float sum = 0;
        float tempValue = 0;
        for (int i = 0; (i + DrawPos) < bufferLength && i < count; i++) {
            tempValue = mDrawBuffer[i + DrawPos];
            sum += (tempValue / count);
        }
        return (short) sum;
    }

    @Override
    public void calculatePath(int width, int height, int moveOffset, int step) {
        // TODO Auto-generated method stub
        // moveOffset = width/getSampleCount()/2;
        super.calculatePath(width, height, moveOffset, step);

    }

    @Override
    public int getSampleCount() {
        // TODO Auto-generated method stub
        return 3;
    }
}

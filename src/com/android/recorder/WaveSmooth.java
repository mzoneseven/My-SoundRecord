package com.android.recorder;

import java.util.Random;

public class WaveSmooth extends Wave {

    Random mRandom1 = new Random(10);

    @Override
    public void calculatePath(int width, int height, int moveOffset, int step) {

        if (mSampleDataArray == null || mSampleDataPreArray == null) {
            return;
        }

        if (mSampleDataArray.length <= 0 || mSampleDataPreArray.length <= 0) {
            return;
        }

        // testData(width, height, moveOffset);

        mWavePath.reset();

        int maxHeight = height * 2 / 3;
        int oriY = height * 2 / 3 - 10;
        int oriX = 0 + moveOffset;
        int setpX = width / (SCREEN_SAMPLE_COUNT - 1);

        mWavePath.moveTo(oriX, oriY + 10);
        // mWavePath.moveTo(oriX, oriY+height/2);
        mWavePath.lineTo(oriX, oriY);

        int pointX1 = 0;
        int pointY1 = 0;
        int pointX2 = 0;
        int pointY2 = 0;

        int yOffset = 0;

        int ValueY = 0;

        int offsetWidth = 0;

        // setpX = setpX+mRandom1.nextInt(100);

        for (int i = 0; i < mSampleDataArray.length
                && i < mSampleDataPreArray.length; i++) {

            if (offsetWidth > 0) {
                offsetWidth = 0 - mRandom1.nextInt(50);
            } else {
                offsetWidth = mRandom1.nextInt(50);
            }

            offsetWidth = 0;

            setpX = setpX + offsetWidth;

            ValueY = mSampleDataPreArray[i];

            int offsetY = (mSampleDataArray[i] - mSampleDataPreArray[i]) * step
                    / WAVE_PERIOD;

            ValueY = ValueY + offsetY;

            pointY1 = oriY - ValueY * 2;

            if (pointY1 < 0) {
                pointY1 = 10;
            }
            if (pointY1 > height) {
                pointY1 = height;
            }

            pointX1 = oriX + setpX / 4;
            pointX2 = oriX + setpX * 2 / 4;
            pointY2 = oriY - ValueY;

            mWavePath.quadTo(pointX1, pointY1, pointX2, pointY2);

            pointY1 = oriY;
            pointX1 = oriX + setpX * 3 / 4;
            pointX2 = oriX + setpX;
            pointY2 = oriY - ValueY;

            if (i < mSampleDataArray.length - 2) {
                // nextY1 = oriY - mPointsList.get(i+1).y*2;
                offsetY = (mSampleDataArray[i + 1] - mSampleDataPreArray[i + 1])
                        * step / WAVE_PERIOD;
                pointY2 = oriY - mSampleDataPreArray[i + 1] - offsetY;
            }

            if (pointX2 >= width) {
                pointY2 = oriY;
            }

            mWavePath.quadTo(pointX1, pointY1, pointX2, pointY2);

            oriX = oriX + setpX;

            // oriY = pointY2;

        }

        mWavePath.lineTo(oriX, oriY + 10);

        mWavePath.close();
    }

    @Override
    public int getSampleCount() {
        // TODO Auto-generated method stub
        return 4;
    }

}

package com.android.recorder;


public class DrawView {

    static short[] sDataBuffer = new short[4800 * (16 / 8) * 1 * 5];

    /*
     * 数据缓冲区，存储数据用来画图
     */
    public static void writeDataCacheBuffer(short[] buffer) {
        if (buffer == null || sDataBuffer == null) {
            return;
        }
        synchronized (sDataBuffer) {
            int minLenth = buffer.length > sDataBuffer.length ? sDataBuffer.length
                    : buffer.length;
            System.arraycopy(buffer, 0, sDataBuffer, 0, minLenth);
        }
    }

}

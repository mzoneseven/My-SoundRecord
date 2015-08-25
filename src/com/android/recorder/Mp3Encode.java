package com.android.recorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

public class Mp3Encode {
    private static final String TAG = "MP3Recorder";
    private String filePath;
    private int sampleRate;
    private boolean isRecording = false;
    private boolean isPause = false;
    private Handler handler;

    public static final int MSG_REC_STARTED = 1;
    public static final int MSG_REC_STOPPED = 2;
    public static final int MSG_REC_PAUSE = 3;
    public static final int MSG_REC_RESTORE = 4;
    public static final int MSG_REC_REFRASH_VIEW = 5;
    public static final int MSG_ERROR_GET_MIN_BUFFERSIZE = -1;
    public static final int MSG_ERROR_CREATE_FILE = -2;
    public static final int MSG_ERROR_REC_START = -3;
    public static final int MSG_ERROR_AUDIO_RECORD = -4;
    public static final int MSG_ERROR_AUDIO_ENCODE = -5;
    public static final int MSG_ERROR_WRITE_FILE = -6;
    public static final int MSG_ERROR_CLOSE_FILE = -7;

    public Mp3Encode(String filePath) {
        this.filePath = filePath;
        this.sampleRate = 48000;
    }

    public void start() {
        if (isRecording) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                android.os.Process
                        .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                final int minBufferSize = AudioRecord.getMinBufferSize(
                        sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                if (minBufferSize < 0) {
                    if (handler != null) {
                        handler.sendEmptyMessage(MSG_ERROR_GET_MIN_BUFFERSIZE);
                    }
                    return;
                }
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);

                short[] buffer = new short[sampleRate * (16 / 8) * 1 * 5];
                byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

                FileOutputStream output = null;
                try {
                    Log.i(TAG, filePath);
                    output = new FileOutputStream(new File(filePath), true);
                } catch (FileNotFoundException e) {
                    if (handler != null) {
                        handler.sendEmptyMessage(MSG_ERROR_CREATE_FILE);
                    }
                    return;
                }
                Mp3Encode.mp3init(sampleRate, 1, sampleRate, 32);
                isRecording = true;
                isPause = false;
                try {
                    try {
                        audioRecord.startRecording();
                    } catch (IllegalStateException e) {
                        if (handler != null) {
                            handler.sendEmptyMessage(MSG_ERROR_REC_START);
                        }
                        return;
                    }

                    try {
                        if (handler != null) {
                            Log.i(TAG, "sendEmptyMessage MSG_REC_STARTED");
                            handler.sendEmptyMessage(MSG_REC_STARTED);
                        }

                        int readSize = 0;
                        boolean pause = false;
                        while (isRecording) {
                            if (isPause) {
                                if (!pause) {
                                    Log.i(TAG, "sendEmptyMessage MSG_REC_PAUSE");
                                    handler.sendEmptyMessage(MSG_REC_PAUSE);
                                    pause = true;
                                }
                                continue;
                            }
                            if (pause) {
                                Log.i(TAG, "sendEmptyMessage MSG_REC_RESTORE");
                                handler.sendEmptyMessage(MSG_REC_RESTORE);
                                pause = false;
                            }
                            readSize = audioRecord.read(buffer, 0,
                                    minBufferSize);
                            if (readSize < 0) {
                                if (handler != null) {
                                    handler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
                                }
                                break;
                            } else if (readSize == 0) {
                                ;
                            } else {
                                DrawView.writeDataCacheBuffer(buffer);
                                if (handler != null) {
                                    handler.sendEmptyMessage(MSG_REC_REFRASH_VIEW);
                                }
                                int encResult = Mp3Encode.encode(buffer,
                                        buffer, readSize, mp3buffer);
                                if (encResult < 0) {
                                    if (handler != null) {
                                        handler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
                                    }
                                    break;
                                }
                                if (encResult != 0) {
                                    try {
                                        output.write(mp3buffer, 0, encResult);
                                    } catch (IOException e) {
                                        if (handler != null) {
                                            handler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        int flushResult = Mp3Encode.flush(mp3buffer);
                        if (flushResult < 0) {
                            if (handler != null) {
                                handler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
                            }
                        }
                        if (flushResult != 0) {
                            try {
                                output.write(mp3buffer, 0, flushResult);
                            } catch (IOException e) {
                                if (handler != null) {
                                    handler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
                                }
                            }
                        }
                        try {
                            output.close();
                        } catch (IOException e) {
                            if (handler != null) {
                                handler.sendEmptyMessage(MSG_ERROR_CLOSE_FILE);
                            }
                        }
                    } finally {
                        audioRecord.stop();
                        audioRecord.release();
                    }
                } finally {
                    Mp3Encode.close();
                    isRecording = false;
                }
                if (handler != null) {
                    Log.i(TAG, "sendEmptyMessage MSG_REC_STOPPED");
                    handler.sendEmptyMessage(MSG_REC_STOPPED);
                }
            }
        }.start();
    }

    public void stop() {
        Log.i(TAG, "stop");
        isRecording = false;
    }

    public void pause() {
        isPause = true;
    }

    public void restore() {
        isPause = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPaus() {
        if (!isRecording) {
            return false;
        }
        return isPause;
    }

    /**
     * 
     * @see RecMicToMp3#MSG_REC_STARTED
     * @see RecMicToMp3#MSG_REC_STOPPED
     * @see RecMicToMp3#MSG_REC_PAUSE
     * @see RecMicToMp3#MSG_REC_RESTORE
     * @see RecMicToMp3#MSG_ERROR_GET_MIN_BUFFERSIZE
     * @see RecMicToMp3#MSG_ERROR_CREATE_FILE
     * @see RecMicToMp3#MSG_ERROR_REC_START
     * @see RecMicToMp3#MSG_ERROR_AUDIO_RECORD
     * @see RecMicToMp3#MSG_ERROR_AUDIO_ENCODE
     * @see RecMicToMp3#MSG_ERROR_WRITE_FILE
     * @see RecMicToMp3#MSG_ERROR_CLOSE_FILE
     */
    public void setHandle(Handler handler) {
        this.handler = handler;
    }

    static {
        System.loadLibrary("mp3lame");
    }

    public static void mp3init(int inSamplerate, int outChannel,
            int outSamplerate, int outBitrate) {
        init(inSamplerate, outChannel, outSamplerate, outBitrate, 7);
    }

    public native static void init(int inSamplerate, int outChannel,
            int outSamplerate, int outBitrate, int quality);

    public native static int encode(short[] buffer_l, short[] buffer_r,
            int samples, byte[] mp3buf);

    public native static int flush(byte[] mp3buf);

    public native static void close();
}

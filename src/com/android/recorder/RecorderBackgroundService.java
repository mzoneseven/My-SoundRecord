package com.android.recorder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RecorderBackgroundService extends Service {

    public static final String STATUS_CHANGE_ACTION = "com.lewa.lewarecorder.STATUS_CHANGE";
    public static final String ACTION_EXTRA = "record_btn"; // from status bar's
                                                            // broadcast
    private static final String TAG = "RecorderBackgroundService";

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);

        if (intent == null) {
            Log.d(TAG, "intent==null return");
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "RecorderBackgroundService action=" + action);

        Intent broadcast = new Intent();
        broadcast.putExtra(ACTION_EXTRA, action);
        broadcast.setAction(STATUS_CHANGE_ACTION);
        sendBroadcast(broadcast);

    }

}

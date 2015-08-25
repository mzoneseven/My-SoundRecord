package com.android.recorder;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;

public class PlayMp3 {
    public MediaPlayer player;
    private String sourceUri;
    public boolean isPlaying = false;
    private boolean isPrepared = false;
    private Context mContext;

    public PlayMp3() {
        isPrepared = false;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    void setDataSource(String sourceUri) {

        this.sourceUri = sourceUri;
    }

    void start() {
        if (sourceUri == null) {
            return;
        }
        if (player == null) {
            player = new MediaPlayer();
        } else {
            player.reset();
        }
        try {
            player.setDataSource(TimeUtil.FILE_DIR + sourceUri);
            player.prepare();
            isPrepared = true;
            player.start();
            isPlaying = true;

        } catch (IllegalStateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void pause() {

        player.pause();
        isPlaying = false;
    }

    void resume() {
        boolean canresume = true;
        if (!isPrepared && sourceUri != null && player != null) {
            try {
                player.setDataSource(TimeUtil.FILE_DIR + sourceUri);
                player.prepare();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                canresume = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                canresume = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                canresume = false;
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                canresume = false;
                e.printStackTrace();
            }

        }
        if (canresume) {
            player.start();
            // player.
            isPlaying = true;
            isPrepared = true;
        }

    }

    void stop() {
        isPlaying = false;
        if ((player != null) && player.isPlaying()) {
            player.stop();
            // player.reset();
            isPrepared = false;
            // player.release();
            // player = null;
        }
    }

    void release() {
        isPlaying = false;
        isPrepared = false;

        mContext = null;

        if ((player != null) && player.isPlaying()) {
            player.stop();
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

}

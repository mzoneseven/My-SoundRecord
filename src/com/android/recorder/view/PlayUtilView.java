package com.android.recorder.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.android.recorder.R;
import com.android.recorder.ViewExpandAnimation;

public class PlayUtilView extends LinearLayout {

    private String mFileName = "";
    private Boolean mIsExtend = false;

    public PlayUtilView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setIsExtend(boolean isExtend) {
        mIsExtend = isExtend;
    }

    public boolean getIsExtend() {
        return mIsExtend;
    }

    public void showPlayBar() {
        // TODO Auto-generated method stub

        startAnimation(new ViewExpandAnimation(this, 0));

        ImageButton btnPlay = (ImageButton) findViewById(R.id.btn_play);
        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
        btnPlay.setBackgroundResource(R.drawable.play_pause);
        seekbar.setProgress(0);
    }

    public void hidePlayBar() {
        // TODO Auto-generated method stub
        // super.setVisibility(visibility);
        if (getVisibility() == View.VISIBLE) {
            ViewExpandAnimation anim = new ViewExpandAnimation(this, 1);
            anim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(View.GONE);
                    ImageButton btnPlay = (ImageButton) findViewById(R.id.btn_play);
                    SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
                    btnPlay.setBackgroundResource(R.drawable.play_start);
                    seekbar.setProgress(0);
                }
            });
            startAnimation(anim);
        }
    }

}

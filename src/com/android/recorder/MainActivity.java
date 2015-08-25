package com.android.recorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.recorder.view.CustomViewPager;
import com.android.recorder.view.PlayUtilView;
import com.android.recorder.view.RenameView;

public class MainActivity extends Activity implements OnClickListener,
        ViewPager.OnPageChangeListener, OnItemClickListener,
        OnItemLongClickListener, OnCompletionListener, OnSeekBarChangeListener,
        OnSeekCompleteListener {

    // Contorl buttons on bottom, start or stop recorder
    private ImageButton mBtnRecorder;
    private ImageButton mBtnCancle;
    private ImageButton mBtnSave;
    // page to change record file list and recorder main view
    private CustomViewPager mViewPager;
    // text views
    private TextView mTextViewTitle; // "Recorder"
    private TextView mTextViewTime; // "00:00:00"
    private TextView mTextViewState; // "Recording"
    // two dots,use to mark which page is current view
    private ImageView mImageViewDotLeft;
    private ImageView mImageViewDotRight;

    // list for viewpage
    private List<View> list;
    // recorder list
    private List<Map<String, String>> list_mp3;
    // background timer,used for recording time length

    private Timer mTimer;
    private TimerTask mTimerTask;
    public int mTime = 0;

    private Mp3Encode mEncoder;
    private PlayMp3 mMp3Player;

    private String mFileName;
    private String mSaveFileName;
    private String mPlayingFileName;

    private ListView mFileListView;
    private Mp3Adapter mFileListAdapter;
    private View mRecordUnit;
    private View mRenameUnit;
    private WaveSurfaceView mWaveView = null;

    // OnItemClick中所需要的各种控件
    private PlayUtilView mPlayUtilView = null;
    private TextView mTextViewFileName;
    private ImageButton mBtnPlay;
    private SeekBar mSeekbar;
    private TextView mTextViewCurrent;

    private int mLastClickItem = -1;
    private int mLongItemLastClick = -1;
    private int mCurrentPosition;
    private Dialog mMenuDialog;
    private NotificationManager mNotificationManager;

    private static final String TAG = "LewaRecorder_MainActivity";

    private static final String BTN_STOP_PRESS_ACTION = "com.lewa.lewarecorder.btnstop";
    private static final String BTN_PAUSE_PRESS_ACTION = "com.lewa.lewarecorder.btnpause";
    private static final String BTN_CANCEL_PRESS_ACTION = "com.lewa.lewarecorder.btncancel";
    private static final String ACTION_SOUND_REC = "com.lewa.action.STATUS_SOUND_REC";
    private static final String ACTION_RECORDER_PLAY_VIEWER = "com.lewa.lewarecorder.PLAY_RECORDER_VIEWER";

    private static final int FLAG_SAVE = 1;
    private static final int FLAG_CANCLE = 2;
    private static final int FLAG_DELETE = 3;
    private static final int FLAG_RENAME = 4;
    private static final int FLAG_LONG_CLICK = 5;
    private static final int STATUS_IDLE = 0; // not recording not playing
    private static final int STATUS_RECORDING = 1;
    private static final int STATUS_RECORD_PAUSE = 2;
    private static final int STATUS_PLAYING = 3;
    private static final int STATUS_PLAY_PAUSE = 4;
    private static final int STATUS_RECORD_ONRESUME = 5;
    private static final int STATUS_RECORD_ONSTOP = 6;
    private static final int MESSAGE_STA_BTN_STOP = 101;
    private static final int MESSAGE_STA_BTN_PAUSE = 102;
    private static final int MESSAGE_STA_BTN_CANCEL = 103;

    private static final int MESSAGE_TIME_UPDATE = 104;
    private static final int MESSAGE_TIME_UPDATE_PROCESS = 105;
    private static final int MESSAGE_TIME_COMPLETE = 106;

    private static final int SOUNDRECORDER_STATUS = 11;

    private static int SAVE_MIX_TIME_SECOND = 3;// 保存音频的最短时间

    private int mRecorderStatus = STATUS_IDLE;
    private int mRecorderStatusBeforeCalling = STATUS_IDLE;
    private int mRecStaBeforeFocusChange = STATUS_IDLE;

    private AudioManager mAudioManager;

    // is phone calling
    public boolean isCallingActive = false;
    /**
     * 更新显示的计时器时间
     */
    @SuppressLint("HandlerLeak")
    private Handler mTimeHandler = new Handler() {
        public void handleMessage(Message obj) {

            switch (obj.what) {
            case MESSAGE_TIME_UPDATE:
                mTextViewTime.setText(calculationTime(mTime));
                mTime++;
                break;

            case MESSAGE_TIME_UPDATE_PROCESS:
                if ((mMp3Player != null) && (mMp3Player.player.isPlaying())) {
                    int duration = mMp3Player.player.getDuration();
                    mCurrentPosition = mMp3Player.player.getCurrentPosition();
                    int temp = 0;
                    if (temp >= mCurrentPosition) {
                        mCurrentPosition = temp;
                    }
                    temp = mCurrentPosition;
                    int progress = (int) Math
                            .round((100 * mCurrentPosition / duration));

                    mSeekbar.setProgress(progress);

                    mTextViewCurrent
                            .setText(calculationTime(mCurrentPosition / 1000));
                }
                break;

            case MESSAGE_TIME_COMPLETE:
                mTextViewCurrent
                        .setText(calculationTime((mCurrentPosition + 200) / 1000));
                mSeekbar.setProgress(100);
                break;
            }

        }
    };

    private String calculationTime(int time) {

        int hour = (int) (time / 3600);
        int min = (int) ((time - hour * 3600) / 60);
        int second = (int) (time % 60);
        StringBuilder str = new StringBuilder();
        String h;
        String m;
        String s;
        if (hour < 10) {
            h = 0 + "" + hour;
        } else {
            h = "" + hour;
        }
        if (min < 10) {
            m = 0 + "" + min;
        } else {
            m = "" + min;
        }
        if (second < 10) {
            s = 0 + "" + second;
        } else {
            s = "" + second;
        }
        str.append(h);
        str.append(":");
        str.append(m);
        str.append(":");
        str.append(s);
        return str.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        // window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // window.setStatusBarColor(Color.TRANSPARENT); //end
        setContentView(R.layout.activity_main);
        FileUtil.createDb(getApplicationContext());
        initResources();
        // set status to idle,
        updateStatus(STATUS_IDLE);

        IntentFilter filter = new IntentFilter(
                RecorderBackgroundService.STATUS_CHANGE_ACTION);
        registerReceiver(mBroadcastReceiver, filter);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    private void initResources() {

        mRecordUnit = findViewById(R.id.recordUnit);
        mBtnRecorder = (ImageButton) findViewById(R.id.btn_recoder);
        mBtnCancle = (ImageButton) findViewById(R.id.btn_cancle);
        mBtnSave = (ImageButton) findViewById(R.id.btn_save);
        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
        mTextViewTitle = (TextView) findViewById(R.id.tv_title);

        mImageViewDotLeft = (ImageView) findViewById(R.id.dot_left);
        mImageViewDotRight = (ImageView) findViewById(R.id.dot_right);
        list = new ArrayList<View>();
        // list_show = new ArrayList<View>();
        View view_file = LayoutInflater.from(getBaseContext()).inflate(
                R.layout.filepager, null);
        View view_recoder = LayoutInflater.from(getBaseContext()).inflate(
                R.layout.recoderpager, null);
        mTextViewState = (TextView) view_recoder
                .findViewById(R.id.tv_recoder_state);
        mTextViewTime = (TextView) view_recoder
                .findViewById(R.id.tv_recoder_time);
        if (mTextViewTime != null) {
            mTextViewTime.setText("00:00:00");
        }
        mFileListView = (ListView) view_file.findViewById(R.id.file_list);
        mFileListView.setOnItemClickListener(this);
        mFileListView.setOnItemLongClickListener(this);
        mFileListView.setVerticalScrollBarEnabled(true);
        list.add(view_recoder);
        list.add(view_file);
        mWaveView = (WaveSurfaceView) view_recoder
                .findViewById(R.id.recoder_wave_surface);
        mMp3Player = new PlayMp3();
        mMp3Player.setContext(getApplicationContext());

        Intent mp3Intent = new Intent(this, RecorderBackgroundService.class);
        this.startService(mp3Intent);

        mViewPager.setAdapter(new RecoderViewpagerAdapter(list));

        mViewPager.setOnPageChangeListener(this);
        mBtnRecorder.setOnClickListener(this);
        mBtnCancle.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        // TODO
        // load app files on /LEWA/Recorder/
        // FileUtil.loadAllFiles();

        list_mp3 = FileUtil.scanFileItem();
        if (list_mp3 != null) {
            mFileListAdapter = new Mp3Adapter(list_mp3);
            mFileListView.setAdapter(mFileListAdapter);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        case R.id.btn_save:
            btn_save_click();
            break;
        case R.id.btn_cancle:
            btn_cancle_click();
            break;
        case R.id.btn_recoder:
            btn_recoder_click();
            break;
        case R.id.btn_play:
            onPlayPauseClick();
            break;
        case R.id.deleteview:
            mMenuDialog.cancel();
            makeDialog(FLAG_DELETE);
            break;
        case R.id.renameview:
            mMenuDialog.cancel();
            makeDialog(FLAG_RENAME);
            break;
        }
    }

    private void getAudioManagerFocus() {
        if (null == mAudioManager) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED != mAudioManager
                .requestAudioFocus(mAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        }
    }

    private void cancelAudioManagerFocus() {
        if (null == mAudioManager) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);

    }

    private void pausePlayRecorder() {

        mMp3Player.setDataSource(mPlayingFileName);
        mBtnPlay.setBackgroundResource(R.drawable.play_start);
        updateStatus(STATUS_PLAY_PAUSE);
        mMp3Player.pause();
        mViewPager.setScroll(true);

    }

    private void stopPlayRecorder() {

        if (mPlayUtilView != null) {
            mPlayUtilView.hidePlayBar();
        }
        if (mMp3Player.isPlaying) {
            mMp3Player.stop();
        }
        if (mViewPager != null) {
            mViewPager.setScroll(true);
        }
        updateStatus(STATUS_IDLE);
        cancelAudioManagerFocus();
        mLastClickItem = -1;
    }

    private void onPlayPauseClick() {

        if (getRecorderStatus() == STATUS_PLAYING) {
            pausePlayRecorder();
        } else if (getRecorderStatus() == STATUS_PLAY_PAUSE) {
            startPlayRecorder(true);
        }
    }

    private void startPlayerTimerThread() {
        new Thread() {

            public void run() {
                while (mMp3Player != null && mMp3Player.isPlaying) {

                    mTimeHandler.sendEmptyMessage(MESSAGE_TIME_UPDATE_PROCESS);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void startPlayRecorder(boolean isResume) {

        if (isCallingActive) {
            Log.i(TAG, "----isCallingActive = true startPlayRecorder return---");
            return;
        }

        getAudioManagerFocus();

        mViewPager.setScroll(false);
        updateStatus(STATUS_PLAYING);
        mBtnPlay.setBackgroundResource(R.drawable.play_pause);
        mMp3Player.setDataSource(mPlayingFileName);
        if (isResume) {
            // will not reset player
            mMp3Player.resume();
        } else {
            // will reset player
            mMp3Player.start();
        }
        startPlayerTimerThread();
        mMp3Player.player.setOnCompletionListener(this);
    }

    /**
     * 点击保存按钮
     */
    private void btn_save_click() {

        stopRecoder(false);
        /**
         * 录音文件短于3秒时不保存
         */
        if (mTime <= SAVE_MIX_TIME_SECOND) {
            delTempFile();
        } else {
            saveRecoder();
        }

        releaseTimer();

        updateStatus(STATUS_IDLE);
        // makeDialog(FLAG_SAVE);

    }

    /**
     * 录音保存逻辑处理
     */
    private void saveRecoder() {
        hideBtn();
        String saveName = TimeUtil.createFileName(getApplicationContext());
        boolean isSuccess = FileUtil.checkFileName(saveName, mFileName, this);
        if (isSuccess) {
            mFileName = null;
        }

        mTime = 0;
        updateTime();
        if (mViewPager.getCurrentItem() == 0) {
            mViewPager.setCurrentItem(1);
        }
        if (FileUtil.SAVE_OK) {
            list_mp3 = FileUtil.scanFileItem();
            int height = (int) getResources().getDimension(
                    R.dimen.listview_fromY);
            TranslateAnimation an = new TranslateAnimation(0, 0, -height, 0);
            an.setDuration(800);
            mFileListView.startAnimation(an);
            FileUtil.SAVE_OK = false;
        }
        if (list_mp3 == null) {
            return;
        }
        if (mFileListAdapter == null) {
            mFileListAdapter = new Mp3Adapter(list_mp3);
        } else {
            mFileListAdapter.setData(list_mp3);
        }
        mFileListView.setAdapter(mFileListAdapter);
    }

    /**
     * 点击取消按钮
     */
    private void btn_cancle_click() {
        // stopRecoder();
        // releaseTimer();
        makeDialog(FLAG_CANCLE);
    }

    private void recorderStateTextUpdate(boolean isPause) {
        if (mTextViewState != null) {
            if (isPause) {
                mTextViewState.setText(R.string.record_pause);
            } else {
                mTextViewState.setText(R.string.recording);
            }
        }

    }

    /**
     * 点击录音按钮,初始化audioRecorder,开始录音
     */
    private void btn_recoder_click() {

        if (isCallingActive) {
            return;
        }

        if (getRecorderStatus() == STATUS_PLAYING
                || getRecorderStatus() == STATUS_PLAY_PAUSE) {
            stopPlayRecorder();
            // updateStatus(STATUS_IDLE);
        }

        mViewPager.setCurrentItem(0);
        mViewPager.setScroll(false);
        if (getRecorderStatus() == STATUS_RECORD_PAUSE
                || getRecorderStatus() == STATUS_IDLE) {
            TimeUtil.createFiledir();
            startRecorder();
            mBtnSave.setVisibility(View.VISIBLE);
            mBtnCancle.setVisibility(View.VISIBLE);
            mBtnRecorder.setImageDrawable(getResources().getDrawable(
                    R.drawable.pause));
            mTextViewState.setVisibility(View.VISIBLE);
            recorderStateTextUpdate(false);
            startTimer();
            if (mWaveView != null) {
                mWaveView.doResume();
            }

        } else {
            if (mWaveView != null) {
                mWaveView.doPause();
            }
            // pause recorder
            stopRecoder(true);
            recorderStateTextUpdate(true);
            releaseTimer();
            updateStatus(STATUS_RECORD_PAUSE);
        }
    }

    /**
     * 点击删除按钮
     */
    private void btn_delete_click() {
        if (mLongItemLastClick != -1) {
            // if (mMp3Player.isPlaying) {
            // mMp3Player.stop();
            // }
            stopPlayRecorder();
            String deletename = list_mp3.get(mLongItemLastClick).get("title")
                    .toString();
            FileUtil.delFile(deletename);
            list_mp3.remove(mLongItemLastClick);
            mFileListAdapter.notifyDataSetChanged();
            mLongItemLastClick = -1;

        }
    }

    /**
     * 重命名
     */
    private void btn_rename_click(String newName) {
        String oldName = list_mp3.get(mLongItemLastClick).get("title")
                .toString();
        String savename = FileUtil.renameFile(oldName, newName);
        if (savename != null) {
            list_mp3.get(mLongItemLastClick).put("title", savename);
            mFileListAdapter.notifyDataSetChanged();
        }
        mLongItemLastClick = -1;
    }

    /**
     * 开始计时
     */
    private void startTimer() {

        mTimer = new Timer(true);

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                updateTime();
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);
    }

    /**
     * 更新计时时间
     */
    void updateTime() {
        Message msg = mTimeHandler.obtainMessage();
        msg.what = MESSAGE_TIME_UPDATE;
        mTimeHandler.sendMessage(msg);
    }

    /**
     * 暂停计时
     */
    void releaseTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 停止录音，处理界面和audiorecorder 逻辑 isResume true recorder can resume,false is
     * stop,can't resume again
     */
    void stopRecoder(boolean isPause) {

        mViewPager.setScroll(true);
        stopRecorder(isPause);
        /*
         * time = 0; updateTime();
         */
        mBtnRecorder.setImageDrawable(getResources().getDrawable(
                R.drawable.start));
        if (mWaveView != null) {
            mWaveView.doPause();
        }
    }

    /**
	 * 
	 */
    void hideBtn() {
        // if(isSave){
        mBtnSave.setVisibility(View.INVISIBLE);
        mBtnCancle.setVisibility(View.INVISIBLE);
        mTextViewState.setVisibility(View.INVISIBLE);
        // }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        int currentItem = mViewPager.getCurrentItem();
        // LAST_CLICK_ITEM = -1;
        if (currentItem == 0) {
            mImageViewDotLeft.setImageDrawable(getResources().getDrawable(
                    R.drawable.normal));
            mImageViewDotRight.setImageDrawable(getResources().getDrawable(
                    R.drawable.selected));
            mTextViewTitle.setText(R.string.tv_title_recoder);
        } else {
            mImageViewDotLeft.setImageDrawable(getResources().getDrawable(
                    R.drawable.selected));
            mImageViewDotRight.setImageDrawable(getResources().getDrawable(
                    R.drawable.normal));
            mTextViewTitle.setText(R.string.tv_title_recoder_file);
            // 只有保存数据成功才会重新获取数据
        }
    }

    /**
     * 定制对话框
     * 
     * @param flag
     */
    private void makeDialog(int flag) {
        switch (flag) {
        case FLAG_SAVE:
            makeSaveDialog();
            break;
        case FLAG_CANCLE:
            makeCancleDialog();
            break;
        case FLAG_DELETE:
            makeDeleteDialog();
            break;
        case FLAG_LONG_CLICK:
            makeLongClickDialog(FLAG_LONG_CLICK);
            break;
        case FLAG_RENAME:
            makeRenameDialog();
            break;
        }
    }

    /**
     * 保存录音弹出框
     */
    private void makeSaveDialog() {
        Builder dialog = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.savedialogview,
                null);
        final EditText edit_filename = (EditText) view
                .findViewById(R.id.edit_filename);
        String fileName = TimeUtil.createFileName(getApplicationContext());
        edit_filename.setText(fileName);

        dialog.setTitle(R.string.save_dialog_title)
                .setView(view)
                .setNegativeButton(R.string.cancle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                mTime = 0;
                                updateTime();
                                mFileName = null;
                            }
                        });
        dialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         * 检测文件命名是否合法，处理文件重名
                         */
                        mSaveFileName = edit_filename.getText().toString()
                                .trim();
                        if (mSaveFileName.isEmpty()) {
                            Toast.makeText(MainActivity.this,
                                    R.string.file_name_is_nll,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        boolean isSuccess = FileUtil.checkFileName(
                                mSaveFileName, mFileName, MainActivity.this);
                        if (isSuccess) {
                            mFileName = null;
                        }
                        mTime = 0;
                        updateTime();

                    }
                });
        AlertDialog dlg = dialog.show();
        dlg.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        // dialog.show();
    }

    /**
     * 取消录音弹出框
     */
    private void makeCancleDialog() {
        Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(R.string.cancle_dialog_title)
                .setMessage(R.string.cancle_dialog_message)
                .setNegativeButton(R.string.cancle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {

                            }
                        });
        dialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("mhyuan", TAG + mFileName);
                        stopRecoder(false);
                        releaseTimer();
                        delTempFile();
                        updateStatus(STATUS_IDLE);
                    }
                });
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 录音取消时，删除临时文件
     */
    void delTempFile() {
        if (mFileName != null) {
            File file = new File(mFileName);
            if (file.exists()) {
                file.delete();
            }
            mFileName = null;
        }

        mTime = 0;
        updateTime();
        hideBtn();
    }

    /**
     * 长按弹出框
     */
    private void makeLongClickDialog(int position) {
        mRecordUnit.setVisibility(View.INVISIBLE);
        Builder dialog = new AlertDialog.Builder(MainActivity.this);
        mRenameUnit = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.renamelayout, null);
        RenameView renameview = (RenameView) mRenameUnit
                .findViewById(R.id.renameview);
        renameview.setOnClickListener(this);
        RenameView deleteview = (RenameView) mRenameUnit
                .findViewById(R.id.deleteview);
        deleteview.setOnClickListener(this);
        deleteview.setDrawable(R.drawable.delete);
        // deleteview.setText(R.string.delete);
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                if (mRecordUnit.getVisibility() != View.VISIBLE) {
                    mRecordUnit.setVisibility(View.VISIBLE);
                    Log.i("mhyuan", TAG + "111111111111111");
                }
            }
        });
        mMenuDialog = dialog.setView(mRenameUnit).create();
        mMenuDialog.getWindow().setGravity(Gravity.BOTTOM);
        mMenuDialog.show();
    }

    /**
     * 删除框
     */
    private void makeDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this);
        builder.setTitle(R.string.warning)
                .setMessage(R.string.delete_msg)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // TODO Auto-generated method stub
                                btn_delete_click();
                            }
                        })
                .setNegativeButton(R.string.cancle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // TODO Auto-generated method stub

                            }
                        }).show();

    }

    /**
     * 重命名弹出框
     */
    private void makeRenameDialog() {
        View view = LayoutInflater.from(MainActivity.this).inflate(
                R.layout.renamedialoglayout, null);
        final EditText edit = (EditText) view.findViewById(R.id.edit_rename);

        String filename = TimeUtil.createFileName(getApplicationContext());
        if (mLongItemLastClick != -1 && mLongItemLastClick < list_mp3.size()) {
            filename = list_mp3.get(mLongItemLastClick).get("title").toString();
            String extStr = ".mp3";
            if (!filename.isEmpty() && filename.contains(extStr)) {
                filename = filename.substring(0,
                        filename.length() - extStr.length());
            }
        }

        edit.setText(filename);
        edit.setFocusable(true);
        edit.setFocusableInTouchMode(true);
        edit.requestFocus();
        // edit.addTextChangedListener(this);

        Builder builder = new AlertDialog.Builder(MainActivity.this);

        // /builder
        builder.setTitle(R.string.rename)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                btn_rename_click(edit.getText().toString()
                                        .trim());
                            }
                        })
                .setNegativeButton(R.string.cancle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // TODO Auto-generated method stub

                            }
                        });
        Dialog dialog = builder.create();

        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        final Button positiveButton = ((AlertDialog) dialog)
                .getButton(AlertDialog.BUTTON_POSITIVE);

        edit.addTextChangedListener(// 设置编辑栏的文字输入监听
        new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                if (arg0.toString().isEmpty()) { // 当编辑栏为空的时候，将按钮设置为不可点击。
                    if (positiveButton != null) {
                        positiveButton.setEnabled(false);
                    }
                } else {
                    if (positiveButton != null) {
                        positiveButton.setEnabled(true);
                    }
                }
            }

        });

    }

    // private void hideListPlayBar(){
    // //hide play bar after save recorder file.
    // //hideProgressView();
    // mLastClickItem = -1;
    // }
    //

    public void startRecorder() {

        getAudioManagerFocus();

        if (mFileName == null || mFileName.length() == 0) {
            mFileName = TimeUtil.FILE_DIR + "."
                    + TimeUtil.createFileName(getApplicationContext()) + "temp"
                    + ".mp3";
        }

        mEncoder = new Mp3Encode(mFileName);
        mEncoder.start();
        mEncoder.setHandle(mp3RecordHandler);
        updateStatus(STATUS_RECORDING);
    }

    /**
     * 停止录音，处理逻辑
     */
    public void stopRecorder(boolean isPause) {
        if ((mEncoder != null) && (mEncoder.isRecording())) {
            mEncoder.stop();
        }
        if (isPause) {
            updateStatus(STATUS_RECORD_PAUSE);
        } else {
            updateStatus(STATUS_IDLE);
            cancelAudioManagerFocus();
        }

    }

    public class Mp3Adapter extends BaseAdapter {

        private List<Map> list;

        public Mp3Adapter(List list) {
            this.list = list;
        }

        public void setData(List list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.tv_name = (TextView) convertView
                        .findViewById(R.id.file_name);
                holder.tv_length = (TextView) convertView
                        .findViewById(R.id.file_length);
                holder.tv_date = (TextView) convertView
                        .findViewById(R.id.file_date);
                holder.tv_file_current = (TextView) convertView
                        .findViewById(R.id.file_current);
                holder.tv_file_length = (TextView) convertView
                        .findViewById(R.id.file_progress_date);
                holder.seekbar = (SeekBar) convertView
                        .findViewById(R.id.seekBar);
                holder.playUnit = (LinearLayout) convertView
                        .findViewById(R.id.fl_progress);
                holder.fileUnit = (LinearLayout) convertView
                        .findViewById(R.id.ly_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Map<?, ?> map = list.get(position);
            holder.tv_name.setText(map.get("title").toString());
            holder.tv_length.setText(FileUtil.getLength(MainActivity.this, map
                    .get("length").toString()));
            holder.tv_date.setText(map.get("date").toString());
            holder.tv_file_length.setText(FileUtil.getLength(MainActivity.this,
                    map.get("length").toString()));
            if (mLastClickItem != position) {
                holder.playUnit.setVisibility(View.GONE);
            } else if (mLastClickItem == position) {
                holder.playUnit.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView tv_name, tv_length, tv_date, tv_file_current, tv_file_length;
        SeekBar seekbar;
        LinearLayout playUnit;
        LinearLayout fileUnit;
    }

    /**
     * 隐藏进度条
     */
    void hideProgressView() {
        // for (int i = 0; list_show.size() > 0 ; i = 0) {
        // list_show.get(i).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play_start);
        // ((ProgressBar)
        // list_show.get(i).findViewById(R.id.seekBar)).setProgress(0);
        // ((TextView)
        // list_show.get(i).findViewById(R.id.file_current)).setText("00:00:00");
        //
        // final View v = list_show.get(i).findViewById(R.id.fl_progress);
        // // if(v.getVisibility() == View.VISIBLE){
        // // mClosedAnimation = new ViewExpandAnimation(v, 1);
        // // mClosedAnimation.setAnimationListener(new AnimationListener() {
        // //
        // // @Override
        // // public void onAnimationStart(Animation animation) {
        // // }
        // //
        // // @Override
        // // public void onAnimationRepeat(Animation animation) {
        // // }
        // //
        // // @Override
        // // public void onAnimationEnd(Animation animation) {
        // // v.setVisibility(View.GONE);
        // // }
        // // });
        // // v.startAnimation(mClosedAnimation);
        // // }
        // list_show.remove(i);
        // }
        // //mIsShow = false;
        // if(mMp3Player.isPlaying){
        // mMp3Player.stop();
        // }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        mLongItemLastClick = position;
        makeDialog(FLAG_LONG_CLICK);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        final View v = view.findViewById(R.id.fl_progress);

        if (position == mLastClickItem) {
            // mPlayUtilView.hidePlayBar();
            stopPlayRecorder();
            // updateStatus(STATUS_IDLE);
            // mLastClickItem = -1;
        } else {
            if (mLastClickItem != -1 && mPlayUtilView != null) {
                // lastclickitem is not -1,should stop last play one
                mPlayUtilView.hidePlayBar();
                // stopPlayRecorder();
            }

            mLastClickItem = position;
            mPlayUtilView = (PlayUtilView) view.findViewById(R.id.fl_progress);
            mSeekbar = (SeekBar) view.findViewById(R.id.seekBar);
            mSeekbar.setOnSeekBarChangeListener(MainActivity.this);
            mBtnPlay = (ImageButton) view.findViewById(R.id.btn_play);
            mTextViewCurrent = (TextView) view.findViewById(R.id.file_current);

            mBtnPlay.setOnClickListener(this);
            TextView fileName = (TextView) view.findViewById(R.id.file_name);
            mPlayingFileName = fileName.getText().toString().trim();

            mPlayUtilView.showPlayBar();
            // start new player
            startPlayRecorder(false);
        }

        // mBtnPlay = (ImageButton) view.findViewById(R.id.btn_play);
        // mSeekbar = (SeekBar) view.findViewById(R.id.seekBar);
        // mPlayUtilView = (PlayUtilView) view.findViewById(R.id.fl_progress);
        // mPlayUtilView.showPlayBar();
        // if (v.getVisibility() == View.GONE) {
        // mPlayUtilView.showPlayBar();
        // mTextViewFileName = (TextView) view.findViewById(R.id.file_name);
        // mTextViewCurrent = (TextView) view.findViewById(R.id.file_current);
        // // list_show.add(view);
        // //mIsShow = true;
        // startPlayRecorder();
        // mSeekbar.setOnSeekBarChangeListener(MainActivity.this);
        // mBtnPlay.findViewById(R.id.btn_play).setOnClickListener(
        // MainActivity.this);
        // } else {
        // mPlayUtilView.hidePlayBar();
        //
        // mBtnPlay.setBackgroundResource(R.drawable.play_start);
        // mViewPager.setScroll(true);
        // mSeekbar.setProgress(0);
        // mTextViewCurrent.setText("00:00:00");
        // if (mMp3Player.isPlaying) {
        // mMp3Player.stop();
        // }
        // //mIsShow = false;
        // }
        // mLastClickItem = position;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mMp3Player.stop();
        mPlayUtilView.hidePlayBar();

        // mBtnPlay.setBackgroundResource(R.drawable.play_start);
        // seekbar.setProgress(100);
        mViewPager.setScroll(true);
        Log.d("mhyuan", "onCompletion setScroll true");
        stopPlayRecorder();
        mTimeHandler.sendEmptyMessage(MESSAGE_TIME_COMPLETE);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (mMp3Player != null
                && (getRecorderStatus() == STATUS_PLAYING || getRecorderStatus() == STATUS_PLAY_PAUSE)) {
            int Duration = mMp3Player.player.getDuration();
            mMp3Player.player.seekTo(progress * Duration / 100);
        }
    }

    @Override
    protected void onDestroy() {
        btn_save_click();
        mMp3Player.release();
        mMp3Player = null;
        unregisterReceiver(mBroadcastReceiver);
        cancelAudioManagerFocus();
        mAudioManager = null;

        if (list != null) {
            list.clear();
            list = null;
        }

        if (list_mp3 != null) {
            list_mp3.clear();
            list_mp3 = null;
        }
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        sendSoundRecCast(STATUS_RECORD_ONRESUME);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        sendSoundRecCast(STATUS_RECORD_ONSTOP);
        super.onStop();
    }

    private Handler mp3RecordHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            boolean isRecord = (getRecorderStatus() == STATUS_RECORDING || getRecorderStatus() == STATUS_RECORD_PAUSE);

            Log.i(TAG, "message from MP3Recorder is " + msg.what);
            switch (msg.what) {
            case Mp3Encode.MSG_REC_REFRASH_VIEW:
                break;

            case MESSAGE_STA_BTN_STOP:
                // click stop on status bar
                btn_save_click();
                break;
            case MESSAGE_STA_BTN_PAUSE:
                if (isRecord) {
                    btn_recoder_click();
                } else {
                    onPlayPauseClick();
                }

                break;
            case MESSAGE_STA_BTN_CANCEL:
                if (isRecord) {
                    stopRecoder(false);
                    releaseTimer();
                    delTempFile();
                    updateStatus(STATUS_IDLE);
                } else {
                    stopPlayRecorder();

                }
                break;
            }
            if (msg.what < 0) {
                // error occor
                updateStatus(STATUS_IDLE);
            }
        }
    };

    // when record status changed or error occur,call this method,
    private void updateStatus(int status) {
        setRecorderStatus(status);
        updateNotification(status);
        sendSoundRecCast(status);
    }

    private void setRecorderStatus(int status) {
        mRecorderStatus = status;
    }

    private int getRecorderStatus() {
        return mRecorderStatus;
    }

    private int getRecordingTimer() {
        return mTime;
    }

    private void updateNotification(int status) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mNotificationManager == null) {
            return;
        }
        if (getRecorderStatus() == STATUS_IDLE) {
            mNotificationManager.cancel(SOUNDRECORDER_STATUS);
        } else {
            createNotificationView();
        }
    }

    private String getStatusText(int status) {
        String statusStr = "recording";

        switch (status) {
        case STATUS_RECORDING:
            statusStr = getResources().getString(R.string.status_recording);
            break;
        case STATUS_RECORD_PAUSE:
            statusStr = getResources().getString(R.string.status_record_pause);
            break;
        case STATUS_PLAYING:
            statusStr = getResources().getString(R.string.status_playing);
            break;
        case STATUS_PLAY_PAUSE:
            statusStr = getResources().getString(R.string.status_play_pause);
            break;
        }

        return statusStr;
    }

    private void ViewSetClickPending(RemoteViews views, int btn_id,
            String actionString) {

        if (actionString == null) {
            return;
        }
        if (!actionString.isEmpty()) {
            Intent intent = new Intent(actionString);
            intent.setClass(this, RecorderBackgroundService.class);
            PendingIntent pIntent = PendingIntent
                    .getService(this, 0, intent, 0);
            views.setOnClickPendingIntent(btn_id, pIntent);
        }

    }

    private void createNotificationView() {

        Intent intent;
        PendingIntent pIntent;
        intent = new Intent(ACTION_RECORDER_PLAY_VIEWER);
        intent.putExtra("collapse_statusbar", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        int recordStatus = getRecorderStatus();

        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.recorder_statusbar);

        String app_name = getResources().getString(R.string.app_name);
        String status_name = getStatusText(recordStatus);

        // String artist = song.getArtist() == null ? null :
        // song.getArtist().getName();
        views.setOnClickPendingIntent(R.id.player_status, pIntent);

        views.setImageViewResource(R.id.recorder_icon, R.drawable.status_icon);
        views.setTextViewText(R.id.recorder_app, app_name);
        views.setTextViewText(R.id.status_text, status_name);

        // views.setOnClickPendingIntent(R.id.status_cover, pIntent);
        // artist=MusicUtils.buildArtistName(artist);

        if (recordStatus == STATUS_RECORDING
                || recordStatus == STATUS_RECORD_PAUSE) {
            views.setViewVisibility(R.id.status_btn_stop, View.VISIBLE);
            ViewSetClickPending(views, R.id.status_btn_stop,
                    BTN_STOP_PRESS_ACTION);
            if (recordStatus == STATUS_RECORDING) {
                views.setImageViewResource(R.id.status_btn_pause,
                        R.drawable.statusbar_record_pause_selector);
            } else {
                views.setImageViewResource(R.id.status_btn_pause,
                        R.drawable.statusbar_recorder_start_selector);
            }

        } else {
            views.setViewVisibility(R.id.status_btn_stop, View.INVISIBLE);
            if (recordStatus == STATUS_PLAYING) {
                views.setImageViewResource(R.id.status_btn_pause,
                        R.drawable.statusbar_pause_selector);
            } else {
                views.setImageViewResource(R.id.status_btn_pause,
                        R.drawable.statusbar_play_selector);
            }

        }

        ViewSetClickPending(views, R.id.status_btn_pause,
                BTN_PAUSE_PRESS_ACTION);
        ViewSetClickPending(views, R.id.status_btn_cancel,
                BTN_CANCEL_PRESS_ACTION);

        Notification status = new Notification();
        status.contentView = views;
        status.icon = R.drawable.play_status1;

        boolean isStatusCanCancel = false;

        if (isStatusCanCancel) {
            status.flags |= Notification.FLAG_AUTO_CANCEL;
        } else {
            status.flags |= Notification.FLAG_ONGOING_EVENT;
        }
        status.contentIntent = PendingIntent.getService(MainActivity.this, 0,
                intent, 0);
        mNotificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // startForeground(SOUNDRECORDER_STATUS, status);
        String sdStatus = Environment.getExternalStorageState();
        if (sdStatus.equals(Environment.MEDIA_SHARED)
                || sdStatus.equals(Environment.MEDIA_UNMOUNTED)
                || sdStatus.equals(Environment.MEDIA_REMOVED)) {

        } else {
            // if(mIsSupposedToBePlaying||isNotification){
            if (mNotificationManager != null) {
                mNotificationManager.notify(SOUNDRECORDER_STATUS, status);
                // isNotification=false;
            }
            // }
        }

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.d(TAG, "BroadcastReceiver intent==null");
                return;
            }

            if (RecorderBackgroundService.STATUS_CHANGE_ACTION.equals(intent
                    .getAction())) {
                String actionparameter = intent
                        .getStringExtra(RecorderBackgroundService.ACTION_EXTRA);
                if (actionparameter == null) {
                    return;
                }
                if (actionparameter.equals(BTN_STOP_PRESS_ACTION)) {
                    mp3RecordHandler.sendEmptyMessage(MESSAGE_STA_BTN_STOP);
                } else if (actionparameter.equals(BTN_PAUSE_PRESS_ACTION)) {
                    mp3RecordHandler.sendEmptyMessage(MESSAGE_STA_BTN_PAUSE);
                } else if (actionparameter.equals(BTN_CANCEL_PRESS_ACTION)) {
                    mp3RecordHandler.sendEmptyMessage(MESSAGE_STA_BTN_CANCEL);
                }

            }
        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                isCallingActive = false;
                if (mRecorderStatusBeforeCalling == STATUS_RECORDING) {
                    mRecorderStatusBeforeCalling = STATUS_IDLE;
                    btn_recoder_click();
                }
                if (mRecorderStatusBeforeCalling == STATUS_PLAYING) {
                    mRecorderStatusBeforeCalling = STATUS_IDLE;
                    onPlayPauseClick();
                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:
            case TelephonyManager.CALL_STATE_OFFHOOK: {
                isCallingActive = true;
                if (getRecorderStatus() == STATUS_RECORDING) {
                    mRecorderStatusBeforeCalling = getRecorderStatus();
                    btn_recoder_click();
                }
                if (getRecorderStatus() == STATUS_PLAYING) {
                    mRecorderStatusBeforeCalling = getRecorderStatus();
                    onPlayPauseClick();
                }

            }
                break;
            default:
                break;
            }

            super.onCallStateChanged(state, incomingNumber);
        }
    };

    // /wsliu add begin
    private void sendSoundRecCast(int state) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SOUND_REC);
        intent.putExtra("state", state);
        intent.putExtra("time", getRecordingTimer());
        sendBroadcast(intent);
    }

    // /wsliu add end

    private void pausePlayMp3() {
        if (mMp3Player != null && mMp3Player.isPlaying) {
            mBtnPlay.setBackgroundResource(R.drawable.play_start);
            updateStatus(STATUS_PLAY_PAUSE);
            mMp3Player.pause();
            mViewPager.setScroll(true);
        }
    }

    private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN: {
                Log.i(TAG, "AUDIOFOCUS_GAIN");
                if (STATUS_PLAYING == mRecStaBeforeFocusChange) {
                    startPlayRecorder(true);
                    mRecStaBeforeFocusChange = STATUS_IDLE;
                }

                if (STATUS_RECORDING == mRecStaBeforeFocusChange) {
                    btn_recoder_click();
                    mRecStaBeforeFocusChange = STATUS_IDLE;
                }

            }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                Log.i(TAG,
                        "AUDIOFOCUS_LOSS,AUDIOFOCUS_LOSS_TRANSIENT,AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                mRecStaBeforeFocusChange = getRecorderStatus();
                if (getRecorderStatus() == STATUS_RECORDING) {
                    btn_recoder_click();
                }
                if (getRecorderStatus() == STATUS_PLAYING) {
                    pausePlayMp3();
                }

            }
                break;
            default:
                break;
            }
        }
    };
}

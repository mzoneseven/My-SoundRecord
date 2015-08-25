package com.android.recorder;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

/**
 * 创建存储路径，定制录音文件名称
 * 
 */
public class TimeUtil {
    /**
     * 录音文件需要存储的路径
     */
    public static String FILE_DIR = "/storage/sdcard0/LEWA/Recorder/";

    public static String TAG = "TimeUtil";

    private static final String MONTH[] = { "Jan", "Feb", "Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    private static final String HOURS_12 = "12";
    private static final String DEFAULT_TIME_12_24 = "12";

    /**
     * 判断sd卡是否存在
     * 
     * @return
     */
    public static boolean sdCardExits() {
        return Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 判断录音存储路径是否存在，如不存在创建该路径
     */
    public static void createFiledir() {
        File file = new File(FILE_DIR);
        if (sdCardExits()) {
            if (!file.exists()) {
                file.mkdirs();
                Log.e("mhyuan", "路径创建成功");
            } else {
            }
        }
    }

    // public static String createFileName(){
    // @SuppressWarnings("unused")
    // Calendar calendar = Calendar.getInstance();
    // String fileName =
    // calendar.get(Calendar.MONTH)+1+"月"+calendar.get(Calendar.DAY_OF_MONTH)+"日"+calendar.get(Calendar.HOUR_OF_DAY)+"时"+(calendar.get(Calendar.MINUTE)
    // < 10
    // ?("0"+calendar.get(Calendar.MINUTE)):calendar.get(Calendar.MINUTE))+"分";
    // Log.i("mhyuan", fileName);
    // return fileName;
    // }

    public static String getDate() {
        Calendar calendar = Calendar.getInstance();
        String fileDate = calendar.get(Calendar.YEAR) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.DAY_OF_MONTH);
        Log.i("mhyuan", fileDate);
        return fileDate;
    }

    public static String createFileName_EnCn(Context context) {
        String is12 = Settings.System.getString(context.getContentResolver(),
                Settings.System.TIME_12_24);
        Calendar calendar = Calendar.getInstance();
        String filename = null;
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        // Lewa add for pr750225 ,set 12 Hour be default value
        // TODO bellow will be better: boolean is24Hour =
        // DateFormat.is24HourFormat(context);
        if (null == is12) {
            is12 = DEFAULT_TIME_12_24;
        }

        // Lewa end
        if (HOURS_12.equals(is12)) {
            if ("zh".equals(language)) {
                String am = context.getResources().getString(R.string.am);
                String pm = context.getResources().getString(R.string.pm);
                filename = context.getResources().getString(
                        R.string.file_name_formater1,
                        calendar.get(Calendar.MONTH) + 1 + "",
                        calendar.get(Calendar.DATE) + "",
                        calendar.get(Calendar.HOUR_OF_DAY) / 12 == 0 ? am : pm,
                        calendar.get(Calendar.HOUR_OF_DAY) % 12 + "",
                        calendar.get(Calendar.MINUTE) + "");
            } else {
                filename = context.getResources().getString(
                        R.string.file_name_formater1,
                        calendar.get(Calendar.HOUR)
                                % 12
                                + "."
                                + ((calendar.get(Calendar.MINUTE) < 10) ? "0"
                                        + calendar.get(Calendar.MINUTE)
                                        : calendar.get(Calendar.MINUTE))
                                + (calendar.get(Calendar.HOUR) / 12 == 0 ? "am"
                                        : "pm"),
                        MONTH[calendar.get(Calendar.MONTH)] + " "
                                + calendar.get(Calendar.DATE));
            }
        } else {
            if ("zh".equals(language)) {
                filename = context.getResources().getString(
                        R.string.file_name_formater2,
                        calendar.get(Calendar.MONTH) + 1 + "",
                        calendar.get(Calendar.DATE) + "",
                        calendar.get(Calendar.HOUR_OF_DAY) + "",
                        calendar.get(Calendar.MINUTE) + "");
            } else {
                filename = context.getResources().getString(
                        R.string.file_name_formater1,
                        calendar.get(Calendar.HOUR)
                                + "."
                                + ((calendar.get(Calendar.MINUTE) < 10) ? "0"
                                        + calendar.get(Calendar.MINUTE)
                                        : calendar.get(Calendar.MINUTE)),
                        MONTH[calendar.get(Calendar.MONTH)] + " "
                                + calendar.get(Calendar.DATE));
            }
        }

        Log.i("TimeUtil", filename);

        return filename;
    }

    public static String createFileName(Context context) {

        Calendar calendar = Calendar.getInstance();
        String filename = null;

        filename = ""
                + calendar.get(Calendar.YEAR)
                + ""
                + (calendar.get(Calendar.MONTH) < 10 ? "0"
                        + calendar.get(Calendar.MONTH) : calendar
                        .get(Calendar.MONTH))
                + (calendar.get(Calendar.DATE) < 10 ? "0"
                        + calendar.get(Calendar.DATE) : calendar
                        .get(Calendar.DATE))
                + "_"
                + (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0"
                        + calendar.get(Calendar.HOUR_OF_DAY) : calendar
                        .get(Calendar.HOUR_OF_DAY))
                + (calendar.get(Calendar.MINUTE) < 10 ? "0"
                        + calendar.get(Calendar.MINUTE) : calendar
                        .get(Calendar.MINUTE));

        Log.i("TimeUtil", filename);

        return filename;
    }
}

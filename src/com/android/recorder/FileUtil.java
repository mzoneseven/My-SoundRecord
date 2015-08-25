package com.android.recorder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FileUtil {

    public static Mp3DBHelper dbhelper;
    public static SQLiteDatabase db;
    public static String TAG = "FileUtil";
    public static boolean SAVE_OK = false;

    /**
     * 对已有文件进行命名
     * 
     * @param save_fileName
     */
    public static boolean checkFileName(String save_fileName, String file_name,
            Context context) {
        String getName = save_fileName;

        if (file_name == null || file_name.length() == 0) {
            Log.i("mhyuan", "TAG-------------save fail");
            return false;
        } else {
            File file_old = new File(file_name);
            File file_new;
            if (file_old.exists()) {
                save_fileName += ".mp3";
                file_new = new File(TimeUtil.FILE_DIR + save_fileName);
                int count = 0;
                String tempName = null;
                while (file_new.exists()) {
                    ++count;
                    tempName = TimeUtil.FILE_DIR + getName;
                    tempName = tempName + "(" + count + ")";
                    file_new = new File(tempName + ".mp3");
                }
                if (tempName != null) {
                    save_fileName = tempName + ".mp3";
                    file_new = new File(save_fileName);
                }
                file_old.renameTo(file_new);
                insert(save_fileName, context);
            }
        }
        return true;
        // MainActivity.fileName = null;
    }

    /**
     * 重命名文件
     */
    public static String renameFile(String oldName, String newName) {
        String savename = null;
        if (newName.isEmpty() || oldName.isEmpty()) {
            return savename;
        }
        int returnNum = -1;
        File file_old = new File(TimeUtil.FILE_DIR + oldName);
        File file_new = new File(TimeUtil.FILE_DIR + newName + ".mp3");
        if (!file_old.exists()) {
            return savename;
        } else {
            int count = 0;
            String tempName = null;
            while (file_new.exists()) {
                ++count;
                tempName = newName;
                tempName = tempName + "(" + count + ")";
                file_new = new File(TimeUtil.FILE_DIR + tempName + ".mp3");
            }
            if (tempName != null) {
                newName = tempName;
                file_new = new File(TimeUtil.FILE_DIR + newName + ".mp3");
            }
            file_old.renameTo(file_new);
            ContentValues values = new ContentValues();
            values.put("title", newName + ".mp3");
            returnNum = db.update("mp3_db", values, "title = ?",
                    new String[] { oldName });
        }
        if (returnNum > 0) {
            savename = newName + ".mp3";
        }
        return savename;
    }

    /**
     * 扫描Recoder文件夹下的mp3文件并加入数据库
     * 
     * @return
     */
    public static void loadAllFiles() {
        File fileDir = new File(TimeUtil.FILE_DIR);
        String[] mp3s_temp = fileDir.list();
        if (mp3s_temp == null) {
            db.delete("mp3_db", null, null);
            return;
        }
        List list_temp = new ArrayList<>();

        for (String mp3 : mp3s_temp)
            if (mp3.substring(mp3.lastIndexOf(".") + 1).equals("mp3")) {
                // 删除隐藏文件
                if (mp3.startsWith(".")) {
                    File file = new File(TimeUtil.FILE_DIR + mp3);
                    file.delete();
                }
                list_temp.add(mp3);
            }

        if (list_temp.isEmpty()) {
            db.delete("mp3_db", null, null);
            return;
        }

        Cursor cursor_temp = db.query("mp3_db", new String[] { "title", "date",
                "length" }, null, null, null, null, "id DESC", null);
        try {
            if (cursor_temp.getCount() != list_temp.size()) {

                db.delete("mp3_db", null, null);

                for (int i = 0; i < list_temp.size(); i++) {
                    String fileName = list_temp.get(i).toString();
                    ContentValues values = new ContentValues();
                    values.put("title", fileName);
                    values.put("length", getFileLength(fileName));
                    values.put("date", getFileDate(fileName));
                    Long flog = db.insert("mp3_db", null, values);
                    Log.i(TAG, "----loadAllFiles ------------------------"
                            + flog);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // cursor.close();
            cursor_temp.close();
        }

        return;
    }

    private static String getFileLength(String fileName) {

        File mp3File = new File(TimeUtil.FILE_DIR + fileName);

        long length = mp3File.length();

        int spaceSizePerS = 2047 * 2;

        length = length / spaceSizePerS;

        return String.valueOf((int) length + 1);
    }

    private static String getFileDate(String fileName) {
        String dataStr = "";
        if (fileName.length() > 8) {
            dataStr = fileName.substring(0, 4) + "/" + fileName.substring(4, 6)
                    + "/" + fileName.substring(6, 8);
        }
        return dataStr;
    }

    /**
     * 扫描Recoder文件夹下的mp3文件
     * 
     * @return
     */
    public static List scanFileItem() {
        List list_mp3_temp = new ArrayList<Map>();
        List list_mp3 = new ArrayList<Map>();
        File fileDir = new File(TimeUtil.FILE_DIR);
        String[] mp3s_temp = fileDir.list();
        if (mp3s_temp == null) {
            db.delete("mp3_db", null, null);
            return null;
        }
        List list_temp = new ArrayList<>();

        for (String mp3 : mp3s_temp)
            if (mp3.substring(mp3.lastIndexOf(".") + 1).equals("mp3")) {
                // 删除隐藏文件
                if (mp3.startsWith(".")) {
                    File file = new File(TimeUtil.FILE_DIR + mp3);
                    file.delete();
                }
                list_temp.add(mp3);
            }

        if (list_temp.isEmpty()) {
            db.delete("mp3_db", null, null);
            return null;
        }
        String selection = "";
        String[] mp3s = new String[list_temp.size()];
        Log.i("mhyuan", TAG + "list_temp.size()=" + list_temp.size());
        for (int i = 0; i < list_temp.size(); i++) {

            if (i == 0) {
                selection += "title = ?";
            } else {
                selection += "or title = ?";
            }
            mp3s[i] = list_temp.get(i).toString().trim();
        }
        Cursor cursor_temp = db.query("mp3_db", new String[] { "title", "date",
                "length" }, null, null, null, null, "id DESC", null);
        Cursor cursor = db.query("mp3_db", new String[] { "title", "date",
                "length" }, selection, mp3s, null, null, "id DESC", null);
        try {
            if (cursor_temp.getCount() != 0) {
                for (cursor_temp.moveToFirst(); !cursor_temp.isAfterLast(); cursor_temp
                        .moveToNext()) {
                    Map mp3_info = new HashMap<String, String>();
                    mp3_info.put("title", cursor_temp.getString(cursor_temp
                            .getColumnIndex("title")));
                    mp3_info.put("date", cursor_temp.getString(cursor_temp
                            .getColumnIndex("date")));
                    mp3_info.put("length", cursor_temp.getString(cursor_temp
                            .getColumnIndex("length")));
                    list_mp3_temp.add(mp3_info);
                }
            }
            if (cursor.getCount() != 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                        .moveToNext()) {
                    Map mp3_info = new HashMap<String, String>();
                    mp3_info.put("title",
                            cursor.getString(cursor.getColumnIndex("title")));
                    mp3_info.put("date",
                            cursor.getString(cursor.getColumnIndex("date")));
                    mp3_info.put("length",
                            cursor.getString(cursor.getColumnIndex("length")));
                    list_mp3.add(mp3_info);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            cursor.close();
            cursor_temp.close();
        }
        list_mp3_temp.removeAll(list_mp3);
        for (int i = 0; i < list_mp3_temp.size(); i++) {
            Map map = (Map) list_mp3_temp.get(i);
            delete(map.get("title").toString());
        }

        return list_mp3;
    }

    /**
     * 文件删除分两步，第一步删文件、第二步删数据库
     * 
     * @param fileName
     */
    public static long delFile(String fileName) {
        String filePath = TimeUtil.FILE_DIR + fileName;
        File file = new File(filePath);
        long deletenum = -1;
        if (file.exists()) {

            file.delete();
            deletenum = delete(fileName);
        }
        return deletenum;
    }

    /**
     * 创建数据库保存音频的创建日期、时长、命名等信息
     * 
     * @param context
     */
    public static void createDb(Context context) {
        dbhelper = new Mp3DBHelper(context, "mp3.db", null, 1);
        db = dbhelper.getWritableDatabase();
    }

    /**
     * 保存时插入新文件
     * 
     * @param filename
     */
    public static void insert(String filename, Context context) {
        int index = filename.lastIndexOf("/");
        String name = (String) filename.subSequence(index + 1,
                filename.length());
        ContentValues values = new ContentValues();
        values.put("title", name);
        values.put("length", getLength(context));
        values.put("date", TimeUtil.getDate());
        Long flog = db.insert("mp3_db", null, values);
        SAVE_OK = true;
    }

    public static String getLength(Context context, String timeStr) {

        int fileTime = Integer.parseInt(timeStr);
        int hour = (int) ((fileTime - 1) / 3600);
        int min = (int) (((fileTime - 1) - hour * 3600) / 60);
        int second = (int) ((fileTime - 1) % 60);
        StringBuilder str = new StringBuilder();

        String hString = context.getResources().getString(
                R.string.str_file_info_hour);
        String minString = context.getResources().getString(
                R.string.str_file_info_min);
        String secString = context.getResources().getString(
                R.string.str_file_info_second);

        String h;
        String m;
        String s;
        if (hour > 0) {
            h = hour + hString;
            str.append(h);
        }
        if (min > 0) {
            m = min + minString;
            str.append(m);
        }
        if (second > 0) {
            s = second + secString;
            str.append(s);
        }
        Log.i("mhyuan", TAG + "str" + str);
        Log.i("mhyuan", TAG + fileTime + "");
        return str.toString();
    }

    /**
     * 计算文件时长
     * 
     * @return
     */
    public static String getLength(Context context) {

        int fileTime = ((MainActivity) context).mTime;
        return String.valueOf(fileTime);
        // int hour = (int) ((fileTime - 1) / 3600);
        // int min = (int) (((fileTime - 1) - hour * 3600) / 60);
        // int second = (int) ((fileTime - 1) % 60);
        // StringBuilder str = new StringBuilder();
        //
        // String hString =
        // context.getResources().getString(R.string.str_file_info_hour);
        // String minString =
        // context.getResources().getString(R.string.str_file_info_min);
        // String secString =
        // context.getResources().getString(R.string.str_file_info_second);
        //
        // String h;
        // String m;
        // String s;
        // if(hour > 0){
        // h = hour +hString;
        // str.append(h);
        // }
        // if( min > 0){
        // m = min +minString;
        // str.append(m);
        // }
        // if(second > 0){
        // s = second +secString;
        // str.append(s);
        // }
        // Log.i("mhyuan", TAG+"str"+str);
        // Log.i("mhyuan", TAG+fileTime+"");
        // return str.toString();
    }

    /**
     * 数据库文件信息删除
     * 
     * @param newName
     * @param oldName
     */
    public static long delete(String fileName) {

        long id = db.delete("mp3_db", "title = ?", new String[] { fileName });

        return id;
    }
}

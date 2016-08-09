package com.xt.sqlite.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xt.sqlite.bean.SettingDbBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by xiangleiliu on 2016/8/2.
 */
public class ToolDBUtil {

    public static String[] getAllDb(Context context) {
        File file = new File(SettingDbBean.getDbSourceLocation(context));
        final String[] list1 = file.list();
        return list1;
    }

    public static boolean isTableExist(SQLiteDatabase db, String dbname, String tableName) {

        boolean result = false;
        if (tableName == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static SQLiteDatabase ifExist(Context context, String dbname) {
        SQLiteDatabase db;
        final String path = SettingDbBean.getDbNameLocation(context, dbname);
        try {
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return db;
    }

    public static List<String> selectAllTable(SQLiteDatabase db) {
        List<String> list = new ArrayList<String>();
        Cursor cursor = null;

        try {
            String sql = "select name from " + "sqlite_master" + " where type ='table'";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                list.add(tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

}

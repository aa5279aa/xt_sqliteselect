package com.xt.sqlite.bean;

import android.content.Context;

import java.io.File;

/**
 * Created by zzzhao on 2015/11/30.
 */
public class SettingDbBean {

    public final static String SORT_KEY = "xt";
    public final static String DB_TWO_BTN_ACTIVITY = "数据库增量服务";
    //数据库数组列表
//    public final static String DB_LOCATION = "/data/data/ctrip.android.view/databases/";
    public final static String DB_KEY_NAME = "db_key_name";
    public final static String DB_KEY_VALUE = "db_key_value";
    public final static String DB_NAME = "db_name";
    public final static String DB_TABLE = "db_table";
    public final static String DB_ACTION_TYPE = "db_action_type";

    public final static int DB_ACTION_TYPE_SHOW = 0;
    public final static int DB_ACTION_TYPE_ADD = 1;

    public final static String TABLE_TITLE = "table_title";
    public final static String DB_TABLE_STRUCT_VALUE = "db_table_struct_value";//表结构字段值
    public final static String DB_PATH = "databases";//表结构字段值


    public static String getDbSourceLocation(Context context) {
        final String filesDir = context.getFilesDir().getAbsolutePath();
        final String folderPath = filesDir.substring(0, filesDir.lastIndexOf(File.separator)) + File.separator + "databases";
        return folderPath;
    }

    public static String getDbNameLocation(Context context, String dbname) {
        return getDbSourceLocation(context) + File.separator + dbname;
    }
}

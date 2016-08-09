package com.xt.sqlite.main;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xt.sqlite.R;
import com.xt.sqlite.bean.SettingDbBean;
import com.xt.sqlite.util.ToolDBUtil;
import com.xt.sqlite.widget.XTDataShowView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zzzhao on 2015/11/26.
 * Update by lxl
 * 展示每个table的数据
 */
public class ShowTableInfoActivity extends Activity implements View.OnClickListener {

    private ProgressBar laodingView;
    TextView titleTex;
    ImageView backBtn;
    View freshBtn;
    View addBtn;
    String dbname;
    String tablename;

    String key_name;//主键名称
    LinkedHashMap<String, String> titleList;//表结构
    String[] structs;//表结构
    XTDataShowView mDataShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xt_db_show_tableinfo);
        initView();
        //向Fragment传递数据
        titleTex.setText("增量查询列表");
        final Bundle extras = getIntent().getExtras();
        dbname = extras.getString(SettingDbBean.DB_NAME);
        tablename = extras.getString(SettingDbBean.DB_TABLE);
        freshData();
    }

    void initView() {
        mDataShow = (XTDataShowView) findViewById(R.id.xt_data_view);
        laodingView = (ProgressBar) findViewById(R.id.loading);
        backBtn = (ImageView) findViewById(R.id.db_back_btn);
        titleTex = (TextView) findViewById(R.id.db_title_tex);
        freshBtn = findViewById(R.id.db_refresh_btn);
        addBtn = findViewById(R.id.db_add_btn);

        addBtn.setVisibility(View.VISIBLE);
        freshBtn.setVisibility(View.VISIBLE);

        findViewById(R.id.db_edit_btn).setVisibility(View.GONE);
        findViewById(R.id.db_delete_btn).setVisibility(View.GONE);

        freshBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

    }

    Handler mHandler = new Handler();


    private void freshData() {
        showLoading(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(SettingDbBean.getDbNameLocation(ShowTableInfoActivity.this, dbname), null, SQLiteDatabase.OPEN_READONLY);
                if (ToolDBUtil.isTableExist(db, dbname, tablename)) {
                    final String createSql = getCreateSql(db);
                    key_name = getKeyName(createSql);
                    titleList = selectTitleList(createSql);
                    structs = mapToArray(titleList);
                    List<LinkedHashMap<String, String>> totalDbList = selectDataList(db);
                    db.close();
                    showData(titleList, totalDbList);
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ShowTableInfoActivity.this, tablename + "表不存在", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void showLoading(boolean b) {
        mDataShow.setVisibility(!b ? View.VISIBLE : View.GONE);
        if (b) {
            laodingView.setVisibility(View.VISIBLE);
        } else {
            laodingView.setVisibility(View.GONE);
        }
    }


    private void showData(final LinkedHashMap<String, String> titleList, final List<LinkedHashMap<String, String>> totalDbList) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDataShow.setData(titleList, totalDbList);
                showLoading(false);
                mDataShow.setOnItemClickListener(listener);
            }
        });
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final LinkedHashMap<String, String> itemData = (LinkedHashMap<String, String>) parent.getAdapter().getItem(position);
            jumpToShowValue(itemData);
        }
    };

    private ArrayList<LinkedHashMap<String, String>> selectDataList(SQLiteDatabase db) {
        ArrayList<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        final Cursor cursor = db.rawQuery("select * from " + tablename, new String[]{});
        //遍历整个cursor集合
        if (structs == null) {
            return resultList;
        }
        while (cursor.moveToNext()) {
            resultList.add(parseCursor(cursor, structs));
        }
        return resultList;
    }

    private LinkedHashMap<String, String> selectTitleList(String sql) {
        int first = sql.indexOf("(") + 1;
        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();

        String substring = sql.substring(first, sql.indexOf(")", first));

        String[] split = substring.split(",");

        for (int i = 0; i < split.length; i++) {
            String line = split[i].trim();
            if (line.startsWith("[") && line.contains("]")) {
                final String value = line.substring(1, line.indexOf("]"));
                list.put(value, value);
            }
        }
        return list;
    }

    private String getKeyName(String sql) {
        Pattern compile = Pattern.compile("(?s).*?PRIMARY KEY\\(\\[(.*?)\\]\\).*?");
        Matcher matcher = compile.matcher(sql);
        if (matcher.matches()) {
            String group = matcher.group(1);
            return group;
        }
        return "";
    }

    /**
     * 获取建表语句，
     */
    private String getCreateSql(SQLiteDatabase db) {
        String sql = "select * from sqlite_master where type = 'table' and name = '" + tablename + "'";
        final Cursor cursor = db.rawQuery(sql, new String[]{});
        //遍历整个cursor集合
        if (cursor.moveToNext()) {
            final String createTableSql = cursor.getString(4);
            return createTableSql;
        }
        return "";
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.db_refresh_btn) {
            freshData();
            return;
        } else if (id == R.id.db_add_btn) {
            jumpToAddValue();
        } else if (id == R.id.db_back_btn) {
            finish();
        }
    }

    private void jumpToShowValue(LinkedHashMap<String, String> dataMap) {
        //获取主键并传递

        //启动详情页Activity，并传递数据
        Intent intent = new Intent(ShowTableInfoActivity.this, ShowLineInfoActivity.class);
        intent.putExtra(SettingDbBean.DB_KEY_NAME, key_name);//主键名
        intent.putExtra(SettingDbBean.DB_KEY_VALUE, dataMap.get(key_name));
        intent.putExtra(SettingDbBean.DB_NAME, dbname);
        intent.putExtra(SettingDbBean.DB_TABLE, tablename);
        intent.putExtra(SettingDbBean.DB_ACTION_TYPE, SettingDbBean.DB_ACTION_TYPE_SHOW);
        intent.putExtra(SettingDbBean.DB_TABLE_STRUCT_VALUE, structs);
        startActivityForResult(intent, 1);
    }

    private void jumpToAddValue() {
        //启动详情页Activity，并传递数据
        Intent intent = new Intent(ShowTableInfoActivity.this, ShowLineInfoActivity.class);
        intent.putExtra(SettingDbBean.DB_NAME, dbname);
        intent.putExtra(SettingDbBean.DB_TABLE, tablename);
        intent.putExtra(SettingDbBean.DB_ACTION_TYPE, SettingDbBean.DB_ACTION_TYPE_ADD);
        intent.putExtra(SettingDbBean.DB_KEY_NAME, key_name);//主键名
        intent.putExtra(SettingDbBean.DB_TABLE_STRUCT_VALUE, structs);
        startActivityForResult(intent, 1);
    }

    private String[] mapToArray(LinkedHashMap<String, String> list) {
        String[] str = new String[list.size()];
        final Iterator<String> iterator = list.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            str[i++] = iterator.next();
        }
        return str;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            freshData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static LinkedHashMap<String, String> parseCursor(Cursor cursor, String[] structss) {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        if (cursor.getCount() == 0) {
            return map;
        }

        for (int i = 0; i < structss.length; i++) {
            addValue(map, cursor, structss[i]);
        }
        return map;
    }

    public static void addValue(Map<String, String> map, Cursor cursor, String key) {
        final int columnIndex = cursor.getColumnIndex(key);
        final int type = cursor.getType(columnIndex);
        if (type == Cursor.FIELD_TYPE_INTEGER) {
            map.put(key, String.valueOf(cursor.getInt(columnIndex)));
        } else if (type == Cursor.FIELD_TYPE_FLOAT) {
            map.put(key, String.valueOf(cursor.getFloat(columnIndex)));
        } else if (type == Cursor.FIELD_TYPE_STRING) {
            map.put(key, cursor.getString(columnIndex));
        } else {
            map.put(key, "");
        }
    }

}

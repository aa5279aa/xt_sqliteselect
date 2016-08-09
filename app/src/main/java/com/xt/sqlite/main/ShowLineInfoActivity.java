package com.xt.sqlite.main;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xt.sqlite.R;
import com.xt.sqlite.bean.SettingDbBean;
import com.xt.sqlite.util.XTUtil;
import com.xt.sqlite.widget.XTCustomDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zzzhao on 2015/11/27.
 * 展示详细数据，提供增删改功能
 */
public class ShowLineInfoActivity extends FragmentActivity implements View.OnClickListener {

    private TextView titleTex;
    private TextView backImgView;
    private ImageView refershImgView;
    private ImageView deleteImgView;
    private ImageView editImgView;
    private ImageView addImgView;
    private ListView listView;
    private LinearLayout editLayout;
    private TextView saveBtn;
    private TextView cancelBtn;

    String[] structs;
    HashMap<String, String> mData = new HashMap<String, String>();
    DataShowAdapter adapter;

    String key_name = "";
    String db_name = "";
    String table_name = "";
    String key_value = "";
    int actionType = 0;//0为展示，1为添加

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xt_show_itemid_detail);
        findView();
        Intent intent = getIntent();
        if (intent != null) {
            final Bundle bundle = intent.getExtras();
            actionType = bundle.getInt(SettingDbBean.DB_ACTION_TYPE, 0);
            key_name = bundle.getString(SettingDbBean.DB_KEY_NAME);//主键名
            key_value = bundle.getString(SettingDbBean.DB_KEY_VALUE);
            db_name = bundle.getString(SettingDbBean.DB_NAME);
            table_name = bundle.getString(SettingDbBean.DB_TABLE);
            actionType = bundle.getInt(SettingDbBean.DB_ACTION_TYPE);
            structs = bundle.getStringArray(SettingDbBean.DB_TABLE_STRUCT_VALUE);

            //带过来表结构
            if (actionType == 0) {
                //设置数据。
                selectDataFromDb();
            } else {
                //不设置数据，直接保存
                actionForEdit();
            }
        }
        showView();
        showData();
        bindListener();
    }

    private void showView() {
        refershImgView.setVisibility(View.VISIBLE);
        deleteImgView.setVisibility(View.VISIBLE);
        editImgView.setVisibility(View.VISIBLE);
    }

    private void findView() {
        listView = (ListView) findViewById(R.id.listview);
        backImgView = (TextView) findViewById(R.id.db_back_btn);
        titleTex = (TextView) findViewById(R.id.db_title_tex);
        refershImgView = (ImageView) findViewById(R.id.db_refresh_btn);
        deleteImgView = (ImageView) findViewById(R.id.db_delete_btn);
        editImgView = (ImageView) findViewById(R.id.db_edit_btn);
        addImgView = (ImageView) findViewById(R.id.db_add_btn);
        editLayout = (LinearLayout) findViewById(R.id.edit_layout);
        saveBtn = (TextView) findViewById(R.id.save_btn);
        cancelBtn = (TextView) findViewById(R.id.cancel_btn);
    }


    private void bindListener() {
        backImgView.setOnClickListener(this);
        refershImgView.setOnClickListener(this);
        deleteImgView.setOnClickListener(this);
        editImgView.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        addImgView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.db_delete_btn) {
            actionForDelete();
        } else if (id == R.id.db_edit_btn) {
            actionForEdit();
        } else if (id == R.id.db_refresh_btn) {
            selectDataFromDb();
            showData();
        } else if (id == R.id.db_back_btn) {
            finish();
        } else if (id == R.id.save_btn) {
            actionForEditComplete(true);
        } else if (id == R.id.cancel_btn) {
            actionForEditComplete(false);
        }
    }


    public void actionForDelete() {
        final String itemId = mData.get(key_name);
        XTCustomDialog dialog = new XTCustomDialog();
        dialog.setContent("确定删除该选项？", "确定", "取消");
        dialog.setDialogBtnClick(new XTCustomDialog.DialogBtnClickListener() {
            @Override
            public void leftBtnClick(XTCustomDialog dialog) {
                final boolean b = deleteDataFromDb(itemId);
                XTUtil.showToast(ShowLineInfoActivity.this, b ? "删除成功" : "删除失败", Toast.LENGTH_SHORT);
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void rightBtnClick(XTCustomDialog dialog) {

            }
        });
        dialog.show(getSupportFragmentManager(), "show");
    }

    public void actionForEdit() {
        if (editLayout.getVisibility() == View.VISIBLE) {
            return;
        }
        editLayout.setVisibility(View.VISIBLE);
        //设置为可编辑模式
    }

    public void actionForEditComplete(boolean action) {
        if (!action) {
            editLayout.setVisibility(View.GONE);
            return;
        }
        //提交
        if (actionType == 1) {
            if (insertDataFromDb(mData)) {
                XTUtil.showToast(ShowLineInfoActivity.this, "添加成功", Toast.LENGTH_SHORT);
                actionForEditComplete(false);
                setResult(Activity.RESULT_OK);
            } else {
                XTUtil.showToast(ShowLineInfoActivity.this, "添加失败", Toast.LENGTH_SHORT);
            }
        } else if (actionType == 0) {
            if (editDataFromDb(mData)) {
                XTUtil.showToast(ShowLineInfoActivity.this, "修改成功", Toast.LENGTH_SHORT);
                actionForEditComplete(false);
                setResult(Activity.RESULT_OK);
            } else {
                XTUtil.showToast(ShowLineInfoActivity.this, "修改失败", Toast.LENGTH_SHORT);
            }
        }

    }


    private void selectDataFromDb() {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(SettingDbBean.getDbNameLocation(this, db_name), null, SQLiteDatabase.OPEN_READONLY);
            String args[] = {key_value};
            Cursor cursor = db.rawQuery("select * from " + table_name + " where " + key_name + " =? ", args);
            cursor.moveToFirst();
            mData = ShowTableInfoActivity.parseCursor(cursor, structs);
            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("", "");
    }

    private boolean deleteDataFromDb(String itemID) {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(SettingDbBean.getDbNameLocation(this, db_name), null, SQLiteDatabase.OPEN_READWRITE);
            final int person = db.delete(table_name, key_name + "=?", new String[]{itemID});
            db.close();
            return person >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean editDataFromDb(Map<String, String> map) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(SettingDbBean.getDbNameLocation(this, db_name), null, SQLiteDatabase.OPEN_READWRITE);
        ContentValues cv = new ContentValues();
        for (String key : map.keySet()) {
            cv.put(key, map.get(key));
        }
        String where = key_name + "=" + map.get(key_name);
        final int update = db.update(table_name, cv, where, null);
        db.close();
        return update >= 0;
    }

    private boolean insertDataFromDb(Map<String, String> map) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(SettingDbBean.getDbNameLocation(this, db_name), null, SQLiteDatabase.OPEN_READWRITE);
        ContentValues cv = new ContentValues();
        for (String key : map.keySet()) {
            cv.put(key, map.get(key));
        }
        final boolean b = db.insert(table_name, null, cv) > 0;
        return b;
    }

    public void showData() {
        titleTex.setText(actionType == 0 ? "增量详情" : "增量添加");
        List<String[]> list = new ArrayList<String[]>();
        for (int i = 0; i < structs.length; i++) {
            String[] strs = new String[]{
                    structs[i], mData.get(structs[i])
            };
            list.add(strs);
        }

        if (adapter == null) {
            adapter = new DataShowAdapter(this, R.layout.xt_show_itemid_item);
            listView.setAdapter(adapter);
        }
        adapter.clear();
        adapter.addAll(list);
        listView.setDividerHeight(0);
        adapter.notifyDataSetChanged();
        adapter.index = -1;
    }

    class DataShowAdapter extends ArrayAdapter<String[]> {

        public int index = -1;

        public DataShowAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View inflate;
            if (convertView == null) {
                inflate = View.inflate(parent.getContext(), R.layout.xt_show_itemid_item, null);

            } else {
                inflate = convertView;
            }
            TextView keyText = (TextView) inflate.findViewById(R.id.db_itemkey);
            final EditText valueText = (EditText) inflate.findViewById(R.id.db_itemvalue);
            final String[] item = getItem(position);
            keyText.setText(item[0]);
            valueText.setText(item[1]);
            valueText.setEnabled(editLayout.getVisibility() == View.VISIBLE);


            valueText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        index = position;
                    }
                    return false;
                }
            });
            valueText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        final CharSequence text = valueText.getEditableText();
                        item[1] = text.toString();
                        mData.put(item[0], item[1]);
                    }
                }
            });

            valueText.clearFocus();
            if (index != -1 && index == position) {
                // 如果当前的行下标和点击事件中保存的index一致，手动为EditText设置焦点。
                valueText.requestFocus();
                valueText.setSelection(valueText.getText().length());
                return inflate;
            }
            return inflate;
        }
    }
}

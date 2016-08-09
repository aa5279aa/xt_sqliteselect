package com.xt.sqlite.main;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.xt.sqlite.R;
import com.xt.sqlite.bean.SettingDbBean;
import com.xt.sqlite.util.ToolDBUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by lxl on 2016/8/4.
 */
public class ShowDbTableFragment extends Fragment implements AdapterView.OnItemClickListener {

    private String mDbName = "";

    List<Map<String, String>> mDataList = new ArrayList<Map<String, String>>();
    ListView mList;
    SimpleAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mDbName = arguments.getString(SettingDbBean.TABLE_TITLE);
        }
        final View inflate = inflater.inflate(R.layout.xt_db_showtable_fragment, null);
        mList = (ListView) inflate.findViewById(R.id.table_list);
        mAdapter = new SimpleAdapter(getContext(), mDataList, R.layout.xt_data_item, new String[]{SettingDbBean.DB_TABLE}, new int[]{R.id.text});
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        showTable();
        return inflate;
    }

    public static ShowDbTableFragment newInstance(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(SettingDbBean.TABLE_TITLE, title);
        ShowDbTableFragment fragment = new ShowDbTableFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void showTable() {
        final SQLiteDatabase sqLiteDatabase = ToolDBUtil.ifExist(getContext(), mDbName);
        mDataList.clear();
        if (sqLiteDatabase == null) {
            mAdapter.notifyDataSetChanged();
            return;
        }
        for (String tablename : ToolDBUtil.selectAllTable(sqLiteDatabase)) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(SettingDbBean.DB_TABLE, tablename);
            mDataList.add(map);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void jumpToTable(String dbname, String tablename) {
        Intent intent = new Intent();
        intent.setClass(getContext(), ShowTableInfoActivity.class);
        intent.putExtra(SettingDbBean.DB_NAME, dbname);
        intent.putExtra(SettingDbBean.DB_TABLE, tablename);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Object item = parent.getAdapter().getItem(position);
        if (item instanceof HashMap) {
            final String tablename = (String) ((HashMap) item).get(SettingDbBean.DB_TABLE);
            jumpToTable(mDbName, tablename);
        }
    }
}

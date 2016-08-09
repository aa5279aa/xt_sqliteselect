package com.xt.sqlite.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.xt.sqlite.R;
import com.xt.sqlite.bean.SettingDbBean;
import com.xt.sqlite.util.ToolDBUtil;
import com.xt.sqlite.util.XTUtil;
import com.xt.sqlite.widget.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * Created by lxl on 2016/08/03
 * 该界面提供数据库选择以及table展示的功能
 */
public class SelectTableActivity extends FragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ViewPagerIndicator mTitleSelect;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mTabContents = new ArrayList<Fragment>();
    private List<String> mData = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xt_db_selecttable);
        initView();
        initData();
        initShow();
    }

    private void initView() {
        ImageView backTex = (ImageView) findViewById(R.id.db_back_btn);
        TextView titleTex = (TextView) findViewById(R.id.db_title_tex);
        titleTex.setText(SettingDbBean.DB_TWO_BTN_ACTIVITY);
        backTex.setOnClickListener(this);
        findViewById(R.id.db_refresh_btn).setVisibility(View.INVISIBLE);
        findViewById(R.id.db_edit_btn).setVisibility(View.INVISIBLE);
        findViewById(R.id.db_delete_btn).setVisibility(View.INVISIBLE);
        mTitleSelect = (ViewPagerIndicator) findViewById(R.id.title_select);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
    }

    private void initData() {
        //获取所有db名
        final String[] allDb = ToolDBUtil.getAllDb(this);
        if (allDb == null) {
            XTUtil.showToast(this, "数据库为空");
            return;
        }
        Collections.addAll(mData, allDb);
        final Iterator<String> iterator = mData.iterator();
        while (iterator.hasNext()) {
            final String next = iterator.next();
            if (!next.endsWith("db")) {
                iterator.remove();
            }
        }
        Collections.sort(mData, comparator);
        for (String data : mData) {
            ShowDbTableFragment fragment = ShowDbTableFragment.newInstance(data);
            mTabContents.add(fragment);
        }

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mTabContents.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mTabContents.get(position);
            }
        };
        mViewPager.setAdapter(mAdapter);
        mTitleSelect.setTabItemTitles(mData);
        mTitleSelect.setViewPager(mViewPager, 0);
    }

    private void initShow() {


    }

    @Override
    public void onClick(View v) {
        int clickId = v.getId();
        if (clickId == R.id.db_back_btn) {
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Object item = parent.getAdapter().getItem(position);

    }

    MyComparator comparator = new MyComparator();

    class MyComparator implements Comparator<String> {

        @Override
        public int compare(String lhs, String rhs) {
            if (lhs.toLowerCase().contains(SettingDbBean.SORT_KEY) && !(rhs.toLowerCase().contains(SettingDbBean.SORT_KEY))) {
                return -1;
            }
            return 1;
        }
    }
}


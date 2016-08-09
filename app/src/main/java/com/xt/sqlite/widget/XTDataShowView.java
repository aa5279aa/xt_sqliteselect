package com.xt.sqlite.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.xt.sqlite.R;
import com.xt.sqlite.util.XTViewHolder;
import com.xt.sqlite.util.XTUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Created by lxl on 2016/8/7.
 */
public class XTDataShowView extends HorizontalScrollView {

    Context mContext;
    private LinearLayout mContainer;
    private LinearLayout mTitleLayout;
    private ListView mDataShow;
    private DataAdapter mAdapter;
    private float mMaxItemWidth = 200;//dp
    final private int TEXT_SIZE = 15;
    final float density = getResources().getDisplayMetrics().density;
    int pixelFromDip;

    private float[] widths;//宽度集合
    private TextPaint paint = new TextPaint();
    HashMap<String, Integer> idMap = new HashMap<String, Integer>();

    public XTDataShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        pixelFromDip = XTUtil.getPixelFromDip(mContext, 5);
        mContainer = (LinearLayout) inflate(context, R.layout.xt_db_data_show, null);
        addView(mContainer);
        mTitleLayout = (LinearLayout) mContainer.findViewById(R.id.titile_layout);
        mDataShow = (ListView) mContainer.findViewById(R.id.data_layout);
        mDataShow.setDivider(new ColorDrawable(Color.GRAY));
        mDataShow.setDividerHeight(1);
        mAdapter = new DataAdapter();
        mDataShow.setAdapter(mAdapter);
    }


    public void setData(LinkedHashMap<String, String> titleList, List<LinkedHashMap<String, String>> totalDbList) {
        if (paint == null) {
            TextView textView = new TextView(mContext);
            paint = textView.getPaint();
            paint.setTextSize(TEXT_SIZE);
        }
        //测量widths的值
        widths = measureWidths(XTUtil.getPixelFromDip(mContext, mMaxItemWidth), titleList, totalDbList);
        //展示title
        createLine(mContext, titleList, mTitleLayout);
        mAdapter.setData(totalDbList);
        mAdapter.notifyDataSetChanged();
    }

    private float[] measureWidths(int maxWidth, LinkedHashMap<String, String> titleList, List<LinkedHashMap<String, String>> totalDbList) {
        float[] widths = new float[titleList.size()];
        setWidths(widths, maxWidth, titleList);
        for (LinkedHashMap<String, String> lineDta : totalDbList) {
            setWidths(widths, maxWidth, lineDta);
        }
        return widths;
    }

    private void setWidths(float[] widths, int maxWidth, LinkedHashMap<String, String> lineDta) {
        int i = 0;
        for (String key : lineDta.keySet()) {
            final String line = lineDta.get(key);
            float measureWidth = 0;
            if (line.length() < 100) {
                measureWidth = paint.measureText(line) * density;
                measureWidth = measureWidth >= maxWidth ? maxWidth : measureWidth;
            }
            widths[i] = widths[i] >= measureWidth ? widths[i] : measureWidth;
            i++;
        }
    }

    public LinearLayout createLine(Context context, LinkedHashMap<String, String> lineData, LinearLayout lineContainer) {
        if (lineContainer == null) {
            lineContainer = new LinearLayout(context);
        } else {
            lineContainer.setBackgroundColor(Color.parseColor("#FFEC8B"));
            lineContainer.removeAllViews();
        }
        int i = 0;
        for (String key : lineData.keySet()) {
            TextView text = new TextView(context);
            text.setText(lineData.get(key));
            text.setId(i);
            text.setTextSize(TEXT_SIZE);
            text.setBackgroundResource(R.drawable.xt_tool_right_line);
            text.setTextColor(Color.BLACK);
            text.setPadding(pixelFromDip, 0, pixelFromDip, 0);
            idMap.put(key, i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) widths[i++] + pixelFromDip * 4, pixelFromDip * 4);
            lineContainer.addView(text, lp);
        }
        return lineContainer;
    }


    public void setMaxItemWidth(float maxItemWidth) {
        this.mMaxItemWidth = maxItemWidth;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        if (mDataShow != null) {
            mDataShow.setOnItemClickListener(onItemClickListener);
        }
    }


    class DataAdapter extends BaseAdapter {

        List<LinkedHashMap<String, String>> totalDbList = new ArrayList<LinkedHashMap<String, String>>();

        public void setData(List<LinkedHashMap<String, String>> totalDbList) {
            this.totalDbList = totalDbList;
        }

        @Override
        public int getCount() {
            return totalDbList.size();
        }

        @Override
        public Object getItem(int position) {
            return totalDbList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LinkedHashMap<String, String> itemData = (LinkedHashMap<String, String>) getItem(position);
            if (convertView == null) {
                convertView = createLine(parent.getContext(), itemData, null);
            }
            for (String key : itemData.keySet()) {
                final int id = idMap.get(key);
                final String s = itemData.get(key);
                final TextView textView = XTViewHolder.requestView(convertView, id);
                textView.setText(s);
            }
            return convertView;
        }
    }
}

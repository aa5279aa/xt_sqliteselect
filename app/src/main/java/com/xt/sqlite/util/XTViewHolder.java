package com.xt.sqlite.util;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by bxy on 16/4/30.
 */
public class XTViewHolder {

    public static  <T extends View> T requestView(View convertView, int id){
        SparseArray<View> viewHolder = (SparseArray<View>)convertView.getTag();
        if(viewHolder == null){
            viewHolder = new SparseArray<View>();
            convertView.setTag(viewHolder);
        }
        View view = viewHolder.get(id);
        if(view == null){
            view = convertView.findViewById(id);
            viewHolder.put(id,view);
        }
        return (T)view;
    }
}

package com.example.asus.musicplayer;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class LrcAdapter  extends BaseAdapter {
    private Context context;
    private List<String> list;
    private int currentItem;
    public LrcAdapter(MainActivity mainActivity, List<String> list) {
        this.context = mainActivity;
        this.list = list;
    }

    public void setCurrentItem(int currentItem){
        this.currentItem = currentItem;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LrcAdapter.ViewHolder holder = null;
        if (view == null) {
            holder = new LrcAdapter.ViewHolder();
            //引入布局
            view = View.inflate(context, R.layout.lyric_item, null);
            //实例化对象
            holder.lyric = (TextView) view.findViewById(R.id.everyLrc);
            view.setTag(holder);
        } else {
            holder = (LrcAdapter.ViewHolder) view.getTag();
        }
        //给控件赋值
        holder.lyric.setText(list.get(i).toString());

        if (currentItem == i){
            holder.lyric.setTextColor(Color.parseColor("#31B1FF"));
            holder.lyric.setTextSize(20);
        }else {
            holder.lyric.setTextColor(Color.parseColor("#666666"));
            holder.lyric.setTextSize(18);
        }

        return view;
    }

    public int getCurrentItem() {
        return currentItem;
    }

    class ViewHolder{
        TextView lyric;//每句歌词

    }

}

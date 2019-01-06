package com.example.asus.musicplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends BaseAdapter {
    private Context context;
    private List<Song> list;
    private int currentItem;
    public MyAdapter(MainActivity mainActivity, List<Song> list) {
        this.context = mainActivity;
        this.list = list;
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
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            //引入布局
            view = View.inflate(context, R.layout.list_format, null);
            //实例化对象
            holder.song = (TextView) view.findViewById(R.id.item_mymusic_song);
            holder.singer = (TextView) view.findViewById(R.id.item_mymusic_singer);
            holder.duration = (TextView) view.findViewById(R.id.item_mymusic_duration);
            holder.position = (TextView) view.findViewById(R.id.item_mymusic_postion);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        //给控件赋值
        holder.song.setText(list.get(i).getSong().toString());
        holder.singer.setText(list.get(i).getSinger().toString());
        //时间需要转换一下
        int duration = list.get(i).getDuration();
        String time = MusicList.formatTime(duration);
        holder.duration.setText(time);
        holder.position.setText(i+1+"");

        if (currentItem == i){
            holder.song.setSelected(true);
            holder.singer.setSelected(true);
            holder.duration.setSelected(true);
            holder.position.setSelected(true);
        }else {
            holder.song.setSelected(false);
            holder.singer.setSelected(false);
            holder.duration.setSelected(false);
            holder.position.setSelected(false);
        }

        return view;
    }

    public int getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(int currentItem){
        this.currentItem = currentItem;
    }

    class ViewHolder{
        TextView song;//歌曲名
        TextView singer;//歌手
        TextView duration;//时长
        TextView position;//序号

    }

}

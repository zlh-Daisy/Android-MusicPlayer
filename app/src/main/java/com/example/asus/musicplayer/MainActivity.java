package com.example.asus.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ViewPager vPager;//页卡内容
    private List<View> listViews; // Tab页面列表
    private ImageView cursor;// 动画图片
    private TextView playing,songs;// 页卡头标
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW = 0;// 动画图片宽度
    private int height = 0; //屏幕高度

    private MyAdapter adapter;
    private LrcAdapter lrcAdapter,letterAdapter;
    private ListView mListView,letterListView ,lrcList;
    private List<Song> list;
    private MediaPlayer mplayer = new MediaPlayer();
    private int currentPlaying = -1;
    private String playOrder = "inOrder";

    private SeekBar seekbar;
    private TextView currSongName,timeListening;
    private ImageView prev,next,toPlay,playOrder_view,menu;
    private Handler handler,letterLVHandler;
    private int index=0,selected_to_delete;

    private SearchView searchView;

    private List<String> songFirstLetters,lettersList;
    private final String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
            "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private KrcFileInfo krcFileInfo;
    private List<KrcFileInfo> krcFiles;
    private List<String> lrcs;
    private List<Integer> times;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;


        seekbar = (SeekBar)findViewById(R.id.seekbar);

        currSongName = (TextView)findViewById(R.id.currSongName);
        timeListening = (TextView)findViewById(R.id.timeListening);
        prev = (ImageView)findViewById(R.id.prev);
        next = (ImageView)findViewById(R.id.next);
        toPlay = (ImageView)findViewById(R.id.toPlay);


        list = new ArrayList<>();

        initTextView();
        initViewPager();
        initImageView();
        initSearchView();
        topRightMenuControl();
        bottomMusicControl();
    }

    private void initTextView(){
        playing = (TextView)findViewById(R.id.text1);
        songs = (TextView)findViewById(R.id.text2);

        songs.setOnClickListener(new MyOnClickListener(0));
        playing.setOnClickListener(new MyOnClickListener(1));
    }

    private void initViewPager(){
        vPager = (ViewPager)findViewById(R.id.vPager);

        //添加页卡
        listViews = new ArrayList<View>();
        LayoutInflater mInflater = this.getLayoutInflater();
        listViews.add(mInflater.inflate(R.layout.activity_songs_list,null));
        listViews.add(mInflater.inflate(R.layout.activity_current_play,null));

        vPager.setAdapter(new MyPageAdapter(listViews));
        vPager.setCurrentItem(0);
        vPager.setOnPageChangeListener(new MyOnPageChangeListener());

        //操作控制各页卡内容
        songsListControl();
        try {
            currSongLrc();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initImageView(){
        cursor = (ImageView)findViewById(R.id.cursor);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_remove_blue_40dp,options);
        bmpW = options.outWidth;// 获取图片宽度

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        cursor.setImageMatrix(matrix);// 设置动画初始位置
    }

    private void initSearchView(){
        searchView = (SearchView)findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                String selection = MediaStore.Audio.Media.TITLE + " LIKE '%" + queryText + "%' " + " OR "
                        + MediaStore.Audio.Media.ARTIST + " LIKE '%" + queryText + "%' ";

                scanMusic(selection);
                sortSongName();
                adapter.setCurrentItem(-1);
                //通知ListView改变状态
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    //头标点击监听
    public class MyOnClickListener implements View.OnClickListener{
        private int index = 0;

        public MyOnClickListener(int i){
            index = i;
        }

        @Override
        public void onClick(View v) {
            vPager.setCurrentItem(index);
        }
    }

    //ViewPager适配器
    public class MyPageAdapter extends PagerAdapter{
        public List<View> mListViews;

        public MyPageAdapter(List<View> mListViews){
            this.mListViews = mListViews;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == (o);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            //super.destroyItem(container, position, object);
            ((ViewPager)container).removeView(mListViews.get(position));
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            //super.finishUpdate(container);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public void restoreState(@Nullable Parcelable state, @Nullable ClassLoader loader) {
            //super.restoreState(state, loader);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            //return super.instantiateItem(container, position);
            ((ViewPager)container).addView(mListViews.get(position),0);
            return mListViews.get(position);
        }

        @Nullable
        @Override
        public Parcelable saveState() {
            //return super.saveState();
            return null;
        }

        @Override
        public void startUpdate(@NonNull ViewGroup container) {
            //super.startUpdate(container);
        }
    }

    /**
     * 页卡切换监听
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int translateX = offset * 4 + bmpW;// 页卡1 -> 页卡2 偏移量
        //int two = translateX * 2;// 页卡1 -> 页卡3 偏移量

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(offset * 2 + bmpW, 0, 0, 0);

                    }
                    /*else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, 0, 0, 0);
                    }*/
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, offset * 2 + bmpW, 0, 0);
                    }
                    /*else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, one, 0, 0);
                    }*/
                    break;
                /*case 2:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, two, 0, 0);
                    } else if (currIndex == 1) {
                        animation = new TranslateAnimation(one, two, 0, 0);
                    }
                    break;*/
            }
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(500);
            cursor.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    //扫描本地音乐
    private void scanMusic(String selection){
        mListView = (ListView) listViews.get(0).findViewById(R.id.mListView);
        list = new ArrayList<>();
        //把扫描到的音乐赋值给list
        list = MusicList.getMusicData(MainActivity.this,selection);
        adapter = new MyAdapter(MainActivity.this,list);
        mListView.setAdapter(adapter);
        mListView.setTextFilterEnabled(true);

    }

    //歌名排序
    public void sortSongName(){
        songFirstLetters = new ArrayList<>();
        ArrayList<String> nameEnList = new ArrayList<String>();
        ArrayList<Song> nameSort = new ArrayList<Song>(list.size());
        for (int i=0;i<list.size();i++){
            String convertoL = new ConverToLetter().String2Alpha(list.get(i).getSong(),"s");
            if (convertoL.substring(0,1).equals("0")){
                convertoL = convertoL.substring(1);
            }
            nameEnList.add(convertoL+":"+i);
        }
        Collections.sort(nameEnList,Collator.getInstance(Locale.ENGLISH));
        for (int i = 0;i < nameEnList.size();i++){
            String[] attr = nameEnList.get(i).split(":");
            int index = Integer.parseInt(attr[1]);
            nameSort.add(list.get(index));
            songFirstLetters.add(nameEnList.get(i).substring(0,1).toUpperCase());
        }
        list.clear();
        list.addAll(nameSort);

        Thread thread = new FindCurrentPlaying();
        thread.start();
        adapter.setCurrentItem(currentPlaying);
        //通知ListView改变状态
        adapter.notifyDataSetChanged();

    }

    //右侧首字母模板
    private void letterModel(){

        lettersList = new ArrayList<>();
        for (String l:letters){
            lettersList.add(l);
        }
        letterListView = (ListView)listViews.get(0).findViewById(R.id.letterListView);
        letterAdapter = new LrcAdapter(MainActivity.this,lettersList);
        letterListView.setAdapter(letterAdapter);
        letterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Thread thread1 = new ScrollToPosition(mListView,songFirstLetters.indexOf(lettersList.get(position)),0);
                thread1.start();
                Thread thread2 = new ScrollToPosition(letterListView,position,0);
                thread2.start();
                letterAdapter.setCurrentItem(position);
                //通知ListView改变状态
                letterAdapter.notifyDataSetChanged();
            }
        });
    }

    //音乐的播放
    private void musicPlay(int position) {
        if (!mplayer.isPlaying()){
            toPlay.setImageResource(R.drawable.ic_pause);
        }
        try {
            mplayer.reset();
            mplayer = new MediaPlayer();
            mplayer.setDataSource(list.get(position).getPath());
            mplayer.prepare();
            mplayer.start();

            if (currentPlaying>=0){
                adapter.setCurrentItem(currentPlaying);
                //通知ListView改变状态
                adapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        currSongName.setText(list.get(currentPlaying).getSong());

        index = 0;
        handler = new TimeHandle();
        Thread thread = new SeekBarThread();
        thread.start();

        showLyrics();
    }

    // 上一曲
    private void frontMusic() {
        currentPlaying --;
        if (currentPlaying < 0) {
            currentPlaying = list.size() - 1;
        }
        musicPlay(currentPlaying);
    }

    // 下一曲
    private void nextMusic() {
        currentPlaying ++;
        if (currentPlaying > list.size()-1) {
            currentPlaying = 0;
        }
        musicPlay(currentPlaying);
    }

    //随机播放
    private void randomPlay(){
        currentPlaying = (int)(Math.random()*list.size());
        musicPlay(currentPlaying);
    }

    //设置播放顺序
    private void setPlayOrder(String order){
        switch (order){
            case "inOrder":
                nextMusic();
                break;
            case "inRandom":
                randomPlay();
                break;
            default:
                musicPlay(currentPlaying);
                break;
        }
    }

    //操作控制右上角按钮
    private void topRightMenuControl(){
        menu = (ImageView)findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v,R.menu.top_right_menu_item);
            }
        });
    }

    //操作控制底部音乐栏
    private void bottomMusicControl(){

            toPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mplayer.isPlaying()){
                        mplayer.pause();
                        toPlay.setImageResource(R.drawable.ic_play_arrow);
                    }
                    else {
                        mplayer.start();
                        toPlay.setImageResource(R.drawable.ic_pause);
                    }
                }
            });

            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    frontMusic();
                }
            });

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPlayOrder(playOrder);
                }
            });

            playOrder_view = (ImageView)findViewById(R.id.playOrder);
            playOrder_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(v,R.menu.play_order_item);
                }
            });
    }

    //点击弹出的小菜单
    private void showPopupMenu(View view,int layoutId) {
        // View当前PopupMenu显示的相对View的位置
        PopupMenu popupMenu = new PopupMenu(this, view);
        // menu布局
        if (layoutId == R.menu.play_order_item){popupMenu.getMenuInflater().inflate(R.menu.play_order_item, popupMenu.getMenu());
            // menu的item点击事件
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.play_inOrder:
                            playOrder = "inOrder";
                            playOrder_view.setImageResource(R.drawable.ic_forward);
                            break;
                        case R.id.play_random:
                            playOrder = "inRandom";
                            playOrder_view.setImageResource(R.drawable.ic_shuffle);
                            break;
                        default:
                            playOrder = "repeatOne";
                            playOrder_view.setImageResource(R.drawable.ic_repeat_one);
                            break;
                    }
                    return false;
                }
            });
            // PopupMenu关闭事件
            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    //Toast.makeText(getApplicationContext(), "关闭PopupMenu", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // menu布局
        if (layoutId == R.menu.top_right_menu_item){
            popupMenu.getMenuInflater().inflate(R.menu.top_right_menu_item, popupMenu.getMenu());
            // menu的item点击事件
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.scan_songs:
                            scanMusic(null);
                            Toast.makeText(MainActivity.this,"扫描歌曲完成",Toast.LENGTH_LONG);
                            sortSongName();
                            break;
                        case R.id.locate_song:
                            Thread thread = new ScrollToPosition(mListView,currentPlaying,height/4);
                            thread.start();
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
            // PopupMenu关闭事件
            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    //Toast.makeText(getApplicationContext(), "关闭PopupMenu", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // menu布局
        if (layoutId == R.menu.long_click_item){
            popupMenu.getMenuInflater().inflate(R.menu.long_click_item, popupMenu.getMenu());
            // menu的item点击事件
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.delete_song:
                            mListView = (ListView) listViews.get(0).findViewById(R.id.mListView);
                            list.remove(selected_to_delete);
                            adapter = new MyAdapter(MainActivity.this,list);
                            mListView.setAdapter(adapter);
                            mListView.setTextFilterEnabled(true);
                            sortSongName();
                            mListView.setSelectionFromTop(selected_to_delete,height/4);
                            Toast.makeText(MainActivity.this,"歌曲已移除",Toast.LENGTH_LONG);
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
            // PopupMenu关闭事件
            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    //Toast.makeText(getApplicationContext(), "关闭PopupMenu", Toast.LENGTH_SHORT).show();
                }
            });
        }

        popupMenu.show();
    }

    //操作控制页卡1-歌曲列表
    public void songsListControl(){
        scanMusic(null);
        sortSongName();
        letterModel();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                if (currentPlaying == position){
                    if (mplayer.isPlaying()){
                        mplayer.pause();
                        toPlay.setImageResource(R.drawable.ic_play_arrow);
                    }else {
                        mplayer.start();
                        toPlay.setImageResource(R.drawable.ic_pause);
                    }
                }else {
                    currentPlaying = position;
                    musicPlay(currentPlaying);
                }
            }
        });

        //长按删除
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selected_to_delete = position;
                showPopupMenu(view,R.menu.long_click_item);
                return false;
            }
        });

        //一首歌播放完毕时
        mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setPlayOrder(playOrder);
            }
        });

        //匹配首字母索引
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 触摸移动时的操作
                if (event.getAction() == MotionEvent.ACTION_MOVE){
                    letterLVHandler = new LetterLVScrollHandler();
                    Thread thread = new LetterLVScrollThread();
                    thread.start();
                }
                return false;
            }
        });
        /*letterLVHandler = new LetterLVScrollHandler();
        Thread thread = new LetterLVScrollThread();
        thread.start();*/

    }

    //操作控制页卡2当前播放
    public void currSongLrc() throws IOException {
        lrcList = (ListView)listViews.get(1).findViewById(R.id.lrcList);lrcs = new ArrayList<>();
    }

    /*根据信息寻找对应的krc歌词文件*/
    public void findKrcFile(String song){
        krcFileInfo = new KrcFileInfo();
        krcFiles = new ArrayList<>();
        String[] projection = new String[]{MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE};
        Cursor cursor = getContentResolver().query(
                Uri.parse("content://media/external/file"), projection,
                MediaStore.Files.FileColumns.DATA + " like ?", new String[]{"%"+song+"%.krc"}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idindex = cursor
                        .getColumnIndex(MediaStore.Files.FileColumns._ID);
                int dataindex = cursor
                        .getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int sizeindex = cursor
                        .getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                do {
                    krcFileInfo.setId(cursor.getString(idindex));
                    krcFileInfo.setPath(cursor.getString(dataindex));
                    krcFileInfo.setSize(cursor.getString(sizeindex));
                    krcFiles.add(krcFileInfo);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        return;
    }

    //*解析拆分歌词*/
    public void splitLrc(String lrcText){
        Lyric lyric = new Lyric();
        List<Lyric> lyrics = new ArrayList<>();

        String[] str1 = lrcText.split("\\[");
        for (String s1:str1){
            String[] str2 = s1.split("\\]");
            if (str2.length ==2 && str2[1].length()>2){
                String time = (str2[0].split(","))[0];
                String lrc = "";
                String[] str3 = str2[1].split(">");
                for (String s3:str3){
                    String[] str4 = s3.split("<");
                    for (int i=0;i<str4.length;i=i+2){
                        lrc += str4[i]+" ";
                    }
                }
                lyric.setLrcTime(Integer.parseInt(time));
                lyric.setLrcStr(lrc.substring(1));
                lyrics.add(lyric);

                lrcs.add(lrc.substring(1));
                times.add(Integer.parseInt(time));
            }
        }
        for (int i=0;i<6;i++){
            lrcs.add("");
            times.add(times.get(times.size()-1));
        }
    }

    //显示歌词
    public void showLyrics(){
        lrcs = new ArrayList<>();
        times = new ArrayList<>();
        for (int i=0;i<6;i++){
            lrcs.add("");
            times.add(0);
        }

        findKrcFile(currSongName.getText().toString());
        if (krcFiles!=null && krcFiles.size()>0){
            try {
                String lrcText = new KrcText().getKrcText(krcFiles.get(0).getPath());
                splitLrc(lrcText);
                //lrcs.add(lrcText);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            times.add(0);
            lrcs.add(currSongName.getText().toString()+" 没有找到歌词");
        }

        lrcAdapter = new LrcAdapter(MainActivity.this,lrcs);
        lrcList.setAdapter(lrcAdapter);

    }

    //获取滚动高度
    public int getScrollY() {
        View c = mListView.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int top = c.getTop();
        return -top + firstVisiblePosition * c.getHeight() ;
    }

    public class SeekbarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser==true){
                mplayer.seekTo(progress);
                timeListening.setText(MusicList.formatTime(progress)+"/"+MusicList.formatTime(mplayer.getDuration()));

                handler = new TimeHandle();
                Thread thread = new SeekBarThread();
                thread.start();
            }
        }
    }

    public class ScrollToPosition extends Thread{
        ListView listView;
        int position,height;
        public ScrollToPosition(ListView listView,int position,int height){
            this.listView = listView;
            this.position = position;
            this.height = height;
        }
        @Override
        public void run() {
            listView.smoothScrollToPositionFromTop(position,height);
            super.run();
        }
    }

    public class SeekBarThread extends Thread{
        @Override
        public void run() {
            while (mplayer.getCurrentPosition()<=list.get(currentPlaying).getDuration()){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int currentPosition = mplayer.getCurrentPosition();
                int duration = list.get(currentPlaying).getDuration();

                Message msg = handler.obtainMessage();
                msg.arg1 = currentPosition;
                msg.arg2 = duration;
                handler.sendMessage(msg);
                super.run();
            }
        }
    }

    public class TimeHandle extends Handler{
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.arg1;
            int duration = msg.arg2;

            timeListening.setText(MusicList.formatTime(currentPosition)+"/"+MusicList.formatTime(duration));

            //控制歌词滚动
            if (index<times.size()){
                if (currentPosition <times.get(index)-300) {
                    Thread thread = new ScrollToPosition(lrcList,index-1,height/4);
                    thread.start();
                }else {
                    index++;
                }
            }
            lrcAdapter.setCurrentItem(index-1);
            //通知ListView改变状态
            lrcAdapter.notifyDataSetChanged();

            seekbar.setMax(duration);
            seekbar.setProgress(currentPosition);
            seekbar.setOnSeekBarChangeListener(new SeekbarChangeListener());
            super.handleMessage(msg);
        }
    }

    public class LetterLVScrollThread extends Thread{
        @Override
        public void run() {
            Message msg = letterLVHandler.obtainMessage();
            int firstVisiblePosition = mListView.getFirstVisiblePosition();
            msg.what = firstVisiblePosition;
            letterLVHandler.sendMessage(msg);
            super.run();
        }
    }

    public class LetterLVScrollHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            int firstVisiblePosition = msg.what;
            int i = lettersList.indexOf(songFirstLetters.get(firstVisiblePosition));
            Thread thread = new ScrollToPosition(letterListView,i,0);
            thread.start();
            letterAdapter.setCurrentItem(i);
            //通知ListView改变状态
            letterAdapter.notifyDataSetChanged();
            super.handleMessage(msg);
        }
    }

    public class FindCurrentPlaying extends Thread{
        @Override
        public void run() {
            int currPlaying = -1;
            for(int i = 0;i < list.size();i++){
                if (list.get(i).getSong().equals(currSongName.getText())){
                    currPlaying = i;
                    break;
                }
            }
            if (currPlaying != currentPlaying){
                currentPlaying = currPlaying;
            }
            adapter.setCurrentItem(currentPlaying);
            //通知ListView改变状态
            adapter.notifyDataSetChanged();
            super.run();
        }
    }


}

package com.example.asus.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FindLrc {

    public Context context;
    private String lrc = "";
    private KrcFileInfo krcFileInfo;
    private List<KrcFileInfo> krcFiles;
    private String lrcText;

    final String TAG = "MainActivity";

    /*@Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showLrc = (TextView)findViewById(R.id.tv_show);

        findKrcFile("在这世界上没有天使");
        try {
            lrcText = new KrcText().getKrcText(krcFiles.get(0).getPath());
            splitLrc(lrcText);
            showLrc.setText(lrc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*根据信息寻找对应的krc歌词文件*/
    public List<KrcFileInfo> findKrcFile(String song){
        krcFileInfo = new KrcFileInfo();
        krcFiles = new ArrayList<>();
        String[] projection = new String[]{MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE};
        Cursor cursor = context.getContentResolver().query(
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
        return krcFiles;
    }

    /*解析拆分歌词*/
    public List<Lyric> splitLrc(String lrcText){
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
                    lrc += s3.substring(0,1);
                }
                lyric.setLrcTime(Integer.parseInt(time));
                lyric.setLrcStr(lrc.substring(1));
                lyrics.add(lyric);
            }
        }
        return lyrics;
    }
}

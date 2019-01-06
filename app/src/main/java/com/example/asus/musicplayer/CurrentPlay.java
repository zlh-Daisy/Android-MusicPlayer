package com.example.asus.musicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CurrentPlay extends Activity{
    private TextView lrcList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_play);

        lrcList = (TextView)findViewById(R.id.lrcList);
        lrcList.setText("修改后");
    }
}

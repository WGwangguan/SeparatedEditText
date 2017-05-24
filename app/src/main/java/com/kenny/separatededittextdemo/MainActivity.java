package com.kenny.separatededittextdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kenny.separatededittext.SeparatedEditText;

public class MainActivity extends AppCompatActivity {

    SeparatedEditText solid;
    SeparatedEditText hollow;
    SeparatedEditText underline;

    boolean showContent;
    boolean showCursor = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        solid = (SeparatedEditText) findViewById(R.id.edit_solid);
        hollow = (SeparatedEditText) findViewById(R.id.edit_hollow);
        underline = (SeparatedEditText) findViewById(R.id.edit_underline);

    }


    public void handleContent(View v) {
        solid.setPassword(!showContent);
        hollow.setPassword(!showContent);
        underline.setPassword(!showContent);
        showContent = !showContent;
    }

    public void handleCursor(View v) {
        solid.setShowCursor(!showCursor);
        hollow.setShowCursor(!showCursor);
        underline.setShowCursor(!showCursor);
        showCursor = !showCursor;
    }

}

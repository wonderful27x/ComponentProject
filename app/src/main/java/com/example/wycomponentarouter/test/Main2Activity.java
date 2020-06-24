package com.example.wycomponentarouter.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.annotation.ARouter;
import com.example.wycomponentarouter.R;

public class Main2Activity extends AppCompatActivity {

    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
}

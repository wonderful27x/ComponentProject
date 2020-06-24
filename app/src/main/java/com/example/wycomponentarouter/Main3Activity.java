package com.example.wycomponentarouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.annotation.ARouter;

@ARouter(path = "/app/Main3Activity")
public class Main3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
    }
}

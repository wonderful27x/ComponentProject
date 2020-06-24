package com.example.wycomponentarouter.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.annotation.ARouter;

@ARouter(path = "/personal/Personal2MainActivity")
public class Personal2MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal2_activity_main);
    }
}

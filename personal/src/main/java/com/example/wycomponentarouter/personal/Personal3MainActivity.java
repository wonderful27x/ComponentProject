package com.example.wycomponentarouter.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.annotation.ARouter;

@ARouter(path = "/personal/Personal3MainActivity")
public class Personal3MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal3_activity_main);
    }
}

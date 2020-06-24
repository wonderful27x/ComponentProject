package com.example.wycomponentarouter.order;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.annotation.ARouter;
import com.example.annotation.Parameter;
import com.example.arouter_api.ParameterManager;
import com.example.arouter_api.RouterManager;
import com.example.wycomponentarouter.common.base.BaseActivity;
import com.example.wycomponentarouter.common.utils.Constant;

@ARouter(path = "/order/OrderMainActivity")
public class OrderMainActivity extends BaseActivity {

    @Parameter
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_main);
        Log.d(Constant.PATH,"OrderMainActivity");

        //参数的初始化
        ParameterManager.getInstance().loadParameter(this);
        Toast.makeText(this,"收到参数： " + name,Toast.LENGTH_SHORT).show();

        findViewById(R.id.app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpToApp();
            }
        });

        findViewById(R.id.personal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpToPersonal();
            }
        });

//        OrderMainActivity$$Parameter parameter = new OrderMainActivity$$Parameter();
//        parameter.loadParameter(this);
//        Log.d("Parameter",name);
    }

    //TODO 子module无法直接跳转到主app，因为他没有依赖主app，这是解耦必须的,使用路由跳转
    private void jumpToApp(){
        RouterManager.getInstance().build("/app/MainActivity")
                .withString("name","MainActivity")
                .navigation(this);
    }

    //TODO 子module无法直接跳转到另一个子module，因为他没有依赖子module，这是解耦必须的,使用路由跳转
    private void jumpToPersonal(){
        RouterManager.getInstance().build("/personal/PersonalMainActivity")
                .withString("name","PersonalMainActivity")
                .navigation(this);
    }
}

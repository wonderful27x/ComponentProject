package com.example.wycomponentarouter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.annotation.ARouter;
import com.example.annotation.Parameter;
import com.example.annotation.RouterBean;
import com.example.arouter_api.ARouterLoadGroup;
import com.example.arouter_api.ARouterLoadPath;
import com.example.arouter_api.ParameterManager;
import com.example.arouter_api.RouterManager;
import com.example.wycomponentarouter.apt.ARouter$$Group$$order;
import com.example.wycomponentarouter.apt.ARouter$$Group$$personal;
import com.example.wycomponentarouter.common.base.BaseActivity;
import com.example.wycomponentarouter.common.utils.Constant;
import java.util.Map;

/**
 * android组件化，APT+JAVA-POET的路由架构，实现子模块间的跳转交互
 */
@ARouter(path = "/app/MainActivity")
public class MainActivity extends BaseActivity {

    @Parameter
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(Constant.PATH,"MainActivity");

        //参数的初始化
        ParameterManager.getInstance().loadParameter(this);
        Toast.makeText(this,"收到参数： " + name,Toast.LENGTH_SHORT).show();

        findViewById(R.id.order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpToOrder();
            }
        });

        findViewById(R.id.personal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpToPersonal();
            }
        });
    }

    //组件化间的交互
    private void jumpToOrder(){
        //TODO 从主app跳转到子module中是可以像正常情况下那样跳转的，因为主app是依赖了子module的
//        Intent intent = new Intent(this, OrderMainActivity.class);
//        startActivity(intent);

        //TODO 使用路由的形式跳转
//        ARouterLoadGroup group$$Order = new ARouter$$Group$$order();
//        //加载order组
//        Map<String,Class<? extends ARouterLoadPath>> map = group$$Order.loadGroup();
//        //获取根据组名获取order组
//        Class<? extends ARouterLoadPath> clazz = map.get("order");
//        try {
//            ARouterLoadPath path$$Order = clazz.newInstance();
//            //加载路由信息
//            Map<String, RouterBean> routerBeanMap = path$$Order.loadPath();
//            //获取指定路径下的类信息
//            RouterBean routerBean = routerBeanMap.get("/order/OrderMainActivity");
//            //获取跳转类
//            Class jumpClass = routerBean.getClazz();
//            Intent intent = new Intent(MainActivity.this,jumpClass);
//            intent.putExtra("name","wonderful");
//            startActivity(intent);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        }

        //TODO 使用路由管理跳转
        RouterManager.getInstance().build("/order/OrderMainActivity")
                .withString("name","OrderMainActivity")
                .navigation(this);
    }

    private void jumpToPersonal(){
        //TODO 从主app跳转到子module中是可以像正常情况下那样跳转的，因为主app是依赖了子module的
//        Intent intent = new Intent(this, PersonalMainActivity.class);
//        startActivity(intent);

        //TODO 使用路由的形式跳转
//        ARouterLoadGroup group$$Order = new ARouter$$Group$$personal();
//        //加载order组
//        Map<String,Class<? extends ARouterLoadPath>> map = group$$Order.loadGroup();
//        //获取根据组名获取order组
//        Class<? extends ARouterLoadPath> clazz = map.get("personal");
//        try {
//            ARouterLoadPath path$$Order = clazz.newInstance();
//            //加载路由信息
//            Map<String, RouterBean> routerBeanMap = path$$Order.loadPath();
//            //获取指定路径下的类信息
//            RouterBean routerBean = routerBeanMap.get("/personal/PersonalMainActivity");
//            //获取跳转类
//            Class jumpClass = routerBean.getClazz();
//            Intent intent = new Intent(MainActivity.this,jumpClass);
//            startActivity(intent);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        }

        //TODO 使用路由管理跳转
        RouterManager.getInstance().build("/personal/PersonalMainActivity")
                .withString("name","PersonalMainActivity")
                .navigation(this);
    }
}

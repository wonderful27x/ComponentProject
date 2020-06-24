package com.example.wycomponentarouter.test;

import com.example.annotation.RouterBean;
import com.example.arouter_api.ARouterLoadPath;
import com.example.wycomponentarouter.order.OrderMainActivity;

import java.util.HashMap;
import java.util.Map;

//模拟APT生成类,注意这里生成的是当前组内所有的被ARouter注解的类，这里只模拟了一个OrderMainActivity
public class ARouter$$Path$$Order implements ARouterLoadPath {
    @Override
    public Map<String, RouterBean> loadPath() {
        Map<String,RouterBean> map = new HashMap<>();
        map.put("order/OrderMainActivity",
                RouterBean.create(RouterBean.Type.ACTIVITY,
                        "order",
                        "order/OrderMainActivity",
                        OrderMainActivity.class)
        );
        return map;
    }
}

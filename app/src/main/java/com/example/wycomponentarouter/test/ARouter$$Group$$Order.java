package com.example.wycomponentarouter.test;

import com.example.arouter_api.ARouterLoadGroup;
import com.example.arouter_api.ARouterLoadPath;

import java.util.HashMap;
import java.util.Map;

//模拟APT生成类
public class ARouter$$Group$$Order implements ARouterLoadGroup {
    @Override
    public Map<String, Class<? extends ARouterLoadPath>> loadGroup() {
        Map<String,Class<? extends ARouterLoadPath>> map = new HashMap<>();
        map.put("order",ARouter$$Path$$Order.class);
        return map;
    }
}

package com.example.arouter_api;

import com.example.annotation.RouterBean;
import java.util.Map;

/**
 * 路由信息加载接口，加载当前组对应路径下的类，并返回一个Map，这个Map就拥有了刚才加载的类信息，
 * 再通过get，获取相应路径下的类，最后完成交互
 * RouterBean封装了我们想要交互的类的信息
 */
public interface ARouterLoadPath {
    public Map<String, RouterBean> loadPath();
}

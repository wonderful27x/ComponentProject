package com.example.arouter_api;

import java.util.Map;

/**
 * 路由组加载接口，加载对应的组，并返回一个Map，这个Map就已经拥有了刚才加载的组，
 * 再通过get就能获取对应组,如order组，这个order组就是能够获取到order对应的所有交互类的一个类信息
 * ARouterLoadPath就是对应了组，通过这个ARouterLoadPath就能获取其对应组下的需要交互的类信息
 */
public interface ARouterLoadGroup {
    public Map<String,Class<? extends ARouterLoadPath>> loadGroup();
}

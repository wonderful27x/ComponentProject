package com.example.arouter_api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.LruCache;
import com.example.annotation.RouterBean;

/**
 * 使用apt已经生成了module间跳转的关键代码Group和Path，以及参数传递的Parameter
 * 但是真正跳转时如何拿到对方的Group和Path仍然比较麻烦，这是为了解耦module之间没有互相依赖的原因
 * 所有我们需要通过反射来寻找Group和Path，RouterManager的作用就是找到Group和Path并做跳转动作
 * 他将繁琐的逻辑封装了，方便使用
 */
public class RouterManager {
    private static RouterManager routerManager = null;

    private String group;
    private String path;
    private LruCache<String,ARouterLoadGroup> routerGroupCache;
    private LruCache<String,ARouterLoadPath> routerPathCache;

    //属组固定前缀
    private static final String PREFIX = "ARouter$$Group$$";

    public static RouterManager getInstance(){
        if (routerManager == null){
            synchronized (RouterManager.class){
                if (routerManager == null){
                    routerManager = new RouterManager();
                }
            }
        }
        return routerManager;
    }

    private RouterManager(){
        routerGroupCache = new LruCache<>(100);
        routerPathCache = new LruCache<>(100);
    }

    /**
     * 构造一个BundleManager来传递参数
     * @param path 路由路径
     * @return
     */
    public BundleManager build(String path){
        //校验用户传递的路由路径，格式是固定的，/group/target
        if (TextUtils.isEmpty(path) || !path.startsWith("/")){
            throw new IllegalArgumentException("你在玩锤子，正确写法如：/order/OrderMainActivity!");
        }
        if (path.lastIndexOf("/") == 0){
            throw new IllegalArgumentException("你在玩锤子，正确写法如：/order/OrderMainActivity!");
        }
        //截取组名 /order/OrderMainActivity -> order
        String groupName = path.substring(1,path.lastIndexOf("/"));
        if (TextUtils.isEmpty(groupName)){
            throw new IllegalArgumentException("你在玩锤子，正确写法如：/order/OrderMainActivity!");
        }
        this.group = groupName;
        this.path = path;
        return new BundleManager();
    }

    //开始处理导航功逻辑
    public void navigation(Context context, BundleManager bundleManager) {
        //拼接属组的类名，注意我们生成的apt文件的包名是在主app的包名下的apt包下，所以我们可以这样拼接
        String groupClassName = context.getPackageName() + ".apt." + PREFIX + group;
        try {
            //先获取数组
            ARouterLoadGroup loadGroup = routerGroupCache.get(group);
            if (loadGroup == null){
                Class groupClass = Class.forName(groupClassName);
                loadGroup = (ARouterLoadGroup) groupClass.newInstance();
                routerGroupCache.put(group,loadGroup);
            }
            if (loadGroup.loadGroup().isEmpty()){
                throw new RuntimeException("路由表Group报废了！");
            }
            //通过数组获取路由表
            ARouterLoadPath loadPath = routerPathCache.get(path);
            if (loadPath == null){
                Class<? extends ARouterLoadPath> pathClass = loadGroup.loadGroup().get(group);
                loadPath =  pathClass.newInstance();
                routerPathCache.put(path,loadPath);
            }
            if (loadPath.loadPath().isEmpty()){
                throw new RuntimeException("路由表Path报废了！");
            }
            //根据路由路径从路由表中取出对应的class
            RouterBean routerBean = loadPath.loadPath().get(path);
            if (routerBean == null)return;
            switch (routerBean.getType()){
                case ACTIVITY:
                    Intent intent = new Intent(context,routerBean.getClazz());
                    intent.putExtras(bundleManager.getBundle());
                    context.startActivity(intent);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

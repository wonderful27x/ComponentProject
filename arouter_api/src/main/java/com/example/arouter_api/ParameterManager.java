package com.example.arouter_api;

import android.app.Activity;
import android.util.Log;
import android.util.LruCache;

/**
 * 参数传递管理类，简化参数传递的代码编写，注意我们生成的apt文件是和Activity在同包下的
 */
public class ParameterManager {

    private static final String TAG = "ParameterManager";

    private static ParameterManager parameterManager = null;
    private LruCache<String,ParameterLoad> parameterLoadCache;
    //APT文件后缀
    private static final String SUFFIX = "$$Parameter";

    public static ParameterManager getInstance(){
        if (parameterManager == null){
            synchronized (ParameterManager.class){
                if (parameterManager == null){
                    parameterManager = new ParameterManager();
                }
            }
        }
        return parameterManager;
    }

    private ParameterManager(){
        parameterLoadCache = new LruCache<>(100);
    }

    public void loadParameter(Activity activity){
        String className = activity.getClass().getName() + SUFFIX;
        Log.d(TAG, "className: " + className);
        try {
            ParameterLoad parameterLoad = parameterLoadCache.get(className);
            if (parameterLoad == null){
                Class clazz = Class.forName(className);
                parameterLoad = (ParameterLoad) clazz.newInstance();
                parameterLoadCache.put(className,parameterLoad);
            }
            parameterLoad.loadParameter(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

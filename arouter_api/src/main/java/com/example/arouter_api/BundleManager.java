package com.example.arouter_api;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 参数传递管理类
 */
public class BundleManager {
    private Bundle bundle;

    public BundleManager(){
        bundle = new Bundle();
    }

    public BundleManager withString(@NonNull String key, @Nullable String vale){
        bundle.putString(key,vale);
        return this;
    }

    public BundleManager widthInt(@NonNull String key,int value){
        bundle.putInt(key,value);
        return this;
    }

    public BundleManager witBoolean(@NonNull String key,boolean value){
        bundle.putBoolean(key,value);
        return this;
    }

    public BundleManager witBundle(Bundle bundle){
        this.bundle = bundle;
        return this;
    }

    //开始导航，真正的逻辑交给RouterManager
    public void navigation(Context context){
        RouterManager.getInstance().navigation(context,this);
    }

    public Bundle getBundle() {
        return bundle;
    }
}

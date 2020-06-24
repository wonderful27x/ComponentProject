package com.example.wycomponentarouter.test;

import com.example.arouter_api.ParameterLoad;
import com.example.wycomponentarouter.MainActivity;

//参数注解模拟类,这个文件需要和MainActivity同包，否则拿不到默认属性的字段
public class Main2Activity$$Parameter implements ParameterLoad {
    @Override
    public void loadParameter(Object object) {
        Main2Activity target = (Main2Activity) object;
        target.name = target.getIntent().getStringExtra("S");
        target.getIntent().getIntExtra("name",10);
        target.getIntent().getBooleanExtra("name",false);
    }
}

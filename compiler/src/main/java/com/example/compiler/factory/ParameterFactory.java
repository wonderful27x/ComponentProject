package com.example.compiler.factory;

import com.example.annotation.Parameter;
import com.example.compiler.utils.Constant;
import com.example.compiler.utils.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

//用于生成Parameter注解处理器方法的工厂
public class ParameterFactory {

    private MethodSpec.Builder methodBuilder;  //方法构造类
    private ParameterSpec parameterSpec;       //方法参数
    private ClassName className;               //类节点ClassName
    private Messager messager;                 //日志打印

    private ParameterFactory(){

    }

    private ParameterFactory create(){
        methodBuilder = MethodSpec.methodBuilder(Constant.PARAMETER_METHOD) //方法名
                .addAnnotation(Override.class)                              //注解
                .addModifiers(Modifier.PUBLIC)                              //public属性
                .addParameter(parameterSpec);                               //方法参数
        return this;
    }

    //添加方法体第一行内容 MainActivity target = (MainActivity) object;
    public void addFirstStatemet(){
        methodBuilder.addStatement(
                "$T target = ($T) $N",
                className,
                className,
                Constant.PARAMETER_NAME
        );
    }

    /**
     * 实现参数赋值 target.name = target.getIntent().getStringExtra("S");
     * 每个element节点对应一个参数，这个方法会被循环调用
     */
    public void buildStatement(Element element){
        //获取描述
        TypeMirror typeMirror = element.asType();
        //获取枚举类型的序号
        int type = typeMirror.getKind().ordinal();
        //获取属性名
        String fieldName = element.getSimpleName().toString();
        //获取属性注解的值
        String annotationValue = element.getAnnotation(Parameter.class).name();
        //判断注解值是否为空，如果为空将属性名作为key，否则注解值为key
        String key = EmptyUtils.isEmpty(annotationValue) ? fieldName : annotationValue;
        //方法体前缀
        String preMethod = "target." + fieldName;
        //方法体
        String methodContent = preMethod + " = target.getIntent().";
        if (type == TypeKind.INT.ordinal()){
            //target.name = target.getIntent().getIntExtra("name",target.name);
            methodContent += "getIntExtra($S," + preMethod + ")";
        }else if (type == TypeKind.BOOLEAN.ordinal()){
            //target.name = target.getIntent().getBooleanExtra("name",target.name);
            methodContent += "getBooleanExtra($S," + preMethod + ")";
        }else {
            //TypeKind不包含String类型
            if (typeMirror.toString().equalsIgnoreCase(Constant.STRING)){
                //target.name = target.getIntent().getStringExtra("S");
                methodContent += "getStringExtra($S)";
            }
        }

        if (methodContent.endsWith(")")){
            methodBuilder.addStatement(methodContent,key);
        }else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String、int、boolean传参");
        }
    }

    public MethodSpec buildMethodSpec(){
        return methodBuilder.build();
    }

    public static class Builder{
        private ParameterFactory factory;
        public Builder(){
            factory = new ParameterFactory();
        }

        public Builder setClassName(ClassName className){
            factory.className = className;
            return this;
        }

        public Builder setMessager(Messager messager){
            factory.messager = messager;
            return this;
        }

        public Builder setParameter(ParameterSpec parameterSpec){
            factory.parameterSpec = parameterSpec;
            return this;
        }

        public ParameterFactory createFactory(){
            try{
                if(factory.className == null){
                    throw new IllegalArgumentException("方法内容className为空！");
                }
                if(factory.parameterSpec == null){
                    throw new IllegalArgumentException("方法体参数parameterSpec为空！");
                }
                if(factory.messager == null){
                    throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息！");
                }

                return factory.create();
            }finally {
                factory = null;
            }
        }
    }
}

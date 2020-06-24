package com.example.compiler;

import com.example.annotation.Parameter;
import com.example.compiler.factory.ParameterFactory;
import com.example.compiler.utils.Constant;
import com.example.compiler.utils.EmptyUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * 参数传递的注解处理器
 */
// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
// 允许/支持的注解类型，让注解处理器处理（新增annotation module）
@SupportedAnnotationTypes(Constant.ANNOTATION_PARAMETER)
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParameterProcessor extends AbstractProcessor {

    // 操作Element工具类 (类、函数、属性都是Element)
    private Elements elementsUtil;
    // type(类信息)工具类，包含用于操作TypeMirror的工具方法
    private Types typeUtils;
    // Messager用来报告错误，警告和其他提示信息
    private Messager messager;
    // 文件生成器 类/资源，Filter用来创建新的源文件，class文件以及辅助文件
    private Filer filer;
    //临时map，用于存储注解的参数，最后遍历生成java文件
    //key：类节点，value：类节点下所有被注解的参数
    private Map<TypeElement, List<Element>> map = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        // 父类受保护属性，可以直接拿来使用。
        // 其实就是init方法的参数ProcessingEnvironment
        elementsUtil = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (EmptyUtils.isEmpty(set))return false;
        //获取被@Parameter注解的元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
        if (EmptyUtils.isEmpty(elements))return false;
        //解析elements集合
        valueOfParameterMap(elements);
        //生成java文件
        try {
            createParameterFile();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //遍历map生产java文件
    private void createParameterFile() throws Exception{
        if (EmptyUtils.isEmpty(map))return;
        //activityType获取类型
        TypeElement activityType = elementsUtil.getTypeElement(Constant.ACTIVITY);
        //parameter接口ParameterLoad
        TypeElement parameterType = elementsUtil.getTypeElement(Constant.PARAMETER_INTERFACE);
        //需要生成的java文件的方法参数配置
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT,Constant.PARAMETER_NAME).build();
        for (Map.Entry<TypeElement,List<Element>> entry:map.entrySet()){
            //对应的父节点，即类节点
            TypeElement typeElement = entry.getKey();
            if (!typeUtils.isSubtype(typeElement.asType(),activityType.asType())){
                throw new RuntimeException("@Parameter注解仅限于作用在Activity类的字段上！");
            }
            //获取ClassName
            ClassName className = ClassName.get(typeElement);
            //构造方法
            ParameterFactory factory =  new ParameterFactory.Builder()
                    .setClassName(className)
                    .setMessager(messager)
                    .setParameter(parameterSpec)
                    .createFactory();
            //添加方法体第一行内容，这里基本是固定的
            factory.addFirstStatemet();
            //为每个注解的字段赋值
            for (Element element:entry.getValue()){
                factory.buildStatement(element);
            }
            MethodSpec methodSpec = factory.buildMethodSpec();

            //生成类名
            String finalClassName = typeElement.getSimpleName() + Constant.CLASS_NAME_PARAMETER;

            //构建类
            TypeSpec typeSpec = TypeSpec.
                    classBuilder(finalClassName)                         //类名
                    .addSuperinterface(ClassName.get(parameterType))     //实现的接口
                    .addModifiers(Modifier.PUBLIC)                       //public属性
                    .addMethod(methodSpec)                               //方法
                    .build();

            //生成类文件,注意这里生成的文件必须放在与注解同包下
            JavaFile javaFile = JavaFile.builder(className.packageName(),typeSpec).build();
            javaFile.writeTo(filer);
        }
    }

    //解析所有elements，将父节点即其对应的注解属性存储起来
    private void valueOfParameterMap(Set<? extends Element> elements) {
        for (Element element:elements){
            //element是字段注解，获取其父节点得到类节点
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            if (map.containsKey(typeElement)){
                map.get(typeElement).add(element);
            }else {
                List<Element> params = new ArrayList<>();
                params.add(element);
                map.put(typeElement,params);
            }
        }
    }
}

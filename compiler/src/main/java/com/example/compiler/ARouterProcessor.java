package com.example.compiler;

import com.example.annotation.ARouter;
import com.example.annotation.RouterBean;
import com.example.compiler.utils.Constant;
import com.example.compiler.utils.EmptyUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


/**
 * 路由注解处理器
 */
// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
// 允许/支持的注解类型，让注解处理器处理（新增annotation module）
@SupportedAnnotationTypes(Constant.ANNOTATION_AROUTER)
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
//注解处理器接收的参数,一般从其他module的gradle中传过来
@SupportedOptions({Constant.MODULE_NAME,Constant.APT_PACKAGE})
public class ARouterProcessor extends AbstractProcessor {

    // 操作Element工具类 (类、函数、属性都是Element)
    private Elements elementsUtil;
    // type(类信息)工具类，包含用于操作TypeMirror的工具方法
    private Types typeUtils;
    // Messager用来报告错误，警告和其他提示信息
    private Messager messager;
    // 文件生成器 类/资源，Filter用来创建新的源文件，class文件以及辅助文件
    private Filer filer;
    // module名称
    private String moduleName;
    // 存放APT生成文件包名
    private String packageNameForAPT;
    // 临时map，用于存储每个模块的需要生成的ARouterLoadPath
    // String:模块名，List<RouterBean>>当前模块ARouterLoadPath下所有的路由信息
    // 这个map理论上只有一个值，即当前module的ARouterLoadPath，因为不同module的ARouterProcessor是独立的
    private Map<String, List<RouterBean>> pathMap = new HashMap<>();
    // 临时map，用于存储每个模块的需要生成ARouterLoadGroup
    // String:模块名，String:ARouterLoadPath名
    // 这个map理论上只有一个值，即当前module的ARouterLoadGroup，因为不同module的ARouterProcessor是独立的
    private Map<String, String> groupMap = new HashMap<>();

    // 该方法主要用于一些初始化的操作，通过该方法的参数ProcessingEnvironment可以获取一些列有用的工具类
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        // 父类受保护属性，可以直接拿来使用。
        // 其实就是init方法的参数ProcessingEnvironment
        elementsUtil = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        // 通过ProcessingEnvironment去获取build.gradle传过来的参数
        Map<String,String> options = processingEnvironment.getOptions();
        if (!EmptyUtils.isEmpty(options)){
            moduleName = processingEnvironment.getOptions().get(Constant.MODULE_NAME);
            packageNameForAPT = processingEnvironment.getOptions().get(Constant.APT_PACKAGE);
            //打印
            messager.printMessage(Diagnostic.Kind.NOTE,moduleName + "-" + packageNameForAPT);
        }

        if(EmptyUtils.isEmpty(moduleName) || EmptyUtils.isEmpty(packageNameForAPT)){
            throw new RuntimeException("注解处理器需要的参数moduleName或者packageName为空，请在对应build.gradle配置参数");
        }

    }

    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     * @param set              使用了支持处理注解的节点集合（类 上面写了注解）
     * @param roundEnvironment 当前或是之前的运行环境,可以通过该对象查找找到的注解。
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (EmptyUtils.isEmpty(set))return false;
        //获取所有带ARouter注解的类节点
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        if (EmptyUtils.isEmpty(elements))return false;
        //解析elements
        try {
            parseElements(elements);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //解析每个ARouter注解的类节点，将关键信息存储到map中，然后遍历生成最终java文件
    private void parseElements(Set<? extends Element> elements) throws Exception{
        //通过工具类将注解类型ACTIVITY转化为自描述Mirror,用于比较
        TypeElement typeElement = elementsUtil.getTypeElement(Constant.ACTIVITY);
        TypeMirror typeMirror = typeElement.asType();

        //遍历所有节点
        for (Element element : elements) {
            //获取元素类信息
            TypeMirror mirror = element.asType();
            // 高级判断：ARouter注解仅能用在类之上，并且是规定的Activity
            // 类型工具类方法isSubtype，相当于instance一样
            if(!typeUtils.isSubtype(mirror,typeMirror)){
                throw new RuntimeException("@ARouter注解仅限于作用在Activity类之上！");
            }
            //获取注解
            ARouter aRouter = element.getAnnotation(ARouter.class);
            //创建RouterBean
            RouterBean.Builder builder = new RouterBean.Builder();
            builder.setElement(element)
                    .setGroup(aRouter.group())
                    .setPath(aRouter.path())
                    .setType(RouterBean.Type.ACTIVITY);
            RouterBean routerBean = builder.create();

            //解析并将信息存入pathMap
            valueOfPathMap(routerBean);
        }

        //获取ARouterLoadGroup和ARouterLoadPath类型，生成的java文件需要实现这两个接口
        TypeElement typeElementGroup = elementsUtil.getTypeElement(Constant.AROUTER_GROUP_NTERFACE);
        TypeElement typeElementPath = elementsUtil.getTypeElement(Constant.AROUTER_PATH_NTERFACE);

        //先生成ARouterLoadPath
        createPathFile(typeElementPath);
        //再生成ARouterLoadGroup,顺序不能变，因为ARouterLoadGroup的生成依赖ARouterLoadPath
        createGroupFile(typeElementPath,typeElementGroup);

    }

    //生成路由group，即ARouterLoadGroup
    private void createGroupFile(TypeElement typeElementPath, TypeElement typeElementGroup) throws Exception {
        if (EmptyUtils.isEmpty(groupMap))return;

        //方法返回值
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),         //Map
                ClassName.get(String.class),      //Map<String
                //第二个参数Class<? extends ARouterLoadPath>
                //某某类是否属于ARouterLoadPath接口的实现类
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(typeElementPath))
                )
        );

        //遍历groupMap Map，生成对应模块下的ARouterLoadGroup，事实上只有一个值
        for (Map.Entry<String,String> entry:groupMap.entrySet()){

            //方法配置
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constant.GROUP_METHOD)//方法名
                    .addAnnotation(Override.class)  //注解
                    .addModifiers(Modifier.PUBLIC)  //public属性
                    .returns(methodReturns);        //返回值

            //HashMap
            //Map<String,Class<? extends ARouterLoadPath>> map = new HashMap<>();
            methodBuilder.addStatement(
                    "$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(typeElementPath))
                    ),
                    Constant.GROUP_PARAMETER_NAME,
                    ClassName.get(HashMap.class)
            );

            //groupMap.put("order",ARouter$$Path$$Order.class);
            methodBuilder.addStatement(
                    "$N.put($S,$T.class)",
                    Constant.GROUP_PARAMETER_NAME,                      //groupMap.put
                    entry.getKey(),                                     //groupMap.put("order"
                    // 类文件在指定包名下
                    ClassName.get(packageNameForAPT,entry.getValue())   //groupMap.put("order",ARouter$$Path$$Order.class);
            );

            //返回值
            methodBuilder.addStatement("return $N", Constant.GROUP_PARAMETER_NAME);

            //生成方法
            MethodSpec methodSpec = methodBuilder.build();

            //生成最终类名
            String finalClassName = Constant.CLASS_NAME_GROUP + entry.getKey();

            //构建类
            TypeSpec typeSpec = TypeSpec.
                    classBuilder(finalClassName)                         //类名
                    .addSuperinterface(ClassName.get(typeElementGroup))  //实现的接口
                    .addModifiers(Modifier.PUBLIC)                       //public属性
                    .addMethod(methodSpec)                               //方法
                    .build();

            //生成类文件
            JavaFile javaFile = JavaFile.builder(packageNameForAPT,typeSpec).build();
            javaFile.writeTo(filer);

        }
    }

    //生成路由group对应的path，即ARouterLoadPath
    private void createPathFile(TypeElement typeElementPath) throws Exception{
        if (EmptyUtils.isEmpty(pathMap))return;

        //方法返回值
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),         //Map
                ClassName.get(String.class),      //Map<String
                ClassName.get(RouterBean.class)   //Map<String,RouterBean>
        );

        //遍历path Map，生成对应模块下的ARouterLoadPath，事实上只有一个值
        for (Map.Entry<String,List<RouterBean>> entry:pathMap.entrySet()){

            //方法配置
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constant.PATH_METHOD)//方法名
                    .addAnnotation(Override.class)  //注解
                    .addModifiers(Modifier.PUBLIC)  //public属性
                    .returns(methodReturns);        //返回值

            //HashMap
            //Map<String,RouterBean> map = new HashMap<>();
            methodBuilder.addStatement(
                    "$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    Constant.PATH_PARAMETER_NAME,
                    ClassName.get(HashMap.class)
            );

            //遍历当前分组下所有的路由信息
//            pathMpa.put("order/OrderMainActivity",
//                    RouterBean.create(RouterBean.Type.ACTIVITY,
//                            "order",
//                            "order/OrderMainActivity",
//                            OrderMainActivity.class)
//            );
            for (RouterBean bean:entry.getValue()){
                methodBuilder.addStatement(
                        "$N.put($S,$T.create($T.$L,$S,$S,$T.class))",
                        Constant.PATH_PARAMETER_NAME,                   //pathMpa.put
                        bean.getPath(),                                 //pathMpa.put("order/OrderMainActivity"
                        ClassName.get(RouterBean.class),                //pathMpa.put("order/OrderMainActivity",RouterBean.create
                        ClassName.get(RouterBean.Type.class),           //pathMpa.put("order/OrderMainActivity",RouterBean.create(RouterBean.Type
                        bean.getType(),                                 //pathMpa.put("order/OrderMainActivity",RouterBean.create(RouterBean.Type.ACTIVITY
                        bean.getGroup(),                                //pathMpa.put("order/OrderMainActivity",RouterBean.create(RouterBean.Type.ACTIVITY,"order"
                        bean.getPath(),                                 //pathMpa.put("order/OrderMainActivity",RouterBean.create(RouterBean.Type.ACTIVITY"order","order/OrderMainActivity"
                        ClassName.get((TypeElement) bean.getElement())  //pathMpa.put("order/OrderMainActivity",RouterBean.create(RouterBean.Type.ACTIVITY"order","order/OrderMainActivity",OrderMainActivity.class)
                );
            }

            //返回值
            methodBuilder.addStatement("return $N", Constant.PATH_PARAMETER_NAME);

            //生成方法
            MethodSpec methodSpec = methodBuilder.build();

            //生成最终类名
            String finalClassName = Constant.CLASS_NAME_PATH + entry.getKey();

            //构建类
            TypeSpec typeSpec = TypeSpec.
                    classBuilder(finalClassName)                         //类名
                    .addSuperinterface(ClassName.get(typeElementPath))   //实现的接口
                    .addModifiers(Modifier.PUBLIC)                       //public属性
                    .addMethod(methodSpec)                               //方法
                    .build();

            //生成类文件
            JavaFile javaFile = JavaFile.builder(packageNameForAPT,typeSpec).build();
            javaFile.writeTo(filer);

            //加入groupMap
            groupMap.put(entry.getKey(),finalClassName);

        }

    }

    //解析routerBean并将信息存入pathMap
    private void valueOfPathMap(RouterBean routerBean) {
        if(checkRouterPath(routerBean)){
            List<RouterBean> list = pathMap.get(routerBean.getGroup());
            if (list == null){
                List<RouterBean> routerBeans = new ArrayList<>();
                routerBeans.add(routerBean);
                pathMap.put(routerBean.getGroup(),routerBeans);
            }else {
                list.add(routerBean);
            }
        }else {
            messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter注解未按规范配置，如：/app/MainActivity！");
        }
    }

    //对注解的路径进行校验，规范注解
    private boolean checkRouterPath(RouterBean routerBean) {
        String groupName = routerBean.getGroup();
        String pathName = routerBean.getPath();
        //1.@ARouter注解中的path必须以"/"开头
        if (EmptyUtils.isEmpty(pathName) || !pathName.startsWith("/")){
            messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter注解中的path必须以 / 开头！");
            return false;
        }
        //2.必须是两级的path路径，即/moduleName/class
        if(pathName.lastIndexOf("/") == 0){
            messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter注解未按规范配置，如/app/MainActivity！");
            return false;
        }
        //3.第一个/和第二个/的部分表示组名，必须和module名称一样
        String name = pathName.substring(1,pathName.indexOf("/",1));
        if(EmptyUtils.isEmpty(name) || !name.equals(moduleName)){
            messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter注解中path第一个/和第二个/的部分表示组名，必须和子模块名称一致！");
            return false;
        }
        //4.@ARouter注解中的group有赋值情况,则必须和子模块名一致
        if(!EmptyUtils.isEmpty(groupName) && !groupName.equals(moduleName)){
            messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter注解中的group必须和子模块名一致！");
            return false;
        }else {
            routerBean.setGroup(name);
        }
        return true;
    }
}

package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数注解，用于注解Activity跳转时的传参
 * 通过APT生成获取参数的代码，简化程序
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Parameter {
    //参数传递的key，如何注解中省略，则参数名作为key
    String name() default "";
}

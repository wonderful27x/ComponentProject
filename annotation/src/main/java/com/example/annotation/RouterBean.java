package com.example.annotation;

import javax.lang.model.element.Element;

/**
 * 路由信息封装类
 */
public class RouterBean {

    //ARouter作用的类型
    public enum Type{
        ACTIVITY
    }

    private Type type;       //类型
    private String group;    //属组
    private String path;     //路径
    private Class<?> clazz;  //class对象
    private Element element; //类节点，用于其他处理

    private RouterBean(){}

    private RouterBean(Type type, String group, String path, Class<?> clazz) {
        this.type = type;
        this.group = group;
        this.path = path;
        this.clazz = clazz;
    }

    public static RouterBean create(Type type, String group, String path, Class<?> clazz){
        return new RouterBean(type,group,path,clazz);
    }

    public static class Builder{

        private RouterBean routerBean;

        public Builder(){
            routerBean = new RouterBean();
        }

        public Builder setType(Type type) {
            this.routerBean.type = type;
            return this;
        }

        public Builder setGroup(String group) {
            this.routerBean.group = group;
            return this;
        }

        public Builder setPath(String path) {
            this.routerBean.path = path;
            return this;
        }

        public Builder setClazz(Class<?> clazz) {
            this.routerBean.clazz = clazz;
            return this;
        }

        public Builder setElement(Element element) {
            this.routerBean.element = element;
            return this;
        }

        public RouterBean create(){
            try {
                if (routerBean.path == null || routerBean.path.length() == 0){
                    throw new IllegalArgumentException("path cannot be null or empty!");
                }
                return this.routerBean;
            }finally {
                this.routerBean = null;
            }
        }

    }

    public Type getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public String getPath() {
        return path;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}

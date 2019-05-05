package com.self.code.framework.beans;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 17:56
 * @Description:
 */
public class BeanWrapper {
    private Object wrapperInstance;
    private Class<?> wrapperClass;

    public BeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }


    public Class<?> getWrapperClass() {
        return wrapperInstance.getClass();
    }
}


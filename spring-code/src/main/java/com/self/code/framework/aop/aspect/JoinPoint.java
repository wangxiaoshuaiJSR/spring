package com.self.code.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 15:29
 * @Description:
 */
public interface JoinPoint {
    Object getThis();

    Object[] getArguments();

    Method getMethod();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}

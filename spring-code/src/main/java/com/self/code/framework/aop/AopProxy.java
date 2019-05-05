package com.self.code.framework.aop;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 17:31
 * @Description:
 */
public interface AopProxy {
    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}

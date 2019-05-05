package com.self.code.framework.aop.intercept;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 15:54
 * @Description:
 */
public interface MethodInterceptor {
    Object invoke(MethodInvocation methodInterceptor)throws Throwable;
}

package com.self.code.framework.aop;

import com.self.code.framework.aop.intercept.MethodInvocation;
import com.self.code.framework.aop.support.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 17:32
 * @Description:
 */
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private AdvisedSupport advisedSupport;

    public JdkDynamicAopProxy(AdvisedSupport advisedSupport) {
        this.advisedSupport = advisedSupport;
    }

    @Override
    public Object getProxy() {
        return getProxy(advisedSupport.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader,advisedSupport.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> interceptorsAndDynamicMethodMatchers=advisedSupport.getInterceptorsAndDynamicInterceptionAdvice(method,advisedSupport.getTargetClass());
        //Method method, Object target, Object[] arguments, List<Object> interceptorsAndDynamicMethodMatchers
        MethodInvocation methodInvocation = new MethodInvocation(method,advisedSupport.getTarget(),args,interceptorsAndDynamicMethodMatchers);
        return methodInvocation.proceed();
    }
}

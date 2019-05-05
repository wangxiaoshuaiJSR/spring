package com.self.code.framework.aop.aspect;

import com.self.code.framework.aop.intercept.MethodInterceptor;
import com.self.code.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 16:51
 * @Description:
 */
public class AfterReturningAdviceInterceptor extends AbstractAspectAdvice implements MethodInterceptor {
    private JoinPoint joinPoint;
    public AfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public void after(JoinPoint joinPoint,Object returnValue) throws Throwable {
         super.invokeAdviceMethod(joinPoint,returnValue,null);
    }
    @Override
    public Object invoke(MethodInvocation methodInterceptor) throws Throwable {
        Object returnValue = methodInterceptor.proceed();
        joinPoint = methodInterceptor;
        after(joinPoint,returnValue);
        return returnValue;
    }
}

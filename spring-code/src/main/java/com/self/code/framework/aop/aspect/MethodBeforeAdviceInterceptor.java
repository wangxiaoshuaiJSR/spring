package com.self.code.framework.aop.aspect;

import com.self.code.framework.aop.intercept.MethodInterceptor;
import com.self.code.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 16:31
 * @Description:
 */
public class MethodBeforeAdviceInterceptor extends AbstractAspectAdvice implements MethodInterceptor {
    private JoinPoint joinPoint;
    public MethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public Object before(JoinPoint joinPoint) throws Throwable {
        return super.invokeAdviceMethod(joinPoint,null,null);
    }

    @Override
    public Object invoke(MethodInvocation methodInterceptor) throws Throwable {
        this.joinPoint=methodInterceptor;
        before(joinPoint);
        return methodInterceptor.proceed();
    }
}

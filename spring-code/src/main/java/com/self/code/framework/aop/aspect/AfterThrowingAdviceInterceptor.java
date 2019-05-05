package com.self.code.framework.aop.aspect;

import com.self.code.framework.aop.intercept.MethodInterceptor;
import com.self.code.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 17:00
 * @Description:
 */
public class AfterThrowingAdviceInterceptor extends AbstractAspectAdvice implements MethodInterceptor {
    private String throwingName;

    public void setThrowingName(String throwingName) {
        this.throwingName = throwingName;
    }

    public AfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MethodInvocation methodInterceptor) throws Throwable {
        try {
            return methodInterceptor.proceed();
        }catch (Exception e){
            super.invokeAdviceMethod(methodInterceptor,null,e.getCause());
            e.printStackTrace();
        }
        return null;
    }
}

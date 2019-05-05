package com.self.code.framework.aop.intercept;

import com.self.code.framework.aop.aspect.JoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 15:57
 * @Description:
 */
public class MethodInvocation implements JoinPoint {

    private Method method;
    private Object target;
    private Object [] arguments;
    private List<Object> interceptorsAndDynamicMethodMatchers;
    private Map<String, Object> userAttributes;
    //定义一个索引，从-1开始来记录当前拦截器执行的位置
    private int currentInterceptorIndex = -1;

    public MethodInvocation(Method method, Object target, Object[] arguments, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.method = method;
        this.target = target;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    /**
     * 此方法决定了aop的链式调用顺序
     * @return
     */
    public Object proceed()throws Throwable{
        if(this.currentInterceptorIndex==this.interceptorsAndDynamicMethodMatchers.size()-1){
            return method.invoke(target,arguments);
        }
        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++currentInterceptorIndex);
        if(interceptorOrInterceptionAdvice instanceof MethodInterceptor){
            MethodInterceptor methodInterceptor = (MethodInterceptor) interceptorOrInterceptionAdvice;
            return methodInterceptor.invoke(this);
        }else{
            return proceed();
        }
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if(value!=null){
            if(userAttributes==null){
                userAttributes=new HashMap<>();
            }
            userAttributes.put(key,value);
        }else {
            if(userAttributes!=null){
                userAttributes.remove(key);
            }
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return userAttributes!=null?userAttributes.get(key):null;
    }
}

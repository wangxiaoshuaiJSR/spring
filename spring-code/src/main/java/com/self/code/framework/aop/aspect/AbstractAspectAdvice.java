package com.self.code.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 15:38
 * @Description:
 */
public abstract class AbstractAspectAdvice {
    private Method aspectMethod;
    private Object aspectTarget;

    public AbstractAspectAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    /**
     * 反射里的增强功能，链路调用最终都在这儿反射执行
     * @param joinPoint
     * @param returnValue
     * @param tx
     * @return
     * @throws Throwable
     */
    public Object invokeAdviceMethod(JoinPoint joinPoint, Object returnValue, Throwable tx)throws Throwable{
        Class<?>[] parameterTypes = aspectMethod.getParameterTypes();
        if(parameterTypes==null||parameterTypes.length==0){
            return aspectMethod.invoke(aspectTarget);
        }else{
            Object[] objects = new Object[parameterTypes.length];
            for(int i=0;i<parameterTypes.length;i++){
                if(parameterTypes[i]==JoinPoint.class){
                    objects[i]=joinPoint;
                }
                if(parameterTypes[i]==Object.class){
                    objects[i]=returnValue;
                }
                if(parameterTypes[i]==Throwable.class){
                    objects[i]=tx;
                }
            }
            return aspectMethod.invoke(aspectTarget,objects);
        }

    }
}

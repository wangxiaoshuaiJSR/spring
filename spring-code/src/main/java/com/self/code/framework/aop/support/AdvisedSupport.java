package com.self.code.framework.aop.support;

import com.self.code.framework.aop.aspect.AfterReturningAdviceInterceptor;
import com.self.code.framework.aop.aspect.AfterThrowingAdviceInterceptor;
import com.self.code.framework.aop.aspect.MethodBeforeAdviceInterceptor;
import com.self.code.framework.aop.config.AopConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 11:53
 * @Description:
 */
public class AdvisedSupport {
    private AopConfig aopConfig;

    private Class<?> targetClass;

    private Object target;

    private Pattern pointCutClassPattern;

    private String pointCut;

    private transient Map<Method, List<Object>> methodCache;


    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public AdvisedSupport(AopConfig aopConfig) {
        this.aopConfig = aopConfig;
        pointCut = aopConfig.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        //正则匹配
        String pointCutForClassRegex = pointCut.substring(0,pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(
                pointCutForClassRegex.lastIndexOf(" ") + 1));
    }
    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    /**
     * 加载aop配置
     */
    public void parse(){
        String aspectClassName = aopConfig.getAspectClass();
        try {
            Pattern pattern = Pattern.compile(pointCut);
            Class<?> aspectClazz = Class.forName(aspectClassName);
            Map<String, Method> aspectMethodMap = new HashMap<>();
            methodCache = new HashMap<>();
            for (Method aspectMethod : aspectClazz.getMethods()) {
                aspectMethodMap.put(aspectMethod.getName(),aspectMethod);
            }
            for (Method method : targetClass.getMethods()) {
                String methodName = method.toString();
                if(methodName.contains("throws")){
                    methodName = methodName.substring(0, methodName.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pattern.matcher(methodName);
                //如果方法匹配到了，则加载aop配置
                if(matcher.matches()){
                    List<Object> advices = new ArrayList<>();
                    if(aopConfig.getAspectBefore()!=null&&!aopConfig.getAspectBefore().equals("")){
                        advices.add(new MethodBeforeAdviceInterceptor(aspectMethodMap.get(aopConfig.getAspectBefore()),aspectClazz.newInstance()));
                    }
                    if(aopConfig.getAspectAfter()!=null&&!aopConfig.getAspectAfter().equals("")){
                        advices.add(new AfterReturningAdviceInterceptor(aspectMethodMap.get(aopConfig.getAspectAfter()),aspectClazz.newInstance()));
                    }
                    if(aopConfig.getAspectAfterThrow()!=null&&!aopConfig.getAspectAfterThrow().equals("")){
                        AfterThrowingAdviceInterceptor afterThrowingAdviceInterceptor = new AfterThrowingAdviceInterceptor(aspectMethodMap.get(aopConfig.getAspectAfterThrow()),aspectClazz.newInstance());
                        afterThrowingAdviceInterceptor.setThrowingName(aopConfig.getAspectAfterThrowingName());
                        advices.add(afterThrowingAdviceInterceptor);
                    }
                    methodCache.put(method,advices);
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception{
        List<Object> cached = methodCache.get(method);
        if(cached == null){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());

            cached = methodCache.get(m);

            //底层逻辑，对代理方法进行一个兼容处理
            this.methodCache.put(m,cached);
        }

        return cached;
    }


}

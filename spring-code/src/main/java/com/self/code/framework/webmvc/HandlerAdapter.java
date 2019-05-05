package com.self.code.framework.webmvc;

import com.self.code.framework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 20:44
 * @Description:
 */
public class HandlerAdapter {
    public boolean supports(Object handler){ return (handler instanceof HandlerMapping);}

    /**
     * 通过页面传入的参数   匹配方法里带请求参数带注解的
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        HandlerMapping handlerMapping = (HandlerMapping) handler;
        Map<String,Integer> paramIndexMapping=new HashMap<>();
        Annotation[][] annotations = handlerMapping.getMethod().getParameterAnnotations();
        for (int i=0;i<annotations.length;i++) {
            for (Annotation annotation : annotations[i]) {
                if(annotation instanceof RequestParam){
                    String paramName = ((RequestParam) annotation).value();
                    if(!paramName.trim().equals("")){
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }
        Class<?>[] classTypes = handlerMapping.getMethod().getParameterTypes();
        for(int j=0;j<classTypes.length;j++){
            if(classTypes[j]==HttpServletRequest.class||classTypes[j]==HttpServletResponse.class){
                paramIndexMapping.put(classTypes[j].getName(),j);
            }
        }
        //提取方法中的request和response参数
        Class<?> [] paramsTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class ||
                    type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }

        //获得方法的形参列表
        Map<String,String[]> params = request.getParameterMap();

        //实参列表
        Object [] paramValues = new Object[paramsTypes.length];

        for (Map.Entry<String, String[]> parm : params.entrySet()) {
            String value = Arrays.toString(parm.getValue()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s",",");

            if(!paramIndexMapping.containsKey(parm.getKey())){continue;}

            int index = paramIndexMapping.get(parm.getKey());
            paramValues[index] = caseStringValue(value,paramsTypes[index]);
        }

        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = request;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = response;
        }

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(),paramValues);
        if(result == null || result instanceof Void){ return null; }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == ModelAndView.class;
        if(isModelAndView){
            return (ModelAndView) result;
        }
        return null;
    }

    private Object caseStringValue(String value, Class<?> paramsType) {
        if(String.class == paramsType){
            return value;
        }
        //如果是int
        if(Integer.class == paramsType){
            return Integer.valueOf(value);
        }
        else if(Double.class == paramsType){
            return Double.valueOf(value);
        }else {
            if(value != null){
                return value;
            }
            return null;
        }
    }
}

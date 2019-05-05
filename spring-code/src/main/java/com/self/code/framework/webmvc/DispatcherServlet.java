package com.self.code.framework.webmvc;

import com.self.code.framework.annotation.Controller;
import com.self.code.framework.annotation.RequestMapping;
import com.self.code.framework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 19:52
 * @Description:
 */
public class DispatcherServlet extends HttpServlet {

    private final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private ApplicationContext applicationContext;

    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();

    private Map<HandlerMapping,HandlerAdapter> handlerAdapters = new HashMap<HandlerMapping,HandlerAdapter>();

    private List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化ApplicationContext
        applicationContext = new ApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));
        //初始化9大组件
        initStrategies(applicationContext);
    }

    private void initStrategies(ApplicationContext applicationContext) {
        //handlerMapping
        initHandlerMappings(applicationContext);
        //初始化参数适配器
        initHandlerAdapters(applicationContext);
        //初始化视图转换器
        initViewResolvers(applicationContext);
    }

    private void initViewResolvers(ApplicationContext applicationContext) {
        String templateRoot = applicationContext.getConfig().getProperty("templateRoot");
        String templateRootDir = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File file = new File(templateRootDir);
        for (String s : file.list()) {
            viewResolvers.add(new ViewResolver(templateRoot));
        }
    }

    private void initHandlerAdapters(ApplicationContext applicationContext) {
        //把一个requet请求变成一个handler，参数都是字符串的，自动配到handler中的形参

        //可想而知，他要拿到HandlerMapping才能干活
        //就意味着，有几个HandlerMapping就有几个HandlerAdapter
        for (HandlerMapping handlerMapping : handlerMappings) {
            handlerAdapters.put(handlerMapping,new HandlerAdapter());
        }
    }

    //把带controller的每个方法和请求的URL对应上
    private void initHandlerMappings(ApplicationContext applicationContext) {
        String[] beanDefinitionNames =applicationContext.getBeanDefinitionNames();
        try {
            for (String beanDefinitionName : beanDefinitionNames) {
                Object instance = applicationContext.getBean(beanDefinitionName);
                Class<?> clazz = instance.getClass();
                if(!clazz.isAnnotationPresent(Controller.class)){continue;}
                String baseUrl = "/";
                //获取Controller的url配置
                if(clazz.isAnnotationPresent(RequestMapping.class)){
                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    baseUrl = baseUrl+requestMapping.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if(!method.isAnnotationPresent(RequestMapping.class)){continue;}
                     RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                    String regex = (baseUrl+methodAnnotation.value().replaceAll("\\*",".*")).
                            replaceAll("/+","/");
                    Pattern pattern=Pattern.compile(regex);
                    handlerMappings.add(new HandlerMapping(instance,method,pattern));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception,Details:\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll(",\\s", "\r\n"));
            e.printStackTrace();
//            new GPModelAndView("500");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1、通过从request中拿到URL，去匹配一个HandlerMapping
        HandlerMapping handlerMapping = getHandler(req);
        if(handlerMapping == null){
            processDispatchResult(req,resp,new ModelAndView("404"));
            return;
        }
        //2、准备调用前的参数
        HandlerAdapter handlerAdapter = getHandlerAdapter(handlerMapping);
        //3.真正调用方法，返回ModelAndView存储了要穿页面上值，和页面模板的名称
        ModelAndView mv = handlerAdapter.handle(req,resp,handlerMapping);
        //这一步才是真正的输出
        processDispatchResult(req, resp, mv);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView mv) throws Exception {
        //把给我的ModleAndView变成一个HTML、OuputStream、json、freemark、veolcity
        //ContextType
        if(null == mv){return;}

        //如果ModelAndView不为null，怎么办？
        if(this.viewResolvers.isEmpty()){return;}

        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolveViewName(mv.getViewName(),null);
            view.render(mv.getModel(),req,resp);
            return;
        }

    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handlerMapping) {
        if(handlerAdapters.isEmpty()){
            return null;
        }
        HandlerAdapter handlerAdapter = handlerAdapters.get(handlerMapping);
        if(handlerAdapter.supports(handlerMapping)){
            return handlerAdapter;
        }
        return null;
    }

    //根据请求中的uri得到handlerMapping，也就定位到了具体的方法
    private HandlerMapping getHandler(HttpServletRequest req) {
        if(handlerMappings.size()==0){return null;}
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        uri = uri.replaceAll(contextPath,"").replaceAll("/+","/");
        for (HandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(uri);
            if(!matcher.matches()){
                continue;
            }
            return handlerMapping;
        }
        return null;
    }
}

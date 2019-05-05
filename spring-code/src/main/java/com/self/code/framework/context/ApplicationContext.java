package com.self.code.framework.context;

import com.self.code.framework.annotation.Autowired;
import com.self.code.framework.annotation.Controller;
import com.self.code.framework.annotation.Service;
import com.self.code.framework.aop.AopProxy;
import com.self.code.framework.aop.CgLibDynamicAopProxy;
import com.self.code.framework.aop.JdkDynamicAopProxy;
import com.self.code.framework.aop.config.AopConfig;
import com.self.code.framework.aop.support.AdvisedSupport;
import com.self.code.framework.beans.BeanWrapper;
import com.self.code.framework.beans.config.BeanDefinition;
import com.self.code.framework.beans.support.BeanDefinitionReader;
import com.self.code.framework.beans.support.DefaultListableBeanFactory;
import com.self.code.framework.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 09:31
 * @Description:
 */
public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

    private String[] configLoactions;

    private BeanDefinitionReader beanDefinitionReader;

    //单例的IOC容器缓存
    private Map<String,Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();

    //真正的IOC容器
    private Map<String,BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, BeanWrapper>();


    public ApplicationContext(String...configLoactions) {
        this.configLoactions = configLoactions;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() throws Exception {
        //定位
        beanDefinitionReader = new BeanDefinitionReader(configLoactions);
        //加载
        List<BeanDefinition> beanDefinitions =  beanDefinitionReader.loadBeanDefinitions();
        //注册
        doRegisterBeanDefinition(beanDefinitions);
        //把不是延时加载的类，有提前初始化
        doAutowrited();
    }

    //初始化非延迟加载的类
    private void doAutowrited() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            if(entry.getValue().isLazyInit()){
                continue;
            }
            try {
                getBean(entry.getKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 把扫描到的bean装载到伪容器中
     * @param beanDefinitions
     * @throws Exception
     */
    private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if(super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("the bean is"+beanDefinition.getBeanClassName()+"is already exit");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
    }

    @Override
    public Object getBean(String beanName) throws Exception {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        Object instance = instantiateBean(beanName,beanDefinition);
        //得到BeanWrapper
        BeanWrapper beanWrapper=new BeanWrapper(instance);
        factoryBeanInstanceCache.put(beanName,beanWrapper);
        populateBean(beanWrapper);
        return factoryBeanInstanceCache.get(beanName).getWrapperInstance();
    }

    private void populateBean(BeanWrapper beanWrapper) {
        Class<?> clazz = beanWrapper.getWrapperClass();
        if(!(clazz.isAnnotationPresent(Controller.class)||clazz.isAnnotationPresent(Service.class))){
            return;
        }
        Field[] fileds = clazz.getDeclaredFields();
        for (Field filed : fileds) {
            if(!filed.isAnnotationPresent(Autowired.class)){
                continue;
            }
            String filedName = filed.getAnnotation(Autowired.class).value().trim();
            if(filedName==null||filedName.equals("")){
                filedName = filed.getType().getName();
            }
            filed.setAccessible(true);
            if(factoryBeanInstanceCache.get(filedName)==null){
                continue;
            }
            try {
                filed.set(beanWrapper.getWrapperInstance(),factoryBeanInstanceCache.get(filedName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object instantiateBean(String beanName,BeanDefinition beanDefinition) throws Exception {
        String className = beanDefinition.getBeanClassName();
        Object instance=null;
        if(factoryBeanObjectCache.containsKey(className)){
            instance=factoryBeanObjectCache.get(className);
        }else{
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();
            AdvisedSupport advisedSupport = instantionAopConfig();
            advisedSupport.setTarget(instance);
            advisedSupport.setTargetClass(clazz);
            //如果符合切面的配置
            if(advisedSupport.pointCutMatch()){
                advisedSupport.parse();
                instance=createProxy(advisedSupport).getProxy();
            }
            factoryBeanObjectCache.put(className,instance);
            factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(),instance);
        }
        return instance;
    }

    //有接口的用JDK动态代理，否则就是CGLIB
    private AopProxy createProxy(AdvisedSupport advisedSupport) {
        Class<?> clazz = advisedSupport.getTargetClass();
        if(clazz.getInterfaces().length>0){
            return new JdkDynamicAopProxy(advisedSupport);
        }else{
            return new CgLibDynamicAopProxy();
        }
    }

    /**
     * 加载aop配置文件
     * @return
     */
    private AdvisedSupport instantionAopConfig() {
        AopConfig config = new AopConfig();
        config.setPointCut(this.beanDefinitionReader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.beanDefinitionReader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.beanDefinitionReader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.beanDefinitionReader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.beanDefinitionReader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.beanDefinitionReader.getConfig().getProperty("aspectAfterThrowingName"));
        return new AdvisedSupport(config);
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return null;
    }

    public Properties getConfig(){
        return beanDefinitionReader.getConfig();
    }

    public String[] getBeanDefinitionNames(){
        return this.beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.size()]);
    }
}

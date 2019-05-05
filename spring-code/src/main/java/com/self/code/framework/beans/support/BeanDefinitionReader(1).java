package com.self.code.framework.beans.support;

import com.self.code.framework.beans.config.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 10:12
 * @Description:
 */
public class BeanDefinitionReader {
    public static final String SCANNER_PACKAGE = "scanPackage";

    private List<String> registyBeanClasses = new ArrayList<String>();
    Properties properties = new Properties();

    /**
     * 读取aop配置文件的时候要用到
     * @return
     */
    public Properties getConfig(){
        return properties;
    }
    //构造方法初始化时，直接定位需要加载的包
    public BeanDefinitionReader(String...contextConfigLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation[0]);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(properties.getProperty(SCANNER_PACKAGE));
    }

    /**
     * 把扫描到的类名都放到一个list中去
     * @param property
     */
    private void doScanner(String property) {
        URL url = this.getClass().getClassLoader().getResource("/"+property.replaceAll("\\.","/"));
        File files = new File(url.getFile());
        for (File file : files.listFiles()) {
            if(file.isDirectory()){
                doScanner(property+"."+file.getName());
            }else{
                if(!file.getName().endsWith(".class")){continue;}
                String className = (property+"."+file.getName()).replaceAll(".class","");
                registyBeanClasses.add(className);
            }
        }
    }

    /**
     * 把定义加载的包里的bean进行加载，即配置文件定义bean是否延迟加载，是否单例，及bean的生命周期都在此加载
     * @return
     */
    public List<BeanDefinition> loadBeanDefinitions(){
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        if(registyBeanClasses.size()==0){
            throw new RuntimeException("the config scannerPackage is null");
        }
        try{
            for(String className:registyBeanClasses){
                Class<?> clazz = Class.forName(className);
                if(clazz.isInterface()){continue;}
                String factoryBeanName = clazz.getSimpleName();
                String beanName = clazz.getName();
                beanDefinitions.add(doCreateBeanDefinition(toLowerFirstCase(factoryBeanName),beanName));
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    //此处如果一个接口多个实现会出现覆盖的问题，这个时候可以自定义名称
                    beanDefinitions.add(doCreateBeanDefinition(anInterface.getName(),beanName));
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return beanDefinitions;
    }

    /**
     * 把配置文件中的配置信息包装到BeanDefinition
     * @param factoryBeanName
     * @param beanClassName
     * @return
     */
    private BeanDefinition doCreateBeanDefinition(String factoryBeanName,String beanClassName){
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setFactoryBeanName(factoryBeanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    //首字母转化成小写
    private String toLowerFirstCase(String str){
        char[] chars=str.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }


}

package com.self.code.framework.beans.support;

import com.self.code.framework.beans.config.BeanDefinition;
import com.self.code.framework.context.support.AbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 09:47
 * @Description:
 */
public class DefaultListableBeanFactory extends AbstractApplicationContext {

    //伪IOC容器，实际的容器并非这个
    protected final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,BeanDefinition>();
}

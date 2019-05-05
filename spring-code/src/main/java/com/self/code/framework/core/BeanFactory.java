package com.self.code.framework.core;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 09:29
 * @Description:
 */
public interface BeanFactory {
    /**
     * 从IOC容器中获得一个bean
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;

    Object getBean(Class<?> beanClass) throws Exception;
}

package com.self.code.framework.annotation;

import java.lang.annotation.*;

/**
 * 请求参数映射
 * @author
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
	
	String value() default "";
	
	boolean required() default true;

}

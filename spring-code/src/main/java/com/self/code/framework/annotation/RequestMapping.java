package com.self.code.framework.annotation;

import java.lang.annotation.*;

/**
 * 请求url
 * @author
 *
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
	String value() default "";
}

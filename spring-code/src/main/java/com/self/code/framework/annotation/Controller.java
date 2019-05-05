package com.self.code.framework.annotation;

import java.lang.annotation.*;

/**
 * 页面交互
 * @author
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
	String value() default "";
}

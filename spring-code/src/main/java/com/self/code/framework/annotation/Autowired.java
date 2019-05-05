package com.self.code.framework.annotation;

import java.lang.annotation.*;


/**
 * 自动注入
 * @author
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
	String value() default "";
}

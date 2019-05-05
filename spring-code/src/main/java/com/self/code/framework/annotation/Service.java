package com.self.code.framework.annotation;

import java.lang.annotation.*;

/**
 * 业务逻辑,注入接口
 * @author
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
	String value() default "";
}

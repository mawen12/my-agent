package com.mawen.agent.core.plugin.transformer.advice.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/7
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface OnMethodExitNoException {

	Class<?> repeatOn() default void.class;

	Class<? extends Throwable> onThrowable() default NoExceptionHandler.class;

	boolean backupArguments() default true;

	boolean inline() default true;

	Class<? extends Throwable> suppress() default NoExceptionHandler.class;
}

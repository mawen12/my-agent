package com.mawen.agent.plugin.utils.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonProperty {
	String USE_DEFAULT_NAME = "";
	int INDEX_UNKNOWN = -1;

	String value() default "";

	String namespace() default "";

	boolean required() default false;

	int index() default -1;

	String defaultValue() default "";

	com.fasterxml.jackson.annotation.JsonProperty.Access access() default com.fasterxml.jackson.annotation.JsonProperty.Access.AUTO;
}

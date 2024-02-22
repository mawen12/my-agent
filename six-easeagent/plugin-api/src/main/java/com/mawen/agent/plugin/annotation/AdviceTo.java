package com.mawen.agent.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.Points;

/**
 * Use to annotate Interceptor implementation,
 * to link Interceptor to Points and AgentPlugin
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AdvicesTo.class)
public @interface AdviceTo {
	Class<? extends Points> value();
	Class<? extends AgentPlugin> plugin() default AgentPlugin.class;
	String qualifier() default "default";
}

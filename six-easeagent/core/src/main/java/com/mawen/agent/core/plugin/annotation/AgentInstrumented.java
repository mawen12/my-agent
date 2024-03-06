package com.mawen.agent.core.plugin.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentInstrumented {

	int value() default 0;
}

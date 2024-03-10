package com.mawen.agent.core.instrument.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

import lombok.AllArgsConstructor;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/9
 */
public class AgentAttachmentRule implements MethodRule {

	private final boolean available;

	public AgentAttachmentRule() {
		this.available = ByteBuddyAgent.AttachmentProvider.DEFAULT.attempt().isAvailable();
	}

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		Enforce enforce = method.getAnnotation(Enforce.class);
		if (enforce != null) {
			if (!available) {
				return new NoOpStatement("The executing JVM does not support runtime attachment");
			}
			else {
				Instrumentation instrumentation = ByteBuddyAgent.install(ByteBuddyAgent.AttachmentProvider.DEFAULT);
				if (enforce.redefinesClasses() && !instrumentation.isRedefineClassesSupported()) {
					return new NoOpStatement("The executing JVM does not support class redefinition");
				}
				else if (enforce.retransformsClasses() && !instrumentation.isRetransformClassesSupported()) {
					return new NoOpStatement("The executing JVM does not support class retansformation");
				}
				else if (enforce.nativeMethodPrefix() && !instrumentation.isNativeMethodPrefixSupported()) {
					return new NoOpStatement("The executing JVM does not support class native method prefixes");
				}
			}
		}
		return base;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Enforce {
		boolean redefinesClasses() default false;
		boolean retransformsClasses() default false;
		boolean nativeMethodPrefix() default false;
	}

	@AllArgsConstructor
	private static class NoOpStatement extends Statement {

		private final String reason;

		@Override
		public void evaluate() throws Throwable {
			Logger.getLogger("net.bytebuddy").info("Omitting test case: " + reason);
		}
	}
}

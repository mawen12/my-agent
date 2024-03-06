package com.mawen.agent.core.plugin.transformer;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.field.DynamicFieldAccessor;
import com.mawen.agent.plugin.field.NullObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.bytebuddy.asm.Advice;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicFieldAdvice {

	private static final Logger log = LoggerFactory.getLogger(DynamicFieldAdvice.class);

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class DynamicInstanceInit {

		@Advice.OnMethodExit
		public static void exit(@Advice.This(optional = true) Object target) {
			if (target instanceof DynamicFieldAccessor accessor) {
				if (accessor.getAgent$$DynamicField$$Data() == null) {
					accessor.setAgent$$DynamicField$$Data(NullObject.NULL);
				}
			}
		}
	}


	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class DynamicClassInit {
		@Advice.OnMethodExit
		public static void exit(@Advice.Origin("#m") String method) {

		}
	}
}

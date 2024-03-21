package com.mawen.agent.core.plugin.transformer;

import com.mawen.agent.plugin.field.DynamicFieldAccessor;
import com.mawen.agent.plugin.field.NullObject;
import net.bytebuddy.asm.Advice;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */

public class DynamicFieldAdvice {

	private DynamicFieldAdvice() {
	}

	public static class DynamicInstanceInit {

		@Advice.OnMethodExit
		public static void exit(@Advice.This(optional = true) Object target) {
			if (target instanceof DynamicFieldAccessor accessor) {
				if (accessor.getAgent$$DynamicField$$Data() == null) {
					accessor.setAgent$$DynamicField$$Data(NullObject.NULL);
				}
			}
		}

		private DynamicInstanceInit() {
		}
	}


	public static class DynamicClassInit {
		@Advice.OnMethodExit
		public static void exit(@Advice.Origin("#m") String method) {
			// nothing
		}

		private DynamicClassInit() {
		}
	}
}

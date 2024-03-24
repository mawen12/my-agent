package com.mawen.agent.plugin.field;

import java.lang.reflect.Field;

import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AgentDynamicFieldAccessor {
	private static final Logger log = Agent.loggerFactory.getLogger(AgentDynamicFieldAccessor.class);

	public static final String DYNAMIC_FIELD_NAME = "agent_dynamic_$$$_data";

	public static <T> T getDynamicFieldValue(Object target) {
		if (!(target instanceof DynamicFieldAccessor dynamicFieldAccessor)) {
			log.warn(target.getClass().getName()," must implements DynamicFieldAccessor");
			return null;
		}

		return (T) (dynamicFieldAccessor.getAgent$$DynamicField$$Data());
	}

	public static void setDynamicFieldValue(Object target, Object value) {
		if (!(target instanceof DynamicFieldAccessor dynamicFieldAccessor)) {
			log.warn(target.getClass().getName()," must implements DynamicFieldAccessor");
			return;
		}
		dynamicFieldAccessor.setAgent$$DynamicField$$Data(value);
	}

	public static Field getDynamicFieldFormClass(Class<?> clazz) {
		return AgentFieldReflectAccessor.getFieldFromClass(clazz, DYNAMIC_FIELD_NAME);
	}
}

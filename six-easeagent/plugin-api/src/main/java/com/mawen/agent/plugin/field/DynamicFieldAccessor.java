package com.mawen.agent.plugin.field;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface DynamicFieldAccessor {

	void setAgent$$DynamicField$$Data(Object data);

	Object getAgent$$DynamicField$$Data();
}

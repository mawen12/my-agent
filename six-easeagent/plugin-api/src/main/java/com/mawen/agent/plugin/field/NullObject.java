package com.mawen.agent.plugin.field;

/**
 * default value for Agent Dynamic Field, avoiding NullPointerException when serialized
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class NullObject {
	public static final Object NULL = new Object();

	public String toString() {
		return "null";
	}
}

package com.mawen.agent.plugin.api.config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public record ChangeItem(String name, String fullName, String oldValue, String newValue) {

	@Override
	public String toString() {
		return "ChangeItem{" +
				"name='" + name + '\'' +
				", fullName='" + fullName + '\'' +
				", oldValue='" + oldValue + '\'' +
				", newValue='" + newValue + '\'' +
				'}';
	}
}

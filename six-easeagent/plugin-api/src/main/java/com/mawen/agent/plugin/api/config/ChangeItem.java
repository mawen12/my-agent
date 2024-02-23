package com.mawen.agent.plugin.api.config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class ChangeItem {
	private final String name;
	private final String fullName;
	private final String oldValue;
	private final String newValue;

	public ChangeItem(String name, String fullName, String oldValue, String newValue) {
		this.name = name;
		this.fullName = fullName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getOldValue() {
		return oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

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

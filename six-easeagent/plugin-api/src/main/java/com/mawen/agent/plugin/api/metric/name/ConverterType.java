package com.mawen.agent.plugin.api.metric.name;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public enum ConverterType {

	NONE(0),
	RATE(1),
	DURATION(2),
	;

	private int value;

	ConverterType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}

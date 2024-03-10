package com.mawen.agent.plugin.api.metric.name;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public enum MetricSubType {
	DEFAULT("00"),
	ERROR("01"),
	CHANNEL("02"), // for rabbitmq
	CONSUMER("03"),// for messaging kafka/rabbitmq consumer
	PRODUCER("04"),// for messaging kafka/rabbitmq producer
	CONSUMER_ERROR("05"),// for messaging kafka/rabbitmq consumer error
	PRODUCER_ERROR("06"),// for messaging kafka/rabbitmq producer error
	NONE("99")
	;

	private final String code;

	public String getCode() {
		return code;
	}

	MetricSubType(String code) {
		this.code = code;
	}

	public static MetricSubType valueFor(String code) {
		for (var value : MetricSubType.values()) {
			if (value.code.equals(code)) {
				return value;
			}
		}
		throw new IllegalArgumentException("code " + code + " is invalid");
	}
}

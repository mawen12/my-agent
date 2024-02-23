package com.mawen.agent.plugin.api.metric.name;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public enum MetricField {
	MIN_EXECUTION_TIME("min", ConverterType.DURATION, 2),
	MAX_EXECUTION_TIME("max", ConverterType.DURATION, 2),
	MEAN_EXECUTION_TIME("mean", ConverterType.DURATION, 2),
	P25_EXECUTION_TIME("p25", ConverterType.DURATION, 2),
	P50_EXECUTION_TIME("p50", ConverterType.DURATION, 2),
	P75_EXECUTION_TIME("p75", ConverterType.DURATION, 2),
	P95_EXECUTION_TIME("p95", ConverterType.DURATION, 2),
	P98_EXECUTION_TIME("p98", ConverterType.DURATION, 2),
	P99_EXECUTION_TIME("p99", ConverterType.DURATION, 2),
	P999_EXECUTION_TIME("p999", ConverterType.DURATION, 2),

	STD("std"),
	EXECUTION_COUNT("cnt"),
	EXECUTION_ERROR_COUNT("errcnt"),

	M1_RATE("m1", ConverterType.RATE, 5),
	M5_RATE("m5", ConverterType.RATE, 5),
	M15_RATE("m15", ConverterType.RATE, 5),
	RETRY_M1_RATE("retrym1", ConverterType.RATE, 5),
	RETRY_M5_RATE("retrym5", ConverterType.RATE, 5),
	RETRY_M15_RATE("retrym15", ConverterType.RATE, 5),
	RATELIMITER_M1_RATE("rlm1", ConverterType.RATE, 5),
	RATELIMITER_M5_RATE("rlm5", ConverterType.RATE, 5),
	RATELIMITER_M15_RATE("rlm15", ConverterType.RATE, 5),
	CIRCUITBREAKER_M1_RATE("cbm1", ConverterType.RATE, 5),
	CIRCUITBREAKER_M5_RATE("cbm5", ConverterType.RATE, 5),
	CIRCUITBREAKER_M15_RATE("cbm15", ConverterType.RATE, 5),
	MEAN_RATE("mean_rate", ConverterType.RATE, 5),
	M1_ERROR_RATE("m1err", ConverterType.RATE, 5),
	M5_ERROR_RATE("m5err", ConverterType.RATE, 5),
	M15_ERROR_RATE("m15err", ConverterType.RATE, 5),
	M1_COUNT("m1cnt", ConverterType.RATE, 0),
	M5_COUNT("m5cnt", ConverterType.RATE, 0),
	M15_COUNT("m15cnt", ConverterType.RATE, 0),
	TIMES_RATE("time_rate", ConverterType.RATE, 5),
	TOTAL_COLLECTION_TIME("total_collection_time", ConverterType.RATE, 0),
	TIMES("times", ConverterType.RATE, 0),

	CHANNEL_M1_RATE("channel_m1_rate", ConverterType.RATE, 5),
	CHANNEL_M5_RATE("channel_m5_rate", ConverterType.RATE, 5),
	CHANNEL_M15_RATE("channel_m15_rate", ConverterType.RATE, 5),
	QUEUE_M1_RATE("queue_m1_rate", ConverterType.RATE, 5),
	QUEUE_M5_RATE("queue_m5_rate", ConverterType.RATE, 5),
	QUEUE_M15_RATE("queue_m15_rate", ConverterType.RATE, 5),
	QUEUE_M1_ERROR_RATE("queue_m1_err_rate", ConverterType.RATE, 5),
	QUEUE_M5_ERROR_RATE("queue_m5_err_rate", ConverterType.RATE, 5),
	QUEUE_M15_ERROR_RATE("queue_m15_err_rate", ConverterType.RATE, 5),
	PRODUCER_M1_RATe("prodrm1", ConverterType.RATE, 5),
	PRODUCER_M5_RATE("prodrm5", ConverterType.RATE, 5),
	PRODUCER_M15_RATE("prodrm15", ConverterType.RATE, 5),
	PRODUCER_M1_ERROR_RATE("prodrm1err", ConverterType.RATE, 5),
	PRODUCER_M5_ERROR_RATE("prodrm5err", ConverterType.RATE, 5),
	PRODUCER_M15_ERROR_RATE("prodrm15err", ConverterType.RATE, 5),
	CONSUMER()
	;

	private final String field;
	private final ConverterType type;
	private final int scale;

	MetricField(String field) {
		this(field, ConverterType.NONE, 0);
	}

	MetricField(String field, ConverterType type, int scale) {
		this.field = field;
		this.type = type;
		this.scale = scale;
	}

	public String getField() {
		return field;
	}

	public ConverterType getType() {
		return type;
	}

	public int getScale() {
		return scale;
	}
}

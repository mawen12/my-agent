package com.mawen.agent.plugin.report.tracing;

import java.util.Objects;

/**
 * form zipkin2.Annotation
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class Annotation implements Comparable<Annotation> {
	long timestamp;
	String value;

	public Annotation(long timestamp, String value) {
		this.timestamp = timestamp;
		this.value = value;
	}

	public long timestamp() {
		return timestamp;
	}

	public String value() {
		return value;
	}

	@Override
	public int compareTo(Annotation that) {
		if (this == that) return 0;
		int byTimestamp = Long.compare(timestamp(), that.timestamp());
		if (byTimestamp != 0) return byTimestamp;
		return value().compareTo(that.value());
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Annotation that)) return false;

		return timestamp == that.timestamp && Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		int result = Long.hashCode(timestamp);
		result = 31 * result + Objects.hashCode(value);
		return result;
	}
}

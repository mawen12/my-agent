package com.mawen.agent.plugin.report.tracing;

/**
 * form zipkin2.Annotation
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public record Annotation(long timestamp, String value) implements Comparable<Annotation> {
	@Override
	public int compareTo(Annotation that) {
		if (this == that) return 0;
		int byTimestamp = Long.compare(timestamp(), that.timestamp());
		if (byTimestamp != 0) return byTimestamp;
		return value().compareTo(that.value());
	}
}

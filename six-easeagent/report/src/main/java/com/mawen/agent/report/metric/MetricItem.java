package com.mawen.agent.report.metric;

import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
@Getter
public class MetricItem {

	private final String key;
	private final String content;

	public MetricItem(String key, String content) {
		this.key = key;
		this.content = content;
	}
}

package com.mawen.agent.report.encoder.log.pattern;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/28
 */
public class LogDataThreadNamePatternConverter extends LogDataPatternConverter{

	public static final LogDataThreadNamePatternConverter INSTANCE = new LogDataThreadNamePatternConverter();

	/**
	 * Create a new pattern converter.
	 */
	protected LogDataThreadNamePatternConverter() {
		super("Thread", "thread");
	}

	@Override
	public void format(AgentLogData event, StringBuilder toAppendTo) {
		toAppendTo.append(event.getThreadName());
	}
}

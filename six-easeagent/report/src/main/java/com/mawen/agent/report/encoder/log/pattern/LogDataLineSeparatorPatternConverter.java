package com.mawen.agent.report.encoder.log.pattern;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class LogDataLineSeparatorPatternConverter extends LogDataPatternConverter{
	public static final LogDataLineSeparatorPatternConverter INSTANCE = new LogDataLineSeparatorPatternConverter();

	@Override
	public void format(AgentLogData event, StringBuilder toAppendTo) {
		toAppendTo.append(System.lineSeparator());
	}
}

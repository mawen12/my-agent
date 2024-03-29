package com.mawen.agent.report.encoder.log.pattern;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class LogDataLoggerPatternConverter extends NamePatternConverter {

	/**
	 * Create a new pattern converter.
	 *
	 * @param options options, may be null.
	 */
	public LogDataLoggerPatternConverter(String[] options) {
		super("Logger", "logger", options);
	}

	@Override
	public void format(AgentLogData event, StringBuilder toAppendTo) {
		abbreviate(event.getLocation(),toAppendTo);
	}
}

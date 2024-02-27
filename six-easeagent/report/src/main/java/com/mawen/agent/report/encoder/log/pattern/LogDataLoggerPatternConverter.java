package com.mawen.agent.report.encoder.log.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.NamePatternConverter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class LogDataLoggerPatternConverter extends NamePatternConverter {

	public LogDataLoggerPatternConverter(String[] options) {
		super("Logger", "logger", options);
	}

	@Override
	public void format(LogEvent event, StringBuilder toAppendTo) {

	}
}

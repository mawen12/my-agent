package com.mawen.agent.report.encoder.log.pattern;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import org.apache.logging.log4j.core.pattern.DatePatternConverter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class LogDataDatePatternConverterDelegate extends LogDataPatternConverter{

	DatePatternConverter converter;

	/**
	 * Create a new pattern converter.
	 *
	 * @param options options.
	 */
	protected LogDataDatePatternConverterDelegate(String[] options) {
		super("Date", "date");
		this.converter = DatePatternConverter.newInstance(options);
	}

	public LogDataDatePatternConverterDelegate(DatePatternConverter dateConverter) {
		super("Date", "date");
		this.converter = dateConverter;
	}

	@Override
	public void format(AgentLogData event, StringBuilder toAppendTo) {
		this.converter.format(event.getEpochMillis(),toAppendTo);
	}
}

package com.mawen.agent.report.encoder.log.pattern;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import org.apache.logging.log4j.core.pattern.DatePatternConverter;
import org.apache.logging.log4j.core.pattern.FormattingInfo;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class LogDataPatternFormatter extends LogDataPatternConverter {

	private final LogDataPatternConverter converter;
	private final FormattingInfo field;
	private final boolean skipFormattingInfo;

	public LogDataPatternFormatter(String pattern, int patternOffset,
	                               PatternFormatter formatter) {
		super("", "");
		this.field = formatter.getFormattingInfo();
		this.skipFormattingInfo = this.field == FormattingInfo.getDefault();
		this.converter = extractConvert(formatter.getConverter(), pattern, patternOffset);
	}

	public void format(final AgentLogData event, final StringBuilder buf) {
		if (skipFormattingInfo) {
			converter.format(event,buf);
		} else {
			formatWithInfo(event, buf);
		}
	}

	private void formatWithInfo(final AgentLogData event, final StringBuilder buf) {
		final int startField = buf.length();
		converter.format(event,buf);
		field.format(startField, buf);
	}

	private LogDataPatternConverter extractConvert(LogEventPatternConverter converter, String pattern, int patternOffset) {
		if (converter == null) {
			return NoOpPatternConverter.INSTANCE;
		}

		// xxx: can convert to name-INSTANCE map
		if (converter instanceof DatePatternConverter) {
			return new
		}
		else if () {}
	}
}



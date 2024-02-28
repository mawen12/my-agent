package com.mawen.agent.report.encoder.log.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import org.apache.logging.log4j.core.pattern.DatePatternConverter;
import org.apache.logging.log4j.core.pattern.FormattingInfo;
import org.apache.logging.log4j.core.pattern.LevelPatternConverter;
import org.apache.logging.log4j.core.pattern.LineSeparatorPatternConverter;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.LoggerPatternConverter;
import org.apache.logging.log4j.core.pattern.MdcPatternConverter;
import org.apache.logging.log4j.core.pattern.MessagePatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.core.pattern.ThreadNamePatternConverter;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;

/**
 * Log4j pattern formats to LogData
 *
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
			return new LogDataDatePatternConverterDelegate((DatePatternConverter) converter);
		}
		else if (converter instanceof LoggerPatternConverter) {
			return new LogDataLoggerPatternConverter(getOptions(pattern, patternOffset));
		}
		else if (converter instanceof LevelPatternConverter) {
			return new LogDataLevelPatternConverter();
		}
		else if (converter.getName().equals("SimpleLiteral")) {
			return new LogDataSimpleLiteralPatternConverter(converter);
		}
		else if (converter instanceof MessagePatternConverter) {
			return SimpleMessageConverter.INSTANCE;
		}
		else if (converter instanceof ThreadNamePatternConverter) {
			return LogDataThreadNamePatternConverter.INSTANCE;
		}
		else if (converter instanceof LineSeparatorPatternConverter) {
			return LogDataLineSeparatorPatternConverter.INSTANCE;
		}
		else if (converter instanceof MdcPatternConverter) {
			return new LogDataMdcPatternConverter(getOptions(pattern, patternOffset));
		}
		else if (converter instanceof ThrowablePatternConverter) {
			return new LogDataThrowablePatternConverter(getOptions(pattern, patternOffset));
		}
		else {
			return LogDataSimpleLiteralPatternConverter.UNKNOWN;
		}
	}

	public static List<LogDataPatternFormatter> transform(String pattern, PatternParser parser) {
		final List<PatternFormatter> formatters = parser.parse(pattern, false, false, false);

		final List<LogDataPatternFormatter> logDataFormatters = new ArrayList<>();
		final AtomicInteger patternOffset = new AtomicInteger(0);

		formatters.forEach(f -> {
			if (!f.getConverter().getName().equals("SimpleLiteral")) {
				patternOffset.set(pattern.indexOf('%', patternOffset.get()) + 1);
			}
			logDataFormatters.add(new LogDataPatternFormatter(pattern, patternOffset.get(), f));
		});

		return logDataFormatters;
	}
}



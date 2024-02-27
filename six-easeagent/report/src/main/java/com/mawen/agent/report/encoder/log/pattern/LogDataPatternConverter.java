package com.mawen.agent.report.encoder.log.pattern;

import java.util.ArrayList;
import java.util.List;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public abstract class LogDataPatternConverter extends LogEventPatternConverter {

	protected LogDataPatternConverter() {
		super("", "");
	}

	/**
	 * Create a new pattern converter.
	 *
	 * @param name name for pattern converter.
	 * @param style CSS style for formatted output.
	 */
	protected LogDataPatternConverter(String name, String style) {
		super(name, style);
	}

	/**
	 * Formats an event into a string buffer.
	 *
	 * @param event event to format, may not be null.
	 * @param toAppendTo string buffer to which to formatted event will be appended. May not be null.
	 */
	public abstract void format(final AgentLogData event, final StringBuilder toAppendTo);

	@Override
	public void format(Object obj, StringBuilder output) {
		if (obj instanceof AgentLogData) {
			format((AgentLogData)obj,output);
		} else {
			super.format(obj, output);
		}
	}

	@Override
	public void format(LogEvent event, StringBuilder toAppendTo) {
		// ignored
	}

	protected String[] getOptions(String pattern, int start) {
		List<String> options = new ArrayList<>();
		extractOptions(pattern, start, options);
		return options.toArray(new String[0]);
	}

	/**
	 * Extract options. borrow from log4j:PatternParser
	 *
	 * @param pattern conversion pattern.
	 * @param start start of options.
	 * @param options array to receive extracted options
	 * @return position in pattern after options.
	 */
	private int extractOptions(final String pattern, final int start, final List<String> options) {
		int i = pattern.indexOf('{', start);
		if (i < 0) {
			return start;
		}
		while (i < pattern.length() && pattern.charAt(i) == '{') {
			i++; // skip opening "{"
			final int begin = i; // position of first read char
			int depth = i; // already inside one level
			while (depth > 0 && i < pattern.length()) {
				final char c = pattern.charAt(i);
				if (c == '{') {
					depth++;
				}
				else if (c == '}') {
					depth--;
				}
				i++;
			} // while

			if (depth > 0) { // option not closed, continue with pattern after closing bracket
				i = pattern.lastIndexOf('}');
				if (i == -1 || i < start) {
					// if no closing bracket could be found or there is no closing bracket behind the starting
					// character of out parsing process continue parsing after the first opening bracket
					return begin;
				}
				return i + 1;
			}

			options.add(pattern.substring(begin, i - 1));
		} // while

		return i;
	}
}

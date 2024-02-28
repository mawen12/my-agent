package com.mawen.agent.report.encoder.log.pattern;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/28
 */
public class NoOpPatternConverter extends LogDataPatternConverter{

	public final static NoOpPatternConverter INSTANCE = new NoOpPatternConverter("", "");

	/**
	 * Create a new pattern converter.
	 *
	 * @param name name for pattern converter.
	 * @param style CSS style for formatted output.
	 */
	protected NoOpPatternConverter(String name, String style) {
		super(name, style);
	}

	@Override
	public void format(AgentLogData event, StringBuilder toAppendTo) {
		// NOP
	}
}

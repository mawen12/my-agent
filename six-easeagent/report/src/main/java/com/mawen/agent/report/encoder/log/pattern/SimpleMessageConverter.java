package com.mawen.agent.report.encoder.log.pattern;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/28
 */
public class SimpleMessageConverter extends LogDataPatternConverter{

	public static final SimpleMessageConverter INSTANCE = new SimpleMessageConverter();

	/**
	 * Create a new pattern converter.
	 */
	protected SimpleMessageConverter() {
		super("msg", "msg");
	}

	@Override
	public void format(AgentLogData event, StringBuilder toAppendTo) {
		toAppendTo.append(event.getBody().asString());
	}
}

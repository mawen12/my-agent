package com.mawen.agent.report.encoder.log.pattern;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import org.apache.logging.log4j.core.pattern.NameAbbreviator;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class NamePatternConverter extends LogDataPatternConverter {

	private final NameAbbreviator abbreviator;

	protected NamePatternConverter(final String name, final String style, final String[] options) {
		super(name, style);

		if (options != null && options.length > 0) {
			abbreviator = NameAbbreviator.getAbbreviator(options[0]);
		} else {
			abbreviator = NameAbbreviator.getDefaultAbbreviator();
		}
	}


}

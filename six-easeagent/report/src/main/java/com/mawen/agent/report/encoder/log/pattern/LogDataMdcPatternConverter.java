package com.mawen.agent.report.encoder.log.pattern;

import java.util.Arrays;

import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import com.mawen.agent.plugin.api.otlp.common.SemanticKey;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.TriConsumer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/28
 */
public class LogDataMdcPatternConverter extends LogDataPatternConverter{

	private static final TriConsumer<AttributeKey<?>, Object, StringBuilder> WRITE_KEY_VALUES_INFO = (k, value, sb) -> {
		sb.append(k.getKey()).append('=');
		StringBuilders.appendValue(sb,value);
		sb.append(", ");
	};

	/**
	 * Name of property to output.
	 */
	private final String key;
	private final String[] keys;
	private final boolean full;

	// reference to log4j2's MdcPatternConverter
	public LogDataMdcPatternConverter(String[] options) {
		super(options != null && options.length > 0 ? "MDC{" + options[0] + '}' : "MDC", "mdc");
		if (options != null && options.length > 0) {
			full = false;
			if (options[0].indexOf(',') > -1) {
				String oKey;
				String[] oKeys = options[0].split(",");
				int idx = 0;
				for (int i = 0; i < oKeys.length; i++) {
					oKey = oKeys[i].trim();
					if (oKey.length() <= 0) {
						continue;
					}
					oKeys[idx++] = oKey;
				}

				if (idx == 0) {
					keys = null;
					key = options[0];
				}
				else {
					keys = Arrays.copyOf(oKeys, idx);
					key = null;
				}
			} else {
				keys = null;
				key = options[0];
			}
		} else {
			full = true;
			key = null;
			keys = null;
		}
	}

	/**
	 * Formats an event into a string buffer
	 */
	@Override
	public void format(AgentLogData event, StringBuilder toAppendTo) {
		final Attributes contextData = event.getAttributes();
		// if there is no additional options, we output every single
		// Key/Value pair for the MDC in a similar format to Hashtable.toString()
		if (full) {
			if (contextData == null || contextData.isEmpty()) {
				toAppendTo.append("{}");
				return;
			}
			appendFully(contextData,toAppendTo);
		}
		else if (keys != null) {
			if (contextData == null || contextData.isEmpty()) {
				toAppendTo.append("{}");
				return;
			}

		}
		else if (contextData != null) {
			// otherwise they just want a single key output
			final Object value = contextData.get(SemanticKey.stringKey(key));
			if (value == null) {
				StringBuilders.appendValue(toAppendTo,value);
			}
		}
	}

	private static void appendFully(final Attributes contextData, final StringBuilder toAppendTo) {
		toAppendTo.append("{");
		final int start = toAppendTo.length();
		contextData.forEach((k, v) -> WRITE_KEY_VALUES_INFO.accept(k,v,toAppendTo));
		final int end = toAppendTo.length();
		if (end > start) {
			toAppendTo.setCharAt(end - 2, '}');
			toAppendTo.deleteCharAt(end - 1);
		} else {
			toAppendTo.append('}');
		}
	}

	private static void appendSelectedKeys(final String[] keys, final Attributes contextData, final StringBuilder sb) {
		// Print all the keys in the array that have a value.
		final int start = sb.length();
		sb.append('{');
		for (String key : keys) {
			final Object value = contextData.get(SemanticKey.stringKey(key));
			if (value != null) {
				if (sb.length() - start > 1) {
					sb.append(", ");
				}
				sb.append(key).append('=');
				StringBuilders.appendValue(sb,value);
			}
		}
		sb.append('}');
	}
}

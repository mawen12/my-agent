package com.mawen.agent.report.util;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import zipkin2.Span;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class SpanUtils {

	private SpanUtils(){}

	public static boolean isValidSpan(Object next) {
		if (next instanceof ReportSpan) {
			return ((ReportSpan)next).timestamp() > 0;
		}
		else if (next instanceof Span) {
			Span s = ((Span)next);
			return s.timestamp() != null && s.timestamp() > 0;
		}
		return false;
	}
}

package com.mawen.agent.mock.report;

import java.util.Map;

import com.mawen.agent.plugin.api.trace.Span;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 3.4.2
 */
public interface MockSpan {

	Span.Kind kind();

	String traceId();

	String spanId();

	String parentId();

	String tag(String key);

	Map<String, String> tags();

	String remoteServiceName();

	String annotationValueAt(int i);

	long timestamp();

	Long duration();

	int annotationCount();

	int remotePort();

	int localPort();

	String remoteIp();

	String localIp();

	String name();

	String localServerName();

	Boolean shared();

	int tagCount();

	boolean hasError();

	String errorInfo();

}

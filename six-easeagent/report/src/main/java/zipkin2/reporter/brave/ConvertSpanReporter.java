package zipkin2.reporter.brave;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import brave.Span.Kind;
import brave.Tag;
import brave.handler.MutableSpan;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.trace.ReportSpanBuilder;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
final class ConvertSpanReporter implements Reporter<MutableSpan> {
	static final Logger logger = Logger.getLogger(ConvertSpanReporter.class.getName());
	static final Map<Kind, Span.Kind> BRAVE_TO_ZIPKIN_KIND = generateKindMap();

	final Reporter<ReportSpan> delegate;
	final Tag<Throwable> errorTag;

	ConvertSpanReporter(Reporter<ReportSpan> delegate, Tag<Throwable> errorTag) {
		this.delegate = delegate;
		this.errorTag = errorTag;
	}

	@Override
	public void report(MutableSpan span) {
		maybeAddErrorTag(span);
		ReportSpan converted = convert(span);
		delegate.report(converted);
	}

	void maybeAddErrorTag(MutableSpan span) {
		// span.tag(key) iterates: check if we need to first!
		if (span.error() == null) return;
		if (span.tag("error") == null) errorTag.tag(span.error(),null, span);
	}

	static ReportSpan convert(MutableSpan span) {
		ReportSpanBuilder result = ReportSpanBuilder.newBuilder()
				.traceId(span.traceId())
				.parentId(span.parentId())
				.id(span.id())
				.name(span.name());

		long start = span.startTimestamp();
		long finish = span.finishTimestamp();
		result.timestamp(start);
		if (start != 0 && finish != 0)
			result.duration(Math.max(finish - start, 1));

		Kind kind = span.kind();
		if (kind != null) {
			result.kind(BRAVE_TO_ZIPKIN_KIND.get(kind));
		}

		String localServiceName = span.localServiceName();
		String localIp = span.localIp();
		if (localServiceName != null || localIp != null) {
			Endpoint e = Endpoint.newBuilder()
					.serviceName(localServiceName)
					.ip(localIp)
					.port(span.localPort())
					.build();
			result.localEndpoint(ReportSpanBuilder.endpoint(e));
		}

		String remoteServiceName = span.remoteServiceName();
		String remoteIp = span.remoteIp();
		if (remoteServiceName != null || remoteIp != null) {
			Endpoint e = Endpoint.newBuilder()
					.serviceName(remoteServiceName)
					.ip(remoteIp)
					.port(span.remotePort())
					.build();
			result.remoteEndpoint(ReportSpanBuilder.endpoint(e));
		}

		span.forEachTag(Consumer.INSTANCE, result);
		span.forEachAnnotation(Consumer.INSTANCE, result);

		if (span.shared()) result.shared(true);
		if (span.debug()) result.debug(true);
		return result.build();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof ConvertSpanReporter)) return false;
		return delegate.equals(((ConvertSpanReporter) obj).delegate);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	enum Consumer implements MutableSpan.TagConsumer<ReportSpanBuilder>, MutableSpan.AnnotationConsumer<ReportSpanBuilder> {
		INSTANCE;


		@Override
		public void accept(ReportSpanBuilder target, long timestamp, String value) {
			target.addAnnotation(timestamp, value);
		}

		@Override
		public void accept(ReportSpanBuilder target, String key, String value) {
			target.putTag(key, value);
		}
	}


	static Map<Kind, Span.Kind> generateKindMap() {
		Map<Kind, Span.Kind> result = new LinkedHashMap<>();
		for (Kind kind : Kind.values()) {
			result.put(kind, Span.Kind.valueOf(kind.name()));
		}
		return result;
	}
}

package zipkin2.reporter.brave;

import java.util.Map;
import java.util.logging.Logger;

import brave.Span.Kind;
import brave.Tag;
import brave.handler.MutableSpan;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
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

	}
}

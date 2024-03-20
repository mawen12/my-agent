package zipkin2.reporter.brave;

import brave.handler.SpanHandler;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import zipkin2.reporter.Reporter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class ConvertZipkinSpanHandler extends ZipkinSpanHandler {

	public static final class Builder extends ZipkinSpanHandler.Builder {
		final Reporter<ReportSpan> spanReporter;

		public Builder(Reporter<ReportSpan> spanReporter) {
			this.spanReporter = spanReporter;
		}

		@Override
		public ZipkinSpanHandler.Builder alwaysReportSpans(boolean alwaysReportSpans) {
			this.alwaysReportSpans = alwaysReportSpans;
			return this;
		}

		public SpanHandler build() {
			if (spanReporter == null) {
				return SpanHandler.NOOP;
			}
			return new ConvertZipkinSpanHandler(this);
		}
	}

	public static Builder builder(Reporter<ReportSpan> spanReporter) {
		if (spanReporter == null) {
			throw new NullPointerException("spanReporter == null");
		}
		return new Builder(spanReporter);
	}

	ConvertZipkinSpanHandler(Builder builder) {
		super(new ConvertSpanReporter(builder.spanReporter, builder.errorTag),
				builder.errorTag, builder.alwaysReportSpans);
	}
}

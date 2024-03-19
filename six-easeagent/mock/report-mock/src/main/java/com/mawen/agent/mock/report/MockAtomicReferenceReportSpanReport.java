package com.mawen.agent.mock.report;

import java.util.concurrent.atomic.AtomicReference;

import com.mawen.agent.plugin.report.tracing.ReportSpan;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 3.4.2
 */
public class MockAtomicReferenceReportSpanReport implements MockSpanReport{

	AtomicReference<ReportSpan> spanAtomicReference = new AtomicReference<>();

	@Override
	public void report(ReportSpan span) {
		spanAtomicReference.set(span);
	}

	public ReportSpan get() {
		return spanAtomicReference.get();
	}
}

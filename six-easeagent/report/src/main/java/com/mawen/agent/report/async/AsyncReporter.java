package com.mawen.agent.report.async;

import java.util.List;
import java.util.concurrent.ThreadFactory;

import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.report.sender.SenderWithEncoder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface AsyncReporter<S> extends ConfigChangeListener {

	void setFlushThreads(List<Thread> flushThreads);

	void setSender(SenderWithEncoder sender);

	SenderWithEncoder getSender();

	void setPending(int queuedMaxSpans, int queuedMaxBytes);

	void setMessageTimeoutNanos(long messageTimeoutNanos);

	void report(S next);

	void flush();

	boolean check();

	void close();

	void setThreadFactory(ThreadFactory threadFactory);

	void startFlushThread();

	void closeFlushThread();
}

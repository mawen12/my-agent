package com.mawen.agent.report.async;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface AsyncProps {

	int getReportThread();

	int getQueuedMaxItems();

	long getMessageTimeout();

	int getQueuedMaxSize();

	int getMessageMaxBytes();

	static int onePercentOfMemory() {
		long result = (long) (Runtime.getRuntime().totalMemory() * 0.01);
		// don't overflow in the rare case 1% of memory is larger than 2 GiB!
		return (int) Math.max(Math.min(Integer.MAX_VALUE, result), Integer.MIN_VALUE);
	}
}

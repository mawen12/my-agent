package com.mawen.agent.util;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class ExponentialBackoffRetryPolicy<T> {
	private static final AgentLogger logger = AgentLogger.getLogger(ExponentialBackoffRetryPolicy.class.getName());

	private final int maxAttemptCount;
	private final long minSleepMillis;
	private final float scaleFactor;

	private Random random = new Random();

	public ExponentialBackoffRetryPolicy(int maxAttemptCount, long minSleepMillis) {
		this(maxAttemptCount, minSleepMillis, 2.0f);
	}

	public ExponentialBackoffRetryPolicy(int maxAttemptCount, long minSleepMillis, float scaleFactor) {
		this.maxAttemptCount = maxAttemptCount;
		this.minSleepMillis = minSleepMillis;
		this.scaleFactor = scaleFactor;
	}

	public T attempt(Callable<T> operation) {
		int remainingAttempts = maxAttemptCount - 1;
		long minSleepTime = minSleepMillis;
		long maxSleepTime = (long) (minSleepMillis * scaleFactor);

		Throwable previousException;

		try {
			return operation.call();
		}
		catch (Throwable ex) {
			if (remainingAttempts <= 0) {
				throw new RuntimeException("Failed with first try and no remaining retry", ex);
			}
			previousException = ex;
		}

		while (remainingAttempts > 0) {
			long sleepTime = minSleepTime + random.nextInt((int)(maxSleepTime - minSleepTime));
			logger.info(String.format("Retrying (after sleeping %s milliseconds) on exception: %s", sleepTime, previousException));

			try {
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException ex) {
				logger.warn("Sleep interrupted", ex);
			}
			try {
				return operation.call();
			}
			catch (Throwable ex) {
				previousException = ex;
			}

			remainingAttempts--;
			minSleepTime *= scaleFactor;
			maxSleepTime *= scaleFactor;
		}

		String msg = String.format("Failed after trying %s times", maxAttemptCount);
		throw new RuntimeException(msg, previousException);
	}
}

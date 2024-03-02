package com.mawen.agent.report.async;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public interface AsyncReporterMetrics {

	void incrementMessages();

	void incrementMessagesDropped(Throwable cause);

	void incrementItems(int quantity);

	void incrementSpanBytes(int quantity);

	void incrementMessageBytes(int quantity);

	void incrementItemsDropped(int quantity);

	void updateQueuedItems(int update);

	void updateQueuedBytes(int update);

	AsyncReporterMetrics NOOP_METRICS = new AsyncReporterMetrics() {
		@Override
		public void incrementMessages() {
			// noop
		}

		@Override
		public void incrementMessagesDropped(Throwable cause) {
			// noop
		}

		@Override
		public void incrementItems(int quantity) {
			// noop
		}

		@Override
		public void incrementSpanBytes(int quantity) {
			// noop
		}

		@Override
		public void incrementMessageBytes(int quantity) {
			// noop
		}

		@Override
		public void incrementItemsDropped(int quantity) {
			// noop
		}

		@Override
		public void updateQueuedItems(int update) {
			// noop
		}

		@Override
		public void updateQueuedBytes(int update) {
			// noop
		}
	};
}

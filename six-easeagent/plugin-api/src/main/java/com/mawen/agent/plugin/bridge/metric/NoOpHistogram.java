package com.mawen.agent.plugin.bridge.metric;

import com.mawen.agent.plugin.api.metric.Histogram;
import com.mawen.agent.plugin.api.metric.Snapshot;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public enum NoOpHistogram implements Histogram {

	INSTANCE;

	@Override
	public void update(int value) {
		// NOP
	}

	@Override
	public void update(long value) {
		// NOP
	}

	@Override
	public long getCount() {
		return 0L;
	}

	@Override
	public Snapshot getSnapshot() {
		return NoopSnapshot.INSTANCE;
	}

	@Override
	public Object unwrap() {
		return null;
	}
}

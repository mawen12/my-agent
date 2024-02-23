package com.mawen.agent.plugin.api.health;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class AgentHealth {
	public static final AgentHealth INSTANCE = new AgentHealth();

	private volatile boolean readinessEnabled;
	private volatile boolean alive = true;
	private volatile boolean ready;

	public void setReadinessEnabled(boolean readinessEnabled) {
		INSTANCE.readinessEnabled = readinessEnabled;
	}

	public void setAlive(boolean alive) {
		INSTANCE.alive = alive;
	}

	public void setReady(boolean ready) {
		INSTANCE.ready = ready;
	}

	public boolean isReadinessEnabled() {
		return readinessEnabled;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isReady() {
		return ready;
	}
}

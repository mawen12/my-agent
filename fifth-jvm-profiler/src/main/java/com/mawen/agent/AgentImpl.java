package com.mawen.agent;

import com.mawen.agent.util.AgentLogger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class AgentImpl {
	public static final String VERSION = "1.0.0";

	private static final AgentLogger logger = AgentLogger.getLogger(AgentImpl.class.getName());

	private static final int MAX_THREAD_POOL_SIZE = 2;

	private boolean started = false;

	public void run() {

	}
}

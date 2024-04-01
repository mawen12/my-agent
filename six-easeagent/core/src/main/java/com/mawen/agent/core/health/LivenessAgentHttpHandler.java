package com.mawen.agent.core.health;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/4/1
 */
public class LivenessAgentHttpHandler extends HealthAgentHttpHandler {

	@Override
	public String getPath() {
		return "/health/liveness";
	}
}

package com.mawen.agent.core.health;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.IStatus;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/4/1
 */
public enum HStatus implements IStatus {
	SERVICE_UNAVAILABLE(503, "Service Unavailable");

	private final int requestStatus;
	private final String description;

	HStatus(int requestStatus, String description) {
		this.requestStatus = requestStatus;
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getRequestStatus() {
		return requestStatus;
	}
}

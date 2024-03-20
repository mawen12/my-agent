package com.mawen.agent.plugin.api.middleware;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public enum Type {
	REDIS(MiddlewareConstants.TYPE_REDIS),
	DATABASE(MiddlewareConstants.TYPE_DATABASE),
	KAFKA(MiddlewareConstants.TYPE_KAFKA),
	RABBITMQ(MiddlewareConstants.TYPE_RABBITMQ),
	ELASTICSEARCH(MiddlewareConstants.TYPE_ES),
	MONGODB(MiddlewareConstants.ENV_MONGODB),
	MOTAN(MiddlewareConstants.TYPE_MOTAN),
	;

	final String remoteType;

	Type(String remoteType) {
		this.remoteType = remoteType;
	}

	public String getRemoteType() {
		return remoteType;
	}
}

package com.mawen.agent.plugin.api.middleware;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public enum Redirect {
	REDIS(MiddlewareConstants.ENV_REDIS, true),
	ELASTICSEARCH(MiddlewareConstants.ENV_ES, true),
	KAFKA(MiddlewareConstants.ENV_KAFKA, true),
	RABBITMQ(MiddlewareConstants.ENV_RABBITMQ, true),
	DATABASE(MiddlewareConstants.ENV_DATABASE, false),
	MONGODB(MiddlewareConstants.ENV_MONGODB, false),
	;

	private final String env;
	private final boolean needParse;
	private final ResourceConfig config;

	Redirect(String env, boolean needParse) {
		this.env = env;
		this.needParse = needParse;
		this.config = ResourceConfig.getResourceConfig(env, needParse);
	}

	public String getEnv() {
		return env;
	}

	public boolean isNeedParse() {
		return needParse;
	}

	public boolean hasConfig() {
		return config != null;
	}

	public ResourceConfig getConfig() {
		return config;
	}
}

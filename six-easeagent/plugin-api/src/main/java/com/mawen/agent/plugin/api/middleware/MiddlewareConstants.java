package com.mawen.agent.plugin.api.middleware;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public final class MiddlewareConstants {
	public static final String ENV_REDIS = "RESOURCE_REDIS";
	public static final String ENV_ES = "RESOURCE_ELASTICSEARCH";
	public static final String ENV_MONGODB = "RESOURCE_MONGODB";
	public static final String ENV_KAFKA = "RESOURCE_KAFKA";
	public static final String ENV_RABBITMQ = "RESOURCE_RABBITMQ";
	public static final String ENV_DATABASE = "RESOURCE_DATABASE";

	public static final String TYPE_TAG_NAME = "component.type";
	public static final String TYPE_REDIS = "redis";
	public static final String TYPE_ES = "elasticsearch";
	public static final String TYPE_MONGODB = "mongodb";
	public static final String TYPE_KAFKA = "kafka";
	public static final String TYPE_RABBITMQ = "rabbitmq";
	public static final String TYPE_DATABASE = "database";

	public static final String TYPE_MOTAN = "motan";

	public static final String REDIRECTED_LABEL_REMOTE_TAG_NAME = "label.remote";
}

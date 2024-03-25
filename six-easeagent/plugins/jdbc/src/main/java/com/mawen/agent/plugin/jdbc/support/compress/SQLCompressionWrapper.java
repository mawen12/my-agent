package com.mawen.agent.plugin.jdbc.support.compress;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class SQLCompressionWrapper implements SqlCompression {

	public static final SQLCompressionWrapper INSTANCE = new SQLCompressionWrapper();

	private static final String SQL_COMPRESS_ENABLED = "plugin.observability.jdbc.sql.compress.enabled";

	@Override
	public String compress(String origin) {
		Config config = Agent.getConfig();
		Boolean enabled = config.getBoolean(SQL_COMPRESS_ENABLED);
		if (enabled) {
			return MD5SQLCompression.getInstance().compress(origin);
		}
		return SqlCompression.DEFAULT.compress(origin);
	}
}

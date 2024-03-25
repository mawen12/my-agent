package com.mawen.agent.plugin.jdbc.support.compress;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class SQLCompressionFactory {

	public static SqlCompression getSqlCompression() {
		return SQLCompressionWrapper.INSTANCE;
	}
}

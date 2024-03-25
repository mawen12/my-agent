package com.mawen.agent.plugin.jdbc.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class JdbcUtils {

	public static String getUrl(Connection conn) {
		try {
			DatabaseMetaData metadata = conn.getMetaData();
			String url = metadata.getURL();
			int index = url.indexOf('?');
			if (index == -1) {
				return url;
			}

			return url.substring(0, index);
		}
		catch (SQLException ignored) {
		}
		return null;
	}
}

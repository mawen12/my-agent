package com.mawen.agent.plugin.jdbc.support;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public record DatabaseInfo(String databaseType, String database, String host, int port) {

	public static DatabaseInfo getFromConnection(Connection connection) {
		try {
			String jdbcURL = connection.getMetaData().getURL();
			URI url = URI.create(jdbcURL.substring(5)); //strip: jdbc:
			String remoteServiceName = url.getScheme();// e.g. mysql, postgresql, oracle
			String databaseName = connection.getCatalog();
			String host = StringUtils.isNotEmpty(url.getHost()) ? url.getHost() : "";
			int port = url.getPort() != -1 ? url.getPort() : 3306;

			return new DatabaseInfo(remoteServiceName, databaseName, host, port);
		}
		catch (SQLException ignored) {
		}
		return null;
	}

	public String remoteServiceName() {
		return String.format("%s-%s", databaseType, database);
	}
}

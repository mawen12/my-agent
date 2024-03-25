package com.mawen.agent.plugin.jdbc.support;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class SqlInfo {

	private Connection connection;
	private List<String> sqlList;

	public SqlInfo(Connection connection) {
		this.connection = connection;
		this.sqlList = new ArrayList<>();
	}

	public void addSql(String sql, boolean forBatch) {
		if (!forBatch) {
			clearSql();
		}
		sqlList.add(sql);
	}

	public void clearSql() {
		sqlList.clear();
	}

	public String getSql() {
		if (this.sqlList.isEmpty()) {
			return null;
		}

		return String.join("\n", sqlList);
	}

	public Connection getConnection() {
		return connection;
	}

	public List<String> getSqlList() {
		return sqlList;
	}

}

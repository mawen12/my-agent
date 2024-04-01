package com.mawen.agent.plugin.jdbc.support.compress;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class MD5DictionaryItem {
	private final long timestamp;
	private final String category;
	@JsonProperty("host_name")
	private final String hostName;
	@JsonProperty("host_ipv4")
	private final String hostIpV4;
	private final String gid;
	private final String service;
	private final String system;
	private final String type;
	private final String tags;
	private final String id;
	private final String md5;
	private final String sql;

	public MD5DictionaryItem(long timestamp, String category, String hostName, String hostIpV4, String gid, String service, String system, String type, String tags, String id, String md5, String sql) {
		this.sql = sql;
		this.md5 = md5;
		this.id = id;
		this.tags = tags;
		this.type = type;
		this.system = system;
		this.service = service;
		this.gid = gid;
		this.hostIpV4 = hostIpV4;
		this.hostName = hostName;
		this.category = category;
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getCategory() {
		return category;
	}

	public String getHostName() {
		return hostName;
	}

	public String getHostIpV4() {
		return hostIpV4;
	}

	public String getGid() {
		return gid;
	}

	public String getService() {
		return service;
	}

	public String getSystem() {
		return system;
	}

	public String getType() {
		return type;
	}

	public String getTags() {
		return tags;
	}

	public String getId() {
		return id;
	}

	public String getMd5() {
		return md5;
	}

	public String getSql() {
		return sql;
	}
}

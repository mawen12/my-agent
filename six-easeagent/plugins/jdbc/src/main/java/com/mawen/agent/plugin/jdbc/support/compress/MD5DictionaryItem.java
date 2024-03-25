package com.mawen.agent.plugin.jdbc.support.compress;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public record MD5DictionaryItem(
		long timestamp,
		String category,
		@JsonProperty("host_name")
		String hostName,
		@JsonProperty("host_ipv4")
		String hostIpV4,
		String gid,
		String service,
		String system,
		String type,
		String tags,
		String id,
		String md5,
		String sql
) {}

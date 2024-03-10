package com.mawen.agent.plugin.api.middleware;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.utils.SystemEnv;
import com.mawen.agent.plugin.utils.common.JsonUtil;
import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class RedirectProcessor {
	private static final Logger LOGGER = Agent.getLogger(RedirectProcessor.class);
	protected static final String ENV_EASEMESH_TAGS = "EASEMESH_TAGS";

	public static final RedirectProcessor INSTANCE = new RedirectProcessor();

	private volatile Map<Redirect, String> redirectedUris = new HashMap<>();
	private final Map<String, String> tags = getServiceTagsFromEnv();

	public static void redirected(Redirect key, String uris) {
		INSTANCE.setRedirected(key, uris);
	}

	public static void setTagsIfRedirected(Redirect key, Span span, String uris) {
		var remote = getRemote(key, uris);
		if (!StringUtils.isEmpty(remote)) {
			span.tag(MiddlewareConstants.REDIRECTED_LABEL_REMOTE_TAG_NAME, remote);
		}
	}

	public static void setTagsIfRedirected(Redirect key, Tags tags) {
		var remote = INSTANCE.getRedirected(key);
		if (remote == null) {
			return;
		}
		var serviceTags = INSTANCE.getTags();
		if (serviceTags != null && !serviceTags.isEmpty()) {
			for (var entry : serviceTags.entrySet()) {
				tags.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public static Map<String, String> tags() {
		return INSTANCE.getTags();
	}

	public Map<String, String> getTags() {
		return tags;
	}

	private static String getRemote(Redirect key, String uris) {
		if (!key.hasConfig()) {
			return null;
		}
		var remote = INSTANCE.getRedirected(key);
		if (remote == null) {
			return null;
		}
		if (uris != null) {
			return uris;
		} else {
			return remote;
		}
	}


	// all
	public void init() {
		for (var redirect : Redirect.values()) { // init
			// ignore
		}
	}

	private synchronized void setRedirected(Redirect key, String uris) {
		this.redirectedUris = Map.of(key, uris);
	}

	private synchronized String getRedirected(Redirect key) {
		return this.redirectedUris.get(key);
	}

	protected static Map<String, String> getServiceTagsFromEnv() {
		return getServiceTags(ENV_EASEMESH_TAGS);
	}

	protected static Map<String, String> getServiceTags(String env) {
		var str = SystemEnv.get(env);
		if (StringUtils.isEmpty(env)) {
			return Collections.emptyMap();
		}
		try {
			var map = JsonUtil.toObject(str, new TypeReference<Map<String, String>>() {});
			var result = new HashMap<String, String>();
			for (var entry : map.entrySet()) {
				if (StringUtils.isEmpty(entry.getKey()) || StringUtils.isEmpty(entry.getValue())) {
					continue;
				}
				result.put(entry.getKey(), entry.getValue());
			}
			return result;
		}
		catch (Exception e) {
			LOGGER.warn("get env {} result: `{}` to map fail. {}",env, str, e.getMessage());
		}
		return Collections.emptyMap();
	}
}

package com.mawen.agent.plugin.api.middleware;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.bridge.NoOpLoggerFactory;
import com.mawen.agent.plugin.utils.NoNull;
import com.mawen.agent.plugin.utils.SystemEnv;
import com.mawen.agent.plugin.utils.common.JsonUtil;
import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class RedirectProcessor {

	public static final RedirectProcessor INSTANCE = new RedirectProcessor();

	private volatile Map<Redirect, String> redirectedUris = new HashMap<>();
	private final Map<String, String> tags = new HashMap<>();

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

		return NoNull.of(uris, remote);
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
}

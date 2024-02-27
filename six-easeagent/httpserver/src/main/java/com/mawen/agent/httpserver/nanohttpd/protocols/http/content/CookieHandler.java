package com.mawen.agent.httpserver.nanohttpd.protocols.http.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class CookieHandler implements Iterable<String>{

	private final HashMap<String, String> cookies = new HashMap<>();
	private final ArrayList<Cookie> queue = new ArrayList<>();

	public CookieHandler(Map<String, String> httpHeaders) {
		String raw = httpHeaders.get("cookie");
		if (raw != null) {
			String[] tokens = raw.split(";");
			for (String token : tokens) {
				String[] data = token.trim().split("=");
				if (data.length == 2) {
					this.cookies.put(data[0], data[1]);
				}
			}
		}
	}

	public void delete(String name) {
		set(name, "-delete-", -30);
	}

	public String read(String name) {
		return this.cookies.get(name);
	}

	public void set(Cookie cookie) {
		this.queue.add(cookie);
	}

	public void set(String name, String value, int expires) {
		this.queue.add(new Cookie(name, value, Cookie.getHTTPTime(expires)));
	}

	public void unloadQueue(Response response) {
		for (Cookie cookie : this.queue) {
			response.addCookieHeader(cookie.getHTTPHeader());
		}
	}

	@Override
	public Iterator<String> iterator() {
		return this.cookies.keySet().iterator();
	}
}

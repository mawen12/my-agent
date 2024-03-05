package com.mawen.agent.httpserver.jdk;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mawen.agent.httpserver.IHttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class RootContextHandler implements HttpHandler {

	CopyOnWriteArrayList<IHttpHandler> handlers = new CopyOnWriteArrayList<>();
	DefaultRoutes routes = new DefaultRoutes();

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		exchange.getRequestBody();
		exchange.getRequestHeaders();
		// todo complete
	}

	public void addRoute(IHttpHandler handler) {
		this.routes.addRoute(handler);
	}

	public static class DefaultRoutes {
		protected final Collection<UriResource> mappings;

		public DefaultRoutes() {
			this.mappings = newMappingCollection();
		}

		public void addRoute(String url, int priority, Class<?> handler, Object... initParameter) {
			if (url != null) {
				if (handler != null) {
					mappings.add(new UriResource(url, priority, handler, initParameter));
				}
			}
		}

		public void addRoute(IHttpHandler handler) {
			this.addRoute(handler.getPath(), handler.priority(), handler.getClass());
		}

		public void removeRoute(String url) {
			String uriToDelete = UriResource.normalizeUri(url);
			Iterator<UriResource> iter = mappings.iterator();
			while (iter.hasNext()) {
				UriResource uriResource = iter.next();
				if (uriToDelete.equals(uriResource.getUri())) {
					iter.remove();
					break;
				}
			}
		}

		public Collection<UriResource> getPrioritizedRoutes() {
			return Collections.unmodifiableCollection(mappings);
		}

		protected Collection<UriResource> newMappingCollection() {
			return new PriorityQueue<>();
		}
	}


}

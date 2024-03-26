package com.mawen.agent.core.plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.mawen.agent.plugin.Ordered;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class BaseLoader {
	private static final Logger log = Agent.getLogger(BaseLoader.class);

	public static <T> List<T> load(Class<T> serviceClass) {
		List<T> result = new ArrayList<>();
		ServiceLoader<T> services = ServiceLoader.load(serviceClass);
		for (Iterator<T> it = services.iterator(); it.hasNext(); ) {
			try {
				result.add(it.next());
			}
			catch (Exception e) {
				log.warn("Unable to load class: {}, Please check the plugin compile Java version configuration. and it should not latter than current JVM runtime", e.getMessage());
			}
		}
		return result;
	}

	public static <T extends Ordered> List<T> loadOrdered(Class<T> serviceClass) {
		List<T> result = load(serviceClass);
		result.sort(Comparator.comparing(Ordered::order));
		return result;
	}

	private BaseLoader() {
	}
}

package com.mawen.agent.core.plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import com.mawen.agent.plugin.Ordered;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseLoader {
	private static final Logger log = Agent.getLogger(BaseLoader.class);

	public static <T> List<T> load(Class<T> serviceClass) {
		var result = new ArrayList<T>();
		var services = ServiceLoader.load(serviceClass);
		for (var it = services.iterator(); it.hasNext(); ) {
			try {
				result.add(it.next());
			}
			catch (Exception e) {
				log.warn("Unable to load class: {}", e.getMessage());
				log.warn("Please check the plugin compile Java version configuration. and it should not latter than current JVM runtime");
			}
		}
		return result;
	}

	public static <T extends Ordered> List<T> loadOrdered(Class<T> serviceClass) {
		var result = load(serviceClass);
		result.sort(Comparator.comparing(Ordered::order));
		return result;
	}
}

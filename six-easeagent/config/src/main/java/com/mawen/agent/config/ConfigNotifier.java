package com.mawen.agent.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class ConfigNotifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigNotifier.class);

	private final CopyOnWriteArrayList<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
	private final String prefix;

	public ConfigNotifier(String prefix) {
		this.prefix = prefix;
	}

	public Runnable addChangeListener(ConfigChangeListener listener) {
		final boolean add = listeners.add(listener);
		return () -> {
			if (add) {
				listeners.remove(listener);
			}
		};
	}

	public void handleChanges(List<ChangeItem> list) {
		final var changes = this.prefix.isEmpty() ? list : filterChange(list);
		if (changes.isEmpty()) {
			return;
		}
		listeners.forEach(one -> {
			try {
				one.onChange(changes);
			}
			catch (Exception e) {
				LOGGER.warn("Notify config changes to listener failure: {}", e);
			}
		});
	}

	private List<ChangeItem> filterChange(List<ChangeItem> list) {
		return list.stream().filter(one -> one.fullName().startsWith(prefix))
				.map(e -> new ChangeItem(e.fullName().substring(prefix.length()), e.fullName(), e.oldValue(), e.newValue()))
				.collect(Collectors.toList());
	}
}

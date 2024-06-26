package com.mawen.agent.report.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public abstract class ReporterLoader {
	private static final Logger log = LoggerFactory.getLogger(ReporterLoader.class);

	public static void load() {
		encodersLoad();
		sendersLoad();
	}

	public static void encodersLoad() {
		for (var encoder : load(Encoder.class)) {
			var supplier = encoderLoad(encoder);
			ReporterRegistry.registryEncoder(encoder.name(), supplier);
		}
	}

	public static void sendersLoad() {
		for (var sender : load(Sender.class)) {
			var supplier = senderLoad(sender);
			ReporterRegistry.registrySender(sender.name(), supplier);
		}
	}

	private static Supplier<Encoder<?>> encoderLoad(Encoder<?> encoder) {
		return () -> {
			try {
				log.info("Loading encoder {}", encoder.getClass().getName());
				return encoder.getClass().getConstructor().newInstance();
			}
			catch (Exception e) {
				log.warn("Unable to load encoder: {}, because: {}", encoder.name(), e.getMessage());
				return null;
			}
		};
	}

	private static Supplier<Sender> senderLoad(Sender sender) {
		return () -> {
			try {
				log.info("Loading sender {}", sender.getClass().getName());
				return sender.getClass().getConstructor().newInstance();
			}
			catch (Exception e) {
				log.warn("Unable to load sender: {}, because: {}", sender.name(), e.getMessage());
				return null;
			}
		};
	}

	private static <T> List<T> load(Class<T> serviceClass) {
		var result = new ArrayList<T>();
		var services = ServiceLoader.load(serviceClass);
		for (var it = services.iterator(); it.hasNext(); ) {
			try {
				result.add(it.next());
			}
			catch (UnsupportedClassVersionError e) {
				log.info("Unable to load class: {}", e.getMessage());
				log.info("Please check the plugin compile Java version configuration, " +
						"and it should not latter that current JVM runtime");
			}
		}
		return result;
	}
}

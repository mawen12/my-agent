package com.mawen.agent.report.plugin;

import java.lang.reflect.InvocationTargetException;
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
public class ReporterLoader {
	static Logger logger = LoggerFactory.getLogger(ReporterLoader.class);

	private ReporterLoader(){}

	public static void load() {
		encoderLoad();
		senderLoad();
	}

	public static void encoderLoad() {
		for (var encoder : load(Encoder.class)) {
			try {
				var constructor = encoder.getClass().getConstructor();
				Supplier<Encoder<?>> encoderSupplier = () -> {
					try {
						return constructor.newInstance();
					}
					catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
						logger.warn("unable to load sender: {}", encoder.name());
						return null;
					}
				};
				ReporterRegistry.registryEncoder(encoder.name(),encoderSupplier);
			}
			catch (NoSuchMethodException e) {
				logger.warn("Sender load fail:{}", e.getMessage());
			}
		}
	}

	public static void senderLoad() {
		for (var sender : load(Sender.class)) {
			try {
				var constructor = sender.getClass().getConstructor();
				Supplier<Sender> senderSupplier = () -> {
					try {
						return constructor.newInstance();
					}
					catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
						logger.warn("unable to load sender: {}", sender.name());
						return null;
					}
				};
				ReporterRegistry.registrySender(sender.name(),senderSupplier);
			}
			catch (NoSuchMethodException e) {
				logger.warn("Sender load fail: {}", e.getMessage());
			}
		}
	}

	private static <T> List<T> load(Class<T> serviceClass) {
		var result = new ArrayList<T>();
		var services = ServiceLoader.load(serviceClass);
		for (var it = services.iterator(); it.hasNext(); ) {
			try {
				result.add(it.next());
			}
			catch (UnsupportedClassVersionError e) {
				logger.info("Unable to load class: {}", e.getMessage());
				logger.info("Please check the plugin compile Java version configuration, " +
						"and it should not latter that current JVM runtime");
			}
		}
		return result;
	}
}

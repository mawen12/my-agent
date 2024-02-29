package com.mawen.agent.report.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

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

	}

	public static void encoderLoad() {

	}

	public static void senderLoad() {
		for (Sender sender : load(Sender.class)) {
			try {
				Constructor<? extends Sender> constructor = sender.getClass().getConstructor();
				Supplier<Sender> senderSupplier = () -> {
					try {
						return constructor.newInstance();
					}
					catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
						logger.warn("unable to load sender: {}", sender.name());
						return null;
					}
				};

			}
			catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}

		}
	}

	private static <T> List<T> load(Class<T> serviceClass) {
		List<T> result = new ArrayList<>();
		ServiceLoader<T> services = ServiceLoader.load(serviceClass);
		for (Iterator<T> it = services.iterator(); it.hasNext(); ) {
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

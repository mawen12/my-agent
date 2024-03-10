package com.mawen.agent.report.plugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.Sender;
import com.mawen.agent.report.sender.NoOpSender;
import com.mawen.agent.report.sender.SenderConfigDecorator;
import com.mawen.agent.report.sender.SenderWithEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class ReporterRegistry {
	static Logger logger = LoggerFactory.getLogger(ReporterRegistry.class);

	static ConcurrentHashMap<String, Supplier<Encoder<?>>> encoders = new ConcurrentHashMap<>();
	static ConcurrentHashMap<String, Supplier<Sender>> senderSuppliers = new ConcurrentHashMap<>();

	private ReporterRegistry(){}

	public static void registryEncoder(String name, Supplier<Encoder<?>> encoder) {
		var o = encoders.putIfAbsent(name, encoder);
		if (o != null) {
			var on = o.get().getClass().getSimpleName();
			var cn = encoder.get().getClass().getSimpleName();
			logger.error("Encoder name conflict:{}, between {} and {}", name, on, cn);
		}
	}

	public static <T> Encoder<T> getEncoder(String name) {
		if (encoders.get(name) == null) {
			logger.error("Encoder name \"{}\" is not exists!",name);
			return (Encoder<T>) NoOpEncoder.INSTANCE;
		}
		var encoder = (Encoder<T>) encoders.get(name).get();

		return encoder != null ? encoder : (Encoder<T>) NoOpEncoder.INSTANCE;
	}

	public static void registrySender(String name, Supplier<Sender> sender) {
		var o = senderSuppliers.putIfAbsent(name, sender);
		if (o != null) {
			var on = sender.get().getClass().getSimpleName();
			var cn = sender.get().getClass().getSimpleName();
			logger.error("Sender name conflict:{}, between {} and {}", name, on, cn);
		}
	}

	public static SenderWithEncoder getSender(String prefix, Config config) {
		var name = config.getString(join(prefix, APPEND_TYPE_KEY));
		if (name == null) {
			logger.warn("Can not find sender name for:{}",join(prefix, APPEND_TYPE_KEY));
		}
		var sender = new SenderConfigDecorator(prefix, getSender(name), config);
		sender.init(config,prefix);
		return sender;
	}

	private static Sender getSender(String name) {
		var supplier = senderSuppliers.get(name);
		return supplier == null ? NoOpSender.INSTANCE : supplier.get();
	}
}

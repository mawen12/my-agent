package com.mawen.agent.plugin.jdbc.support.compress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.async.ScheduleHelper;
import com.mawen.agent.plugin.utils.common.DataSize;
import com.mawen.agent.plugin.utils.common.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class MD5SQLCompression implements SqlCompression, RemovalListener<String, String> {
	private static final Logger log = LoggerFactory.getLogger(MD5SQLCompression.class);

	private static final AtomicReference<MD5SQLCompression> INSTANCE = new AtomicReference<>();
	public static final DataSize MAX_SQL_SIZE = DataSize.ofKilobytes(32);

	private final Cache<String, String> dictionary;
	private final Cache<String, String> md5Cache;
	private final Consumer<Map<String, String>> reportConsumer;

	public MD5SQLCompression(Consumer<Map<String, String>> reportConsumer) {
		this.reportConsumer = reportConsumer;
		this.dictionary = CacheBuilder.newBuilder()
				.maximumSize(1000)
				.removalListener(this)
				.build();
		this.md5Cache = CacheBuilder.newBuilder()
				.maximumSize(1000)
				.build();

		ScheduleHelper.DEFAULT.nonStopExecute(10,5,this::pushItems);
	}

	public static MD5SQLCompression getInstance() {
		MD5SQLCompression instance = INSTANCE.get();
		if (instance != null) {
			return instance;
		}

		synchronized (INSTANCE) {
			instance = INSTANCE.get();
			if (instance != null) {
				return instance;
			}
			instance = new MD5SQLCompression(new MD5ReporterConsumer());
			INSTANCE.set(instance);
			return instance;
		}
	}

	@Override
	public String compress(String origin) {
		try {
			String cutStr = StringUtils.cutStrByDataSize(origin, MAX_SQL_SIZE);
			String md5 = md5Cache.get(cutStr, () -> cacheLoad(cutStr));
			String value = dictionary.getIfPresent(md5);

			if (value == null) {
				dictionary.put(md5, cutStr);
			}
			return md5;
		}
		catch (ExecutionException e) {
			log.warn("compress content[{}] failure", origin, e);
			return origin;
		}
	}

	@Override
	public void onRemoval(RemovalNotification<String, String> notification) {
		log.info("remove md5 dictionary item. cause: {}, md5: {}, content: {}",
				notification.getCause().toString(), notification.getKey(), notification.getValue());

		Map<String, String> map = new HashMap<>();
		map.put(notification.getKey(), notification.getValue());
		reportConsumer.accept(map);
	}

	private void pushItems() {
		ConcurrentMap<String, String> map = this.dictionary.asMap();
		if (map.isEmpty()) {
			return;
		}
		this.reportConsumer.accept(map);
	}

	private String cacheLoad(String str) {
		return DigestUtils.md5Hex(str);
	}
}

package com.mawen.agent.reporters;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mawen.agent.ArgumentUtils;
import com.mawen.agent.Reporter;
import com.mawen.agent.util.AgentLogger;
import com.mawen.agent.util.JsonUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class KafkaOutputReporter implements Reporter {
	private static final AgentLogger logger = AgentLogger.getLogger(KafkaOutputReporter.class.getName());

	private final String ARG_BROKER_LIST = "brokerList";
	private final String ARG_SYNC_MODE = "syncMode";
	private final String ARG_TOPIC_PREFIX = "topicPrefix";

	private String brokerList = "localhost:9092";
	private boolean syncMode = false;

	private String topicPrefix;

	private ConcurrentHashMap<String, String> profilerTopics = new ConcurrentHashMap<>();

	private Producer<String, byte[]> producer;

	public KafkaOutputReporter() {
	}

	public KafkaOutputReporter(String brokerList, boolean syncMode, String topicPrefix) {
		this.brokerList = brokerList;
		this.syncMode = syncMode;
		this.topicPrefix = topicPrefix;
	}

	@Override
	public void updateArguments(Map<String, List<String>> parsedArgs) {
		String argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_BROKER_LIST);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			setBrokerList(argValue);
			logger.info("Got argument value for brokerList: " + brokerList);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_SYNC_MODE);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			setSyncMode(Boolean.parseBoolean(argValue));
			logger.info("Got argument value for syncMode: " + syncMode);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_TOPIC_PREFIX);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			setTopicPrefix(argValue);
			logger.info("Got argument value for topicPrefix: " + topicPrefix);
		}
	}

	@Override
	public void report(String profilerName, Map<String, Object> metrics) {
		ensureProducer();

		String topicName = getTopic(profilerName);

		String str = JsonUtils.serialize(metrics);
		byte[] message = str.getBytes(StandardCharsets.UTF_8);

		Future<RecordMetadata> future = producer.send(new ProducerRecord<>(topicName, message));

		if (syncMode) {
			producer.flush();
			try {
				future.get();
			}
			catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void close() {
		synchronized (this) {
			if (producer == null) {
				return;
			}

			producer.flush();
			producer.close();

			producer = null;
		}
	}

	public String getBrokerList() {
		return brokerList;
	}

	public void setBrokerList(String brokerList) {
		this.brokerList = brokerList;
	}

	public boolean isSyncMode() {
		return syncMode;
	}

	public void setSyncMode(boolean syncMode) {
		this.syncMode = syncMode;
	}

	public String getTopicPrefix() {
		return topicPrefix;
	}

	public void setTopicPrefix(String topicPrefix) {
		this.topicPrefix = topicPrefix;
	}

	public String getTopic(String profileName) {
		String topic = profilerTopics.getOrDefault(profileName, null);
		if (topic == null || topic.isEmpty()) {
			topic = topicPrefix == null ? "" : topicPrefix;
			topic += profileName;
		}
		return topic;
	}

	private void ensureProducer() {
		synchronized (this) {
			if (producer != null) {
				return;
			}

			Properties props = new Properties();
			props.put("bootstrap.servers", brokerList);
			props.put("retries", 10);
			props.put("batch.size", 16384); // 16KB
			props.put("linger.ms", 0);
			props.put("buffer.memory", 16384000); // 16MB
			props.put("key.serializer", StringSerializer.class.getName());
			props.put("value.serializer", org.apache.kafka.common.serialization.ByteArraySerializer.class.getName());
			props.put("client.id", "jvm_profilers");

			if (syncMode) {
				props.put("acks", "all");
			}

			producer = new KafkaProducer<>(props);
		}
	}
}

package com.mawen.agent.reporters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mawen.agent.Reporter;
import com.mawen.agent.util.AgentLogger;
import org.apache.kafka.clients.producer.Producer;

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
		;
	}

	@Override

	public void report(String profilerName, Map<String, Object> metrics) {

	}

	@Override
	public void close() {

	}
}

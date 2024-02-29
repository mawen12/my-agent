package zipkin2.reporter.kafka11;

import java.util.List;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.BytesMessageEncoder;
import zipkin2.reporter.Sender;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class SimpleSender extends Sender implements SDKSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSender.class);

	@Override
	public Encoding encoding() {
		return Encoding.JSON;
	}

	@Override
	public int messageMaxBytes() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int messageSizeInBytes(List<byte[]> encodedSpans) {
		return encoding().listSizeInBytes(encodedSpans);
	}

	@Override
	public Call<Void> sendSpans(List<byte[]> encodedSpans) {
		final byte[] bytes = BytesMessageEncoder.JSON.encode(encodedSpans);
		LOGGER.info("{}",new String(bytes));
		return Call.create(null);
	}

	@Override
	public boolean isClose() {
		return false;
	}
}

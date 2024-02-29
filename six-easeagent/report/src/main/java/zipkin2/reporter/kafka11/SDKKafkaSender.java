package zipkin2.reporter.kafka11;

import java.io.IOException;
import java.util.List;

import zipkin2.Call;
import zipkin2.CheckResult;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class SDKKafkaSender extends Sender implements SDKSender {

	private final KafkaSender kafkaSender;

	public SDKKafkaSender(KafkaSender kafkaSender) {
		this.kafkaSender = kafkaSender;
	}

	public static SDKKafkaSender wrap(KafkaSender sender) {
		return new SDKKafkaSender(sender);
	}

	@Override
	public Encoding encoding() {
		return kafkaSender.encoding();
	}

	@Override
	public int messageMaxBytes() {
		return kafkaSender.messageMaxBytes();
	}

	@Override
	public int messageSizeInBytes(List<byte[]> encodedSpans) {
		return kafkaSender.messageSizeInBytes(encodedSpans);
	}

	@Override
	public int messageSizeInBytes(int encodedSizeInBytes) {
		return kafkaSender.messageSizeInBytes(encodedSizeInBytes);
	}

	@Override
	public Call<Void> sendSpans(List<byte[]> encodedSpans) {
		if (kafkaSender.closeCalled) {
			throw new IllegalStateException("closed");
		} else {
			byte[] messages = kafkaSender.encoder.encode(encodedSpans);
			return kafkaSender.new KafkaCall(messages);
		}
	}

	public Call<Void> sendSpans(byte[] encodedSpans) {
		if (kafkaSender.closeCalled) {
			throw new IllegalStateException("closed");
		} else {
			return kafkaSender.new KafkaCall(encodedSpans);
		}
	}

	@Override
	public CheckResult check() {
		return kafkaSender.check();
	}

	@Override
	public boolean isClose() {
		return kafkaSender.closeCalled;
	}

	@Override
	public void close() throws IOException {
		kafkaSender.close();
	}
}

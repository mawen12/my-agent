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

	private final KafkaSender delegate;

	public SDKKafkaSender(KafkaSender kafkaSender) {
		this.delegate = kafkaSender;
	}

	public static SDKKafkaSender wrap(KafkaSender sender) {
		return new SDKKafkaSender(sender);
	}

	@Override
	public Encoding encoding() {
		return delegate.encoding();
	}

	@Override
	public int messageMaxBytes() {
		return delegate.messageMaxBytes();
	}

	@Override
	public int messageSizeInBytes(List<byte[]> encodedSpans) {
		return delegate.messageSizeInBytes(encodedSpans);
	}

	@Override
	public int messageSizeInBytes(int encodedSizeInBytes) {
		return delegate.messageSizeInBytes(encodedSizeInBytes);
	}

	@Override
	public Call<Void> sendSpans(List<byte[]> encodedSpans) {
		if (delegate.closeCalled) {
			throw new IllegalStateException("closed");
		} else {
			byte[] messages = delegate.encoder.encode(encodedSpans);
			return delegate.new KafkaCall(messages);
		}
	}

	public Call<Void> sendSpans(byte[] encodedSpans) {
		if (delegate.closeCalled) {
			throw new IllegalStateException("closed");
		} else {
			return delegate.new KafkaCall(encodedSpans);
		}
	}

	@Override
	public CheckResult check() {
		return delegate.check();
	}

	@Override
	public boolean isClose() {
		return delegate.closeCalled;
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}

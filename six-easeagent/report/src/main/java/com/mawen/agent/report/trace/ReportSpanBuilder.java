package com.mawen.agent.report.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.mawen.agent.plugin.report.tracing.Annotation;
import com.mawen.agent.plugin.report.tracing.Endpoint;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.plugin.report.tracing.ReportSpanImpl;
import zipkin2.Span;
import zipkin2.internal.Platform;

import static brave.internal.codec.HexCodec.*;
import static java.lang.String.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public class ReportSpanBuilder extends ReportSpanImpl.Builder {
	static final Endpoint EMPTY_POINT = endpoint(zipkin2.Endpoint.newBuilder().build());

	public static ReportSpanBuilder newBuilder() {
		return new ReportSpanBuilder();
	}

	public ReportSpanBuilder clear() {
		traceId = null;
		parentId = null;
		id = null;
		kind = null;
		name = null;
		timestamp = 0L;
		duration = 0L;
		localEndpoint = null;
		remoteEndpoint = null;
		if (annotations != null) annotations.clear();
		if (tags != null) tags.clear();
		shared = false;
		debug = false;
		return this;
	}

	public ReportSpanBuilder merge(Span source) {
		if (traceId == null) traceId = source.traceId();
		if (id == null) id = source.id();
		if (parentId == null) parentId = source.parentId();
		if (kind == null) kind = source.kind().name();
		if (name == null) name = source.name();
		if (timestamp == 0L) timestamp = source.timestampAsLong();
		if (duration == 0L) duration = source.durationAsLong();

		if (localEndpoint == null)
			localEndpoint = endpoint(source.localEndpoint());
		else if (source.localEndpoint() != null)
			mergeEndpoint(localEndpoint, source.localEndpoint());

		if (remoteEndpoint == null)
			remoteEndpoint = endpoint(source.remoteEndpoint());
		else if (source.remoteEndpoint() != null)
			mergeEndpoint(remoteEndpoint, source.remoteEndpoint());

		if (!source.annotations().isEmpty()) {
			if (annotations == null) {
				annotations = new ArrayList<>(source.annotations().size());
			}
			annotations.addAll(annotations(source.annotations()));
		}

		if (!source.tags().isEmpty()) {
			if (tags == null) {
				tags = new TreeMap<>();
			}
			tags.putAll(source.tags());
		}
		shared = source.shared();
		debug = source.debug();

		return this;
	}

	Annotation annotation(zipkin2.Annotation sa) {
		return new Annotation(sa.timestamp(), sa.value());
	}

	Collection<Annotation> annotations(Collection<zipkin2.Annotation> sa) {
		return sa.stream().map(this::annotation).collect(Collectors.toList());
	}

	public static Endpoint endpoint(zipkin2.Endpoint endpoint) {
		Endpoint e = new Endpoint();
		e.setPort(endpoint.portAsInt());
		e.setServiceName(endpoint.serviceName());
		e.setIpV4(endpoint.ipv4());
		e.setIpV6(endpoint.ipv6());
		return e;
	}

	public static void mergeEndpoint(Endpoint e, zipkin2.Endpoint source) {
		if (e.serviceName() == null) {
			e.setServiceName(source.serviceName());
		}
		if (e.ipV4() == null) {
			e.setIpV4(source.ipv4());
		}
		if (e.ipV6() == null) {
			e.setIpV6(source.ipv6());
		}
		if (e.port() == 0) {
			e.setPort(source.port());
		}
	}

	public String kind() {
		return kind;
	}

	public Endpoint localEndpoint() {
		return localEndpoint;
	}

	public ReportSpanBuilder traceId(String traceId) {
		this.traceId = normalizeTraceId(traceId);
		return this;
	}

	public ReportSpanBuilder traceId(long high, long low) {
		if (high == 0L && low == 0L) throw new IllegalArgumentException("empty trace ID");
		char[] data = Platform.shortStringBuffer();
		int pos = 0;
		if (high != 0L) {
			writeHexLong(data, pos, high);
			pos += 16;
		}
		writeHexLong(data, pos, low);
		this.traceId = new String(data, 0, high != 0L ? 32 : 16);
		return this;
	}

	public ReportSpanBuilder parentId(long parentId) {
		this.parentId = parentId != 0L ? toLowerHex(parentId) : null;
		return this;
	}

	public ReportSpanBuilder parentId(String parentId) {
		if (parentId == null) {
			this.parentId = null;
			return this;
		}
		int length = parentId.length();
		if (length == 0) throw new IllegalArgumentException("parentId is empty");
		if (length > 16) throw new IllegalArgumentException("parentId.length > 16");
		if (validateHexAndReturnZeroPrefix(parentId) == length) {
			this.parentId = null;
		} else {
			this.parentId = length < 16 ? padLeft(parentId, 16) : parentId;
		}
		return this;
	}

	public ReportSpanBuilder id(long id) {
		if (id == 0L) throw new IllegalArgumentException("empty id");
		this.id = toLowerHex(id);
		return this;
	}

	public ReportSpanBuilder id(String id) {
		if (id == null) throw new NullPointerException("id == null");
		int length = id.length();
		if (length == 0) throw new IllegalArgumentException("id is empty");
		if (length > 16) throw new IllegalArgumentException("id.length > 16");
		if (validateHexAndReturnZeroPrefix(id) == 16) {
			throw new IllegalArgumentException("id is all zeros");
		}
		this.id = length < 16 ? padLeft(id, 16) : id;
		return this;
	}

	public ReportSpanBuilder kind(Span.Kind kind) {
		this.kind = kind.name();
		return this;
	}

	public ReportSpanBuilder name(String name) {
		this.name = name == null || name.isEmpty() ? null : name.toLowerCase(Locale.ROOT);
		return this;
	}

	public ReportSpanBuilder timestamp(long timestamp) {
		if (timestamp < 0L) timestamp = 0L;
		this.timestamp = timestamp;
		return this;
	}

	public ReportSpanBuilder timestamp(Long timestamp) {
		if (timestamp == null || timestamp < 0L) timestamp = 0L;
		this.timestamp = timestamp;
		return this;
	}

	public ReportSpanBuilder duration(long duration) {
		if (duration < 0L) duration = 0L;
		this.duration = duration;
		return this;
	}

	public ReportSpanBuilder duration(Long duration) {
		if (duration == null || duration < 0L) duration = 0L;
		this.duration = duration;
		return this;
	}

	public ReportSpanBuilder localEndpoint(Endpoint localEndpoint) {
		if (EMPTY_POINT.equals(localEndpoint)) {
			localEndpoint = null;
		}
		this.localEndpoint = localEndpoint;
		return this;
	}

	public ReportSpanBuilder remoteEndpoint(Endpoint remoteEndpoint) {
		if (EMPTY_POINT.equals(remoteEndpoint)) {
			remoteEndpoint = null;
		}
		this.remoteEndpoint = remoteEndpoint;
		return this;
	}

	public ReportSpanBuilder addAnnotation(long timestamp, String value) {
		if (annotations == null) annotations = new ArrayList<>(2);
		annotations.add(new Annotation(timestamp, value));
		return this;
	}

	public ReportSpanBuilder clearAnnotations() {
		if (annotations == null) return this;
		annotations.clear();
		return this;
	}

	public ReportSpanBuilder putTag(String key, String value) {
		if (tags == null) tags = new TreeMap<>();
		if (key == null) throw new NullPointerException("key == null");
		if (value == null) throw new NullPointerException("value of " + key + " == null");
		this.tags.put(key, value);
		return this;
	}

	public ReportSpanBuilder clearTags() {
		if (tags == null) return this;
		tags.clear();
		return this;
	}

	public ReportSpanBuilder debug(boolean debug) {
		this.debug = debug;
		return this;
	}

	public ReportSpanBuilder debug(Boolean debug) {
		if (debug != null) {
			return debug(debug);
		} else {
			return debug(false);
		}
	}

	public ReportSpanBuilder shared(boolean shared) {
		this.shared = shared;
		return this;
	}

	public ReportSpanBuilder shared(Boolean shared) {
		if (shared != null) {
			return shared(shared);
		} else {
			return shared(false);
		}
	}

	public ReportSpan build() {
		String missing = "";
		if (traceId == null) {
			missing += " traceId";
		}
		if (id == null) {
			missing += " id";
		}
		if (!"".equals(missing)) {
			throw new IllegalStateException("Missing: " + missing);
		}
		if (id.equals(parentId)) {
			Logger logger = Logger.getLogger(ReportSpan.class.getName());
			if (logger.isLoggable(Level.FINEST)) {
				logger.fine(format("undoing circular dependency: traceId=%s, spanId=%s", traceId, id));
			}
			parentId = null;
		}
		annotations = sortedList(annotations);

		if (this.shared && kind.equals(Span.Kind.CLIENT.name())) {
			Logger logger = Logger.getLogger(ReportSpan.class.getName());
			if (logger.isLoggable(Level.FINEST)) {
				logger.fine(format("removing shared flag on client: traceId=%s, spanId=%s", traceId, id));
			}
			shared(null);
		}
		return new ReportSpanImpl(this);
	}

	ReportSpanBuilder(){}

	public static String normalizeTraceId(String traceId) {
		if (traceId == null) throw new NullPointerException("traceId == null");
		int length = traceId.length();
		if (length == 0) throw new IllegalArgumentException("traceId is empty");
		if (length > 32) throw new IllegalArgumentException("traceId.length > 32");
		int zeros = validateHexAndReturnZeroPrefix(traceId);
		if (zeros == length) throw new IllegalArgumentException("traceId is all zeros");
		if (length == 32 || length == 16) {
			if (length == 32 && zeros >= 16) {
				return traceId.substring(16);
			}
			return traceId;
		}
		else if (length < 16) {
			return padLeft(traceId, 16);
		} else {
			return padLeft(traceId, 32);
		}
	}

	static int validateHexAndReturnZeroPrefix(String id) {
		int zeros = 0;
		boolean isZeroPrefix = id.charAt(0) == '0';
		for (int i = 0, length = id.length(); i < length; i++) {
			char c = id.charAt(i);
			if ((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
				throw new IllegalArgumentException(id + " should be lower-hex encoded with no prefix");
			}
			if (c != '0') {
				isZeroPrefix = false;
			}
			else if (isZeroPrefix) {
				zeros++;
			}
		}
		return zeros;
	}

	static final String THIRTY_TWO_ZEROS;

	static {
		char[] zeros = new char[32];
		Arrays.fill(zeros, '0');
		THIRTY_TWO_ZEROS = new String(zeros);
	}

	static String padLeft(String id, int desiredLength) {
		int length = id.length();
		int numZeros = desiredLength - length;

		char[] data = Platform.shortStringBuffer();
		THIRTY_TWO_ZEROS.getChars(0,numZeros,data,0);
		id.getChars(0, length, data, numZeros);

		return new String(data, 0, desiredLength);
	}

	static <T extends Comparable<? super T>> List<T> sortedList(List<T> in) {
		if (in == null || in.isEmpty()) {
			return Collections.emptyList();
		}

		if (in.size() == 1) {
			return Collections.singletonList(in.get(0));
		}

		Object[] array = in.toArray();
		Arrays.sort(array);

		// depude
		int j = 0;
		int i = 1;
		while (i < array.length) {
			if (!array[i].equals(array[j])) {
				array[++j] = array[i];
			}
			i++;
		}

		List result = Arrays.asList(i == j + 1 ? array : Arrays.copyOf(array, j + 1));
		return Collections.<T>unmodifiableList(result);
	}

}

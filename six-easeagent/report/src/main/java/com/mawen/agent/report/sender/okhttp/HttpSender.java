package com.mawen.agent.report.sender.okhttp;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.auto.service.AutoService;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.async.AgentThreadFactory;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Sender;
import com.mawen.agent.plugin.utils.NoNull;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.plugin.NoOpCall;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.tls.Certificates;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@Slf4j
@AutoService(Sender.class)
public class HttpSender implements Sender{

	public static final String SENDER_NAME = ZIPKIN_SENDER_NAME;

	private static final String AUTH_HEADER = "Authorization";

	private static final String ENABLED_KEY = "enabled";
	private static final String URL_KEY = "url";
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";
	private static final String GZIP_KEY = "gzip";
	private static final String MAX_REQUESTS_KEY = "maxRequests";

	private static final String SERVER_USER_NAME_KEY = join(OUTPUT_SERVER_V2, USERNAME_KEY);
	private static final String SERVER_PASSWORD_KEY = join(OUTPUT_SERVER_V2, PASSWORD_KEY);
	private static final String SERVER_GZIP_KEY = join(OUTPUT_SERVER_V2, GZIP_KEY);

	private static final String TLS_ENABLE = join(OUTPUT_SERVER_V2, "tls.enable");

	// private key should be pkcs8 format
	private static final String TLS_KEY = join(OUTPUT_SERVER_V2, "tls.key");
	private static final String TLS_CERT = join(OUTPUT_SERVER_V2, "tls.cert");
	private static final String TLS_CA_CERT = join(OUTPUT_SERVER_V2, "tls.ca_cert");

	private static final int MIN_TIMEOUT = 30_000;

	static ConcurrentHashMap<String, OkHttpClient> clientMap = new ConcurrentHashMap<>();

	private String senderEnabledKey;
	private String urlKey;
	private String usernameKey;
	private String passwordKey;
	private String gzipKey;
	private String maxRequestsKey;
	private Config config;
	private String url;
	private HttpUrl httpUrl;
	private String username;
	private String password;
	private boolean enabled;
	private boolean gzip;
	private boolean isAuth;
	private int timeout;
	private int maxRequests;
	private String credential;
	private OkHttpClient client;
	private Boolean tlsEnable;
	private String tlsKey;
	private String tlsCert;
	private String tlsCaCert;
	private String prefix;

	@Override
	public String name() {
		return SENDER_NAME;
	}

	@Override
	public void init(Config config, String prefix) {
		this.prefix = prefix;
		extractConfig(config);
		this.config = config;
		initClient();
	}

	@Override
	public Call<Void> send(EncodedData encodedData) {
		if (!enabled) {
			return NoOpCall.getInstance(Void.class);
		}
		Request request;

		try {
			if (encodedData instanceof RequestBody) {
				request = newRequest((RequestBody)encodedData);
			} else {
				request = newRequest(new ByteRequestBody(encodedData.getData()));
			}
		}
		catch (Exception e) {
			// log rate-limit
			if (log.isDebugEnabled()) {
				log.debug("tracing send fail!");
			}
			return NoOpCall.getInstance(Void.class);
		}

		return new HttpCall(client.newCall(request));
	}

	@Override
	public boolean isAvailable() {
		return this.enabled;
	}

	@Override
	public void updateConfigs(Map<String, String> changes) {
		this.config.updateConfigsNotNotify(changes);

		String newUsername = StringUtils.noEmptyOf(config.getString(usernameKey), config.getString(SERVER_USER_NAME_KEY));
		String newPassword = StringUtils.noEmptyOf(config.getString(passwordKey), config.getString(SERVER_PASSWORD_KEY));
		// check new client
		boolean renewClient = !getUrl(this.config).equals(this.url)
				|| !org.apache.commons.lang3.StringUtils.equals(newUsername, this.username)
				|| !org.apache.commons.lang3.StringUtils.equals(newPassword, this.password)
				|| !org.apache.commons.lang3.StringUtils.equals(this.config.getString(TLS_CA_CERT), this.tlsCaCert)
				|| !org.apache.commons.lang3.StringUtils.equals(this.config.getString(TLS_CERT), this.tlsCert)
				|| org.apache.commons.lang3.StringUtils.equals(this.config.getString(TLS_KEY), this.tlsKey);

		if (renewClient) {
			clearClient();
			extractConfig(config);
			newClient();
		}
	}

	@Override
	public void close() throws IOException {
		clearClient();
	}

	public static void appendBasicAuth(OkHttpClient.Builder builder, String basicUser, String basicPassword) {
		builder.addInterceptor(chain -> {
			Request request = chain.request();
			Request authRequest = request.newBuilder().header(AUTH_HEADER, Credentials.basic(basicUser, basicPassword)).build();
			return chain.proceed(authRequest);
		});
	}

	public static void appendBasicAuth(OkHttpClient.Builder builder, String basicCredential) {
		builder.addInterceptor(chain -> {
			Request request = chain.request();
			Request authRequest = request.newBuilder().header(AUTH_HEADER, basicCredential).build();
			return chain.proceed(authRequest);
		});
	}

	public static void appendTls(OkHttpClient.Builder builder, String tlsCaCert, String tlsCert, String tlsKey) {
		X509Certificate clientX509Certificate = Certificates.decodeCertificatePem(tlsCert);
		HeldCertificate clientCertificateKey = HeldCertificate.decode(tlsCert + tlsKey);
		HandshakeCertificates.Builder handshakeCertificatesBuilder = new HandshakeCertificates.Builder();
		handshakeCertificatesBuilder.addPlatformTrustedCertificates();
		if (org.apache.commons.lang3.StringUtils.isNotBlank(tlsCaCert)) {
			X509Certificate rootX509Certificate = Certificates.decodeCertificatePem(tlsCaCert);
			handshakeCertificatesBuilder.addTrustedCertificate(rootX509Certificate);
		}
		handshakeCertificatesBuilder.heldCertificate(clientCertificateKey, clientX509Certificate);
		HandshakeCertificates clientCertificates = handshakeCertificatesBuilder.build();
		builder.sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager());
	}

	static Dispatcher newDispatcher(int maxRequests) {
		// bound the executor so that we get consistent performance
		ThreadPoolExecutor dispatchExecutor = new ThreadPoolExecutor(0,maxRequests, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<>(), OkHttpSenderThreadFactory.INSTANCE );

		Dispatcher dispatcher = new Dispatcher(dispatchExecutor);
		dispatcher.setMaxRequests(maxRequests);
		dispatcher.setMaxRequestsPerHost(maxRequests);
		return dispatcher;
	}

	private void extractConfig(Config config) {
		updatePrefix(this.prefix);
		this.url = getUrl(config);
		this.username = StringUtils.noEmptyOf(config.getString(usernameKey), config.getString(SERVER_USER_NAME_KEY));
		this.password = StringUtils.noEmptyOf(config.getString(passwordKey), config.getString(SERVER_PASSWORD_KEY));

		this.tlsEnable = config.getBoolean(TLS_ENABLE);
		this.tlsKey = config.getString(TLS_KEY);
		this.tlsCert = config.getString(TLS_CERT);
		this.tlsCaCert = config.getString(TLS_CA_CERT);

		this.gzip = NoNull.of(config.getBooleanNullForUnset(gzipKey), NoNull.of(config.getBooleanNullForUnset(SERVER_GZIP_KEY), true));

		this.timeout = NoNull.of(config.getInt(OUTPUT_SERVERS_TIMEOUT), MIN_TIMEOUT);
		if (this.timeout < MIN_TIMEOUT) {
			this.timeout = MIN_TIMEOUT;
		}
		this.enabled = NoNull.of(config.getBooleanNullForUnset(senderEnabledKey), true);
		this.maxRequests = NoNull.of(config.getInt(maxRequestsKey), 65);

		if (StringUtils.isEmpty(url) || Boolean.FALSE.equals(config.getBoolean(OUTPUT_SERVERS_ENABLE))) {
			this.enabled = false;
		} else {
			this.httpUrl = HttpUrl.parse(this.url);
			if (this.httpUrl == null) {
				log.error("Invalid Url:{}", this.url);
				this.enabled = false;
			}
		}

		this.isAuth = !StringUtils.isEmpty(username) && !StringUtils.isEmpty(this.password);
		if (isAuth) {
			this.credential = Credentials.basic(this.username, this.password);
		}
	}

	private void updatePrefix(String prefix) {
		senderEnabledKey = join(prefix, ENABLED_KEY);
		urlKey = join(prefix, URL_KEY);
		usernameKey = join(prefix, USERNAME_KEY);
		passwordKey = join(prefix, PASSWORD_KEY);
		gzipKey = join(prefix, GZIP_KEY);
		maxRequestsKey = join(prefix, MAX_REQUESTS_KEY);
	}

	private String getUrl(Config config) {
		// url
		String outputServer = config.getString(BOOTSTRAP_SERVERS);
		String cUrl = NoNull.of(config.getString(urlKey), "");
		if (!StringUtils.isEmpty(outputServer) && !cUrl.startsWith("http")) {
			cUrl = outputServer + cUrl;
		}
		return cUrl;
	}

	private void initClient() {
		if (client != null) {
			return;
		}

		newClient();
	}

	private void newClient() {
		String clientKey = getClientKey();
		OkHttpClient newClient = clientMap.get(clientKey);
		if (newClient != null) {
			client = newClient;
			return;
		}
		OkHttpClient.Builder builder = new OkHttpClient.Builder();

		// timeout
		builder.connectTimeout(timeout, TimeUnit.MILLISECONDS);
		builder.readTimeout(timeout, TimeUnit.MILLISECONDS);
		builder.writeTimeout(timeout, TimeUnit.MILLISECONDS);

		// auth
		if (this.isAuth) {
			appendBasicAuth(builder, this.credential);
		}
		// tls
		if (Boolean.TRUE.equals(this.tlsEnable)) {
			appendTls(builder, this.tlsCaCert, this.tlsCert, this.tlsKey);
		}
		synchronized (HttpSender.class) {
			if (clientMap.get(clientKey) != null) {
				client = clientMap.get(clientKey);
			} else {
				builder.dispatcher(newDispatcher(maxRequests));
				newClient = builder.build();
				clientMap.putIfAbsent(clientKey, newClient);
				client = newClient;
			}
		}
	}

	private String getClientKey() {
		return this.url + ":" + this.username + ":" + this.password;
	}

	private Request newRequest(RequestBody body) throws IOException {
		Request.Builder request = new Request.Builder().url(httpUrl);
		// Amplification can occur when the Zipkin endpoint is accessed through a proxy, and the proxy is instrumented.
		// The prevents that is proxies, such as Envoy, that understand B3 single format.
		request.addHeader("b3", "0");
		if (this.isAuth) {
			request.header(AUTH_HEADER, credential);
		}
		if (this.gzip) {
			request.addHeader("Content-Encoding", "gzip");
			Buffer gzipped = new Buffer();
			BufferedSink gzipSink = Okio.buffer(new GzipSink(gzipped));
			body.writeTo(gzipSink);
			gzipSink.close();
			body = new BufferRequestBody(body.contentType(), gzipped);
		}
		request.post(body);
		return request.build();
	}

	private void clearClient() {
		OkHttpClient dClient = clientMap.remove(getClientKey());
		if (dClient == null) {
			return;
		}
		Dispatcher dispatcher = dClient.dispatcher();
		dispatcher.executorService().shutdown();
		try {
			if (!dispatcher.executorService().awaitTermination(1, TimeUnit.SECONDS)) {
				dispatcher.cancelAll();
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	static class OkHttpSenderThreadFactory extends AgentThreadFactory {
		public static final OkHttpSenderThreadFactory INSTANCE = new OkHttpSenderThreadFactory();

		@Override
		public Thread newThread(@Nullable Runnable r) {
			return new Thread(r, "AgentHttpSenderDispatcher-" + createCount.getAndIncrement());
		}
	}

	@AllArgsConstructor
	static final class BufferRequestBody extends RequestBody {
		final MediaType contentType;
		final Buffer body;

		@Override
		public long contentLength() {
			return body.size();
		}

		@Nullable
		@Override
		public MediaType contentType() {
			return contentType;
		}

		@Override
		public void writeTo(@NotNull BufferedSink sink) throws IOException {
			sink.write(body, body.size());
		}
	}
}

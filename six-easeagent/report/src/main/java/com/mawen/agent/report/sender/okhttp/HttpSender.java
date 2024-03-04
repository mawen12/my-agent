package com.mawen.agent.report.sender.okhttp;

import java.io.IOException;
import java.util.Map;

import com.google.auto.service.AutoService;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Sender;
import lombok.extern.slf4j.Slf4j;

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

	private static final String TLS_ENABLED = join(OUTPUT_SERVER_V2, "tls.enable");

	// private key should be pkcs8 format


	@Override
	public String name() {
		return "";
	}

	@Override
	public void init(Config config, String prefix) {

	}

	@Override
	public Call<Void> send(EncodedData encodedData) {
		return null;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public void updateConfigs(Map<String, String> changes) {

	}

	@Override
	public void close() throws IOException {

	}
}

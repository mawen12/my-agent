package com.mawen.agent.report;

import java.util.Map;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.config.Configs;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.config.ConfigConst.Observability;

import static com.mawen.agent.config.ConfigUtils.*;
import static com.mawen.agent.config.report.ReportConfigConst.*;
import static com.mawen.agent.plugin.api.config.ConfigConst.Observability.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public interface OutputProperties {

	String getServers();

	String getTimeout();

	Boolean isEnabled();

	String getSecurityProtocol();

	String getSSLKeyStoreType();

	String getKeyStoreKey();

	String getKeyStoreCertChain();

	String getTrustCertificate();

	String getTrustCertificateType();

	String getEndpointAlgorithm();

	boolean updateConfig(Map<String, String> changed);

	static OutputProperties newDefault(Config configs) {
		return new Default(configs);
	}

	class Default implements OutputProperties {
		private volatile String endpointAlgorithm = "";
		private volatile String trustCertificateType = "";
		private volatile String trustCertificate = "";
		private volatile String servers = "";
		private volatile String timeout = "";
		private volatile boolean enabled;
		private volatile String protocol = "";
		private volatile String sslKeyStoreType = "";
		private volatile String sslKey = "";
		private volatile String certificate = "";

		public Default(Config configs) {
			extractProp(configs);
		}

		@Override
		public boolean updateConfig(Map<String, String> changed) {
			Config configs = new Configs(changed);
			int changeItems = 0;
			changeItems += ConfigUtils.isChanged(OUTPUT_SERVERS, changed, this.servers);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_TIMEOUT, changed, this.timeout);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_ENABLED, changed, String.valueOf(this.enabled));
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_SECURITY_PROTOCOL, changed, this.protocol);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_SSL_KEYSTORE_KEY, changed, this.sslKeyStoreType);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_SSL_KEYSTORE_KEY, changed, this.sslKey);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_SSL_KEYSTORE_CERT_CHAIN, changed, this.certificate);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_SSL_TRUSTSTORE_CERTS, changed, this.trustCertificate);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_SSL_TRUSTSTORE_TYPE, changed, this.trustCertificateType);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM, changed, this.endpointAlgorithm);
			if (changeItems == 0) {
				return false;
			}
			extractProp(configs);
			return false;
		}

		@Override
		public String getServers() {
			return this.servers;
		}

		@Override
		public String getTimeout() {
			return this.timeout;
		}

		@Override
		public Boolean isEnabled() {
			return this.enabled;
		}

		@Override
		public String getSecurityProtocol() {
			return this.protocol;
		}

		@Override
		public String getSSLKeyStoreType() {
			return this.sslKeyStoreType;
		}

		@Override
		public String getKeyStoreKey() {
			return this.sslKey;
		}

		@Override
		public String getKeyStoreCertChain() {
			return this.certificate;
		}

		@Override
		public String getTrustCertificate() {
			return this.trustCertificate;
		}

		@Override
		public String getTrustCertificateType() {
			return this.trustCertificateType;
		}

		@Override
		public String getEndpointAlgorithm() {
			return this.endpointAlgorithm;
		}

		private void extractProp(Config configs) {
			bindProp(OUTPUT_SERVERS, configs, Config::getString, v -> this.servers = v);
			bindProp(OUTPUT_TIMEOUT, configs,Config::getString, v -> this.timeout = v);
			bindProp(OUTPUT_ENABLED, configs,Config::getBoolean, v -> this.enabled = v);
			bindProp(OUTPUT_SECURITY_PROTOCOL, configs,  Config::getString, v -> this.protocol = v);
			bindProp(OUTPUT_SSL_KEYSTORE_TYPE, configs,Config::getString, v -> this.sslKeyStoreType = v);
			bindProp(OUTPUT_SSL_KEYSTORE_KEY,configs,Config::getString, v -> this.sslKey = v);
			bindProp(OUTPUT_SSL_KEYSTORE_CERT_CHAIN, configs,Config::getString, v -> this.certificate = v);
			bindProp(OUTPUT_SSL_TRUSTSTORE_CERTS, configs,Config::getString, v -> this.trustCertificate = v);
			bindProp(OUTPUT_SSL_TRUSTSTORE_TYPE, configs,Config::getString, v -> this.trustCertificateType = v);
			bindProp(OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM, configs,Config::getString, v -> this.endpointAlgorithm = v);

			// if there are v2 configuration items, override with v2 config.
			bindProp(BOOTSTRAP_SERVERS, configs, Config::getString, v -> this.servers = v);
			bindProp(OUTPUT_SERVERS_TIMEOUT,configs, Config::getString, v -> this.timeout = v);
			bindProp(OUTPUT_SERVERS_ENABLE, configs, Config::getBoolean, v -> this.enabled = v);
			bindProp(OUTPUT_SECURITY_PROTOCOL_V2, configs, Config::getString, v -> this.protocol = v);
			bindProp(OUTPUT_SSL_KEYSTORE_TYPE_V2,configs,Config::getString,v -> this.sslKeyStoreType = v);
			bindProp(OUTPUT_KEY_V2,configs,Config::getString,v -> this.sslKey = v);
			bindProp(OUTPUT_CERT_V2, configs, Config::getString, v -> this.certificate = v);
			bindProp(OUTPUT_TRUST_CERT_V2, configs,Config::getString, v -> this.trustCertificate = v);
			bindProp(OUTPUT_TRUST_CERT_TYPE_V2, configs,Config::getString, v -> this.trustCertificateType = v);
			bindProp(OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM_V2,configs,Config::getString,v -> this.endpointAlgorithm = v);
		}
	}
}

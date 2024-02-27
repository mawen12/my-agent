package com.mawen.agent.report;

import java.util.Map;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.config.Configs;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.config.ConfigConst.Observability;

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

	String getEndpointAlgorithm();

	boolean updateConfig(Map<String, String> changed);

	static OutputProperties newDefault(Config configs) {
		return new ;
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

		}

		@Override
		public String getServers() {
			return null;
		}

		@Override
		public String getTimeout() {
			return null;
		}

		@Override
		public Boolean isEnabled() {
			return null;
		}

		@Override
		public String getSecurityProtocol() {
			return null;
		}

		@Override
		public String getSSLKeyStoreType() {
			return null;
		}

		@Override
		public String getKeyStoreKey() {
			return null;
		}

		@Override
		public String getKeyStoreCertChain() {
			return null;
		}

		@Override
		public String getTrustCertificate() {
			return null;
		}

		@Override
		public String getEndpointAlgorithm() {
			return null;
		}

		@Override
		public boolean updateConfig(Map<String, String> changed) {
			Config configs = new Configs(changed);
			int changeItems = 0;
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_SERVERS, changed, this.servers);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_TIMEOUT, changed, this.timeout);
			changeItems += ConfigUtils.isChanged(Observability.OUTPUT_ENABLED, changed, String.valueOf(this.enabled));
			changeItems += ConfigUtils.isChanged();
			return false;
		}
	}
}

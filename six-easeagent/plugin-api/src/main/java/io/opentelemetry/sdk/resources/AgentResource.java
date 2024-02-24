package io.opentelemetry.sdk.resources;

import java.util.List;

import javax.annotation.Nullable;

import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.otlp.common.AgentAttributes;
import com.mawen.agent.plugin.api.otlp.common.SemanticKey;
import com.mawen.agent.plugin.bridge.Agent;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AgentResource extends Resource implements ConfigChangeListener {
	static volatile AgentResource agentResource = null;

	private final Resource resource;
	private String service;
	private String system;

	private AgentResource() {
		super();
		this.service = Agent.getConfig("service", "demo-service");
		this.system = Agent.getConfig("system", "demo-system");

		this.resource = Resource.getDefault()
				.merge(Resource.create(
						AgentAttributes.builder()
								.put(ResourceAttributes.SERVICE_NAME, this.service)
								.put(ResourceAttributes.SERVICE_NAMESPACE, this.system)
								.build()
				));
	}

	public static AgentResource getResource() {
		if (agentResource == null) {
			synchronized (AgentResource.class) {
				if (agentResource == null) {
					agentResource = new AgentResource();
				}
			}
		}

		return agentResource;
	}

	@Override
	public void onChange(List<ChangeItem> list) {
		list.forEach(change -> {
			if (change.getFullName().equals("name")) {
				this.service = change.getNewValue();
			}
			else if (change.getFullName().equals("system")) {
				this.system = change.getNewValue();
			}

			this.resource.merge(Resource.create(
					Attributes.builder()
							.put(ResourceAttributes.SERVICE_NAME, this.service)
							.put(ResourceAttributes.SERVICE_NAMESPACE, this.system)
							.build()
			));

		});
	}

	@Nullable
	@Override
	public String getSchemaUrl() {
		return SemanticKey.SCHEMA_URL;
	}

	@Override
	public Attributes getAttributes() {
		return this.resource.getAttributes();
	}
}

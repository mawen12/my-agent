package com.mawen.agent.core.env;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Properties;

import com.mawen.agent.core.io.JarUrlResource;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.Assert;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/4/2
 */
public class AgentEnvironment extends StandardEnvironment {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final String AGENT_RESOURCE_NAME = "agent";

	public static final String AGENT_PROPERTY_SOURCE_NAME = "agent.properties";

	private final String path;

	public AgentEnvironment(String path) {
		Assert.hasText(path, "Path must not be empty");
		this.path = path;
	}

	@Override
	protected void customizePropertySources(MutablePropertySources propertySources) {
		propertySources.addLast(getAgentPropertySource());

		// set system properties and system environment in StandardEnvironment
		super.customizePropertySources(propertySources);
	}

	private MapPropertySource getAgentPropertySource() {
		try {
			JarUrlResource agentResource = getAgentResource();
			if (agentResource != null) {
				return new ResourcePropertySource(AGENT_RESOURCE_NAME);
			}
		}
		catch (IOException e) {
			log.warn("Failed to load resource from [{}]", path + AGENT_PROPERTY_SOURCE_NAME, e);
		}
		return new PropertiesPropertySource(AGENT_RESOURCE_NAME, new Properties());
	}

	private JarUrlResource getAgentResource() {
		try {
			return new JarUrlResource(path, AGENT_PROPERTY_SOURCE_NAME);
		}
		catch (MalformedURLException e) {
			log.warn("Failed to load {} from {}", AGENT_PROPERTY_SOURCE_NAME, path, e);
			return null;
		}
	}
}

package com.mawen.agent.core.env;

import java.util.Map;

import com.mawen.agent.core.utils.Assert;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class SystemEnvironmentPropertySource extends MapPropertySource {

	public SystemEnvironmentPropertySource(String name, Map<String, Object> source) {
		super(name, source);
	}

	@Override
	public boolean containsProperty(String name) {
		return getProperty(name) != null;
	}

	@Override
	public Object getProperty(String name) {
		String actualName = resolvePropertyName(name);
		if (logger.isDebugEnabled() && !name.equals(actualName)) {
			logger.debug("PropertySource '" + getName() + "' does not contain property '" + name +
					"', but found equivalent '" + actualName + "'");
		}
		return super.getProperty(name);
	}

	protected final String resolvePropertyName(String name) {
		Assert.notNull(name,"Property name must not be null");
		String resolvedName = checkPropertyName(name);
		if (resolvedName != null) {
			return resolvedName;
		}
		String upperCasedName = name.toUpperCase();
		if (!name.equals(upperCasedName)) {
			resolvedName = checkPropertyName(upperCasedName);
			if (resolvedName != null) {
				return resolvedName;
			}
		}
		return name;
	}

	private String checkPropertyName(String name) {
		// Check name as-is
		if (this.source.containsKey(name)) {
			return name;
		}
		// Check name with just dots replaced
		String noDotName = name.replace('.', '_');
		if (!name.equals(noDotName) && this.source.containsKey(noDotName)) {
			return noDotName;
		}
		// Check name with just hypens replaced
		String noHyphenName = name.replace('-', '_');
		if (!name.equals(noHyphenName) && this.source.containsKey(noHyphenName)) {
			return noHyphenName;
		}
		// Check name with dots and hyphens replaced
		String noDotNoHyphenName = noDotName.replace('-', '_');
		if (!noDotName.equals(noDotNoHyphenName) && this.source.containsKey(noDotNoHyphenName)) {
			return noDotNoHyphenName;
		}
		// Give up
		return null;
	}

}

package com.mawen.agent.plugin.api.metric.name;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.mawen.agent.plugin.api.ProgressFields;

/**
 * A tags describing the metric.
 * It has three predefined tags: category, type, {@code keyFieldName}
 * Its tag is scoped as follows:
 * <pre>{@code
 *  output.put("category", tags.category);
 *  output.put("type", tags.type);
 *  output.put(tags.keyFieldName, NameFactory.key[i])
 *  tags.tags.forEach((k,v) -> {
 *     output.put(k,v);
 *  });
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class Tags {
	public static final String CATEGORY = "category";
	public static final String TYPE = "type";
	private final String category;
	private final String type;
	private final String keyFieldName;
	private final Map<String, String> tags;

	public Tags(@Nonnull String category, @Nonnull String type, @Nonnull String keyFieldName) {
		this.category = category;
		this.type = type;
		this.keyFieldName = keyFieldName;
		this.tags = new HashMap<>(ProgressFields.getServiceTags());
	}

	/**
	 * put tag for.
	 *
	 * @param key tag key
	 * @param value tag value
	 * @return this methods return {@linkplain Tags} for chaining, but the instance is always the same.
	 */
	public Tags put(String key, String value) {
		this.tags.put(key, value);
		return this;
	}

	/**
	 * tag type describing of the metrics.
	 * for example: "application"
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * tag type describing of the metrics.
	 * for example: "http-request"
	 */
	public String getType() {
		return type;
	}

	/**
	 * tag {@link NameFactory} keys describing of the metrics.
	 * for example: "url"
	 *
	 * <pre>{@code
	 *  keyFieldName = "url";
	 *  nameFactory.timerName("http://127.0.0.1:8080/", ...)
	 *  // it will be tag.put("url", "http://127.0.0.1:8080/");
	 * }</pre>
	 *
	 * @see NameFactory
	 */
	public String getKeyFieldName() {
		return keyFieldName;
	}

	/**
	 * Custom tags describing of the metrics.
	 *
	 * @return custom tags
	 */
	public Map<String, String> getTags() {
		return tags;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Tags tags1)) return false;

		return category.equals(tags1.category) && type.equals(tags1.type) && keyFieldName.equals(tags1.keyFieldName) && tags.equals(tags1.tags);
	}

	@Override
	public int hashCode() {
		int result = category.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + keyFieldName.hashCode();
		result = 31 * result + tags.hashCode();
		return result;
	}
}

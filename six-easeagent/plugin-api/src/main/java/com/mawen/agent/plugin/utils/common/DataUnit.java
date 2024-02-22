package com.mawen.agent.plugin.utils.common;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public enum DataUnit {

	/**
	 * Bytes, represented by suffix {@code B}.
	 */
	BYTES("B", DataSize.ofBytes(1L)),
	/**
	 * Kilobytes, represented by suffix {@code KB}.
	 */
	KILOBYTES("KB", DataSize.ofKilobytes(1L)),
	/**
	 * Megabytes, represented by suffix {@code MB}.
	 */
	MEGABYTES("MB", DataSize.ofMegabytes(1L)),
	/**
	 * Gigabytes, represented by suffix {@code GB}.
	 */
	GIGABYTES("GB", DataSize.ofGigabytes(1L)),
	/**
	 * Terabytes, represented by suffix {@code TB}
	 */
	TERABYTES("TB", DataSize.ofTerabytes(1L)),
	;

	private final String suffix;
	private final DataSize size;

	DataUnit(String suffix, DataSize size) {
		this.suffix = suffix;
		this.size = size;
	}

	DataSize size() {
		return this.size;
	}

	/**
	 * Return the {@link DataUnit} matching the specified {@code suffix}.
	 *
	 * @param suffix one of the standard suffixes
	 * @return the {@link DataUnit} matching the specified {@code suffix}.
	 * @throws IllegalArgumentException if the suffix does not match the suffix of any of this enum's constants
	 */
	public static DataUnit fromSuffix(String suffix) {
		for (DataUnit candidate : values()) {
			if (candidate.suffix.equals(suffix)) {
				return candidate;
			}
		}

		throw new IllegalArgumentException("Unknown data unit suffix '" + suffix + "'");
	}
}

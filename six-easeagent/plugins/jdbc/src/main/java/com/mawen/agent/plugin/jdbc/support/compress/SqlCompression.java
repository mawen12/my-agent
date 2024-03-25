package com.mawen.agent.plugin.jdbc.support.compress;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
@FunctionalInterface
public interface SqlCompression {

	SqlCompression DEFAULT = origin -> origin;

	String compress(String origin);
}

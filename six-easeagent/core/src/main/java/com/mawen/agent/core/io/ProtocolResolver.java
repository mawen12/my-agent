package com.mawen.agent.core.io;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/31
 */
@FunctionalInterface
public interface ProtocolResolver {

	Resource resolve(String location, ResourceLoader resourceLoader);
}

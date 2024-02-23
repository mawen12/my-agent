package com.mawen.agent.plugin.api.context;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class ContextCons {
	String CACHE_CMD = ContextCons.class.getName() + ".cache_cmd";
	String CACHE_URI = ContextCons.class.getName() + ".cache_uri";
	String MQ_URI = ContextCons.class.getName() + ".mq_uri";
	String ASYNC_FLAG = ContextCons.class.getName() + ".async";
	String SPAN = ContextCons.class.getName() + ".Span";
	String PROCESSED_BEFORE = ContextCons.class.getName() + ".Processed_before";
	String PROCESSED_AFTER = ContextCons.class.getName() + ".Processed_after";
}

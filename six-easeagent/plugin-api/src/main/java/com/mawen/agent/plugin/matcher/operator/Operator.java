package com.mawen.agent.plugin.matcher.operator;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Operator<S> {
	S and(S matcher);
	S or(S matcher);
	S negate();
}

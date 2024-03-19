package com.mawen.agent.mock.report;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 3.4.2
 */
public interface JsonReporter {

	void report(List<Map<String, Object>> json);
}

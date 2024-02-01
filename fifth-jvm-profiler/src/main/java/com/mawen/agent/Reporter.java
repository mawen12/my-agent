package com.mawen.agent;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public interface Reporter {

	default void updateArguments(Map<String, List<String>> parsedArgs) {

	}

	void report(String profilerName, Map<String, Object> metrics);

	void close();

}

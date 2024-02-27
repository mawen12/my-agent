package com.mawen.agent.report;

import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public interface ReportConfigChange {
	void updateConfigs(Map<String, String> outputProperties);
}

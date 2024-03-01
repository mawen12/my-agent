package com.mawen.agent.httpserver.nano;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public interface AgentHttpHandlerProvider {

	List<AgentHttpHandler> getAgentHttpHandlers();
}

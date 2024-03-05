package com.mawen.agent.metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import com.mawen.agent.httpserver.nano.AgentHttpHandler;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class PrometheusAgentHttpHandler extends AgentHttpHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusAgentHttpHandler.class);

	@Override
	public String getPath() {
		return "/prometheus/metrics";
	}

	@Override
	public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		Map<String, String> headers = session.getHeaders();
		String contentType = TextFormat.chooseContentType(headers.get("Accept"));

		Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());

		StringWriter stringWriter = new StringWriter();
		try (Writer writer = new BufferedWriter(stringWriter)) {
			TextFormat.writeFormat(contentType, writer, samples);
			writer.flush();
		}
		catch (IOException e) {
			LOGGER.warn("write data error. {}", e.getMessage());
		}
		String data = stringWriter.toString();
		return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, data);
	}
}

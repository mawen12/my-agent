package com.mawen.agent.plugin.jdbc.interceptor.tracing;

import java.sql.Connection;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.context.ContextUtils;
import com.mawen.agent.plugin.api.middleware.MiddlewareConstants;
import com.mawen.agent.plugin.api.middleware.Redirect;
import com.mawen.agent.plugin.api.middleware.RedirectProcessor;
import com.mawen.agent.plugin.api.middleware.Type;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.enums.Order;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.interceptor.NonReentrantInterceptor;
import com.mawen.agent.plugin.jdbc.JdbcTracingPlugin;
import com.mawen.agent.plugin.jdbc.advice.JdbcStatementAdvice;
import com.mawen.agent.plugin.jdbc.support.DatabaseInfo;
import com.mawen.agent.plugin.jdbc.support.JdbcUtils;
import com.mawen.agent.plugin.jdbc.support.SqlInfo;
import com.mawen.agent.plugin.jdbc.support.compress.SQLCompressionFactory;
import com.mawen.agent.plugin.jdbc.support.compress.SqlCompression;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
@AdviceTo(value = JdbcStatementAdvice.class, plugin = JdbcTracingPlugin.class)
public class JdbcStmTracingInterceptor implements NonReentrantInterceptor {
	private static final Logger log = LoggerFactory.getLogger(JdbcStmTracingInterceptor.class);

	protected static final String SPAN_KEY = JdbcStmTracingInterceptor.class.getName() + "-SPAN";

	public static final String SPAN_SQL_QUERY_TAG_NAME = "sql";
	public static final String SPAN_ERROR_TAG_NAME = "error";
	public static final String SPAN_LOCAL_COMPONENT_TAG_NAME = "local-component";
	public static final String SPAN_URL = "url";

	private static volatile SqlCompression md5SQLCompression;

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		// make reference to third-part lib
		// make it loaded during init, so these classes can be found during running
		md5SQLCompression = SQLCompressionFactory.getSqlCompression();
		DigestUtils.md5Hex("");
	}

	@Override
	public void doBefore(MethodInfo methodInfo, Context context) {
		SqlInfo sqlInfo = ContextUtils.getFromContext(context, SqlInfo.class);
		if (sqlInfo == null) {
			log.warn("must get sqlInfo from context");
			return;
		}

		Span span = context.nextSpan();
		span.name(methodInfo.getMethod());
		span.kind(Span.Kind.CLIENT);
		span.tag(SPAN_SQL_QUERY_TAG_NAME, md5SQLCompression.compress(sqlInfo.getSql()));
		span.tag(SPAN_LOCAL_COMPONENT_TAG_NAME, "database");
		span.tag(MiddlewareConstants.TYPE_TAG_NAME, Type.DATABASE.getRemoteType());

		Connection conn = sqlInfo.getConnection();
		String url = JdbcUtils.getUrl(conn);
		if (url != null) {
			span.tag(SPAN_URL, url);
		}

		RedirectProcessor.setTagsIfRedirected(Redirect.DATABASE, span, url);
		DatabaseInfo databaseInfo = DatabaseInfo.getFromConnection(conn);
		if (databaseInfo != null) {
			span.remoteServiceName(databaseInfo.remoteServiceName());
			span.remoteIpAndPort(databaseInfo.getHost(), databaseInfo.getPort());
		}

		span.start();
		context.put(SPAN_KEY, span);
	}

	@Override
	public void doAfter(MethodInfo methodInfo, Context context) {
		Span span = context.get(SPAN_KEY);
		if (methodInfo.getThrowable() != null) {
			span.error(methodInfo.getThrowable());
		}
		span.finish();
	}

	@Override
	public String getType() {
		return Order.TRACING.getName();
	}

	@Override
	public int order() {
		return Order.TRACING.getOrder();
	}
}

package com.mawen.agent.log4j2.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.slf4j.EventDataConverter;
import org.apache.logging.slf4j.Log4jMarker;
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.StaticMarkerBinder;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class AgentLoggerProxy implements LocationAwareLogger, Serializable {

	private static final long serialVersionUID = -6526111633435401713L;
	private static final Marker EVENT_MARKER = MarkerFactory.getMarker("EVENT");
	private static final EventDataConverter CONVERTER = createConverter();

	private final boolean eventLogger;
	private transient ExtendedLogger logger;
	private final String name;
	private final String loggerFqcn;

	public AgentLoggerProxy(ExtendedLogger logger, String name, String loggerFqcn) {
		this.logger = logger;
		this.eventLogger = "EventLogger".equals(name);
		this.name = name;
		this.loggerFqcn = loggerFqcn;
	}

	@Override
	public void log(final Marker marker, final String fqcn, final int level, final String message, final Object[] params, Throwable throwable) {
		final var log4jLevel = getLevel(level);
		final var log4jMarker = getMarker(marker);

		if (!logger.isEnabled(log4jLevel, log4jMarker, message, params)) {
			return;
		}
		final Message msg;
		if (CONVERTER != null && eventLogger && marker != null && marker.contains(EVENT_MARKER)) {
			msg = CONVERTER.convertEvent(message, params, throwable);
		}
		else if (params == null) {
			msg = new SimpleMessage(message);
		}
		else {
			msg = new ParameterizedMessage(message, params, throwable);
			if (throwable != null) {
				throwable = msg.getThrowable();
			}
		}
		logger.logMessage(fqcn, log4jLevel, log4jMarker, msg, throwable);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isEnabled(Level.TRACE, null, null);
	}

	@Override
	public void trace(String format) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format);
	}

	@Override
	public void trace(String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format, o);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... args) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format, args);
	}

	@Override
	public void trace(String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format, t);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return logger.isEnabled(Level.TRACE, getMarker(marker), null);
	}

	@Override
	public void trace(Marker marker, String format) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), format);
	}

	@Override
	public void trace(Marker marker, String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), format, o);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), format, params);
	}

	@Override
	public void trace(Marker marker, String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), format, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isEnabled(Level.DEBUG, null, null);
	}

	@Override
	public void debug(String format) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format);
	}

	@Override
	public void debug(String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format, o);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format, params);
	}

	@Override
	public void debug(String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format, t);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return logger.isEnabled(Level.DEBUG, getMarker(marker), null);
	}

	@Override
	public void debug(Marker marker, String format) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), format);
	}

	@Override
	public void debug(Marker marker, String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), format, o);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), format, params);
	}

	@Override
	public void debug(Marker marker, String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), format, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isEnabled(Level.INFO, null, null);
	}

	@Override
	public void info(String format) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, null, format);
	}

	@Override
	public void info(String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, null, format, o);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, null, format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, null, format, params);
	}

	@Override
	public void info(String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, null, format, t);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return logger.isEnabled(Level.INFO, getMarker(marker), null);
	}

	@Override
	public void info(Marker marker, String format) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), format);
	}

	@Override
	public void info(Marker marker, String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), format, o);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), format, params);
	}

	@Override
	public void info(Marker marker, String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), format, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isEnabled(Level.WARN, null, null);
	}

	@Override
	public void warn(String format) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, null, format);
	}

	@Override
	public void warn(String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, null, format, o);
	}

	@Override
	public void warn(String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, null, format, params);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, null, format, arg1, arg2);
	}

	@Override
	public void warn(String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, null, format, t);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return logger.isEnabled(Level.WARN, getMarker(marker), null);
	}

	@Override
	public void warn(Marker marker, String format) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), format);
	}

	@Override
	public void warn(Marker marker, String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), format, o);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), format, params);
	}

	@Override
	public void warn(Marker marker, String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), format, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isEnabled(Level.ERROR, null, null);
	}

	@Override
	public void error(String format) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format);
	}

	@Override
	public void error(String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format, o);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format, params);
	}

	@Override
	public void error(String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format, t);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return logger.isEnabled(Level.ERROR, getMarker(marker), null);
	}

	@Override
	public void error(Marker marker, String format) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), format);
	}

	@Override
	public void error(Marker marker, String format, Object o) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), format, o);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... params) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), format, params);
	}

	@Override
	public void error(Marker marker, String format, Throwable t) {
		logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), format, t);
	}

	private static EventDataConverter createConverter() {
		try {
			LoaderUtil.loadClass("org.slf4j.ext.EventData");
			return new EventDataConverter();
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static Level getLevel(final int i) {
		return switch (i) {
			case TRACE_INT -> Level.TRACE;
			case DEBUG_INT -> Level.DEBUG;
			case INFO_INT -> Level.INFO;
			case WARN_INT -> Level.WARN;
			case ERROR_INT -> Level.ERROR;
			default -> Level.ERROR;
		};
	}

	private static org.apache.logging.log4j.Marker getMarker(final Marker marker) {
		if (marker == null) {
			return null;
		}
		else if (marker instanceof Log4jMarker log4jMarker) {
			return log4jMarker.getLog4jMarker();
		}
		else {
			final Log4jMarkerFactory factory = (Log4jMarkerFactory) StaticMarkerBinder.SINGLETON.getMarkerFactory();
			return ((Log4jMarker) factory.getMarker(marker)).getLog4jMarker();
		}
	}

	// region serialization & deserialization

	private void readObject(final ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
		// always perform the default de-serialization first
		aInputStream.defaultReadObject();
		logger = LogManager.getContext().getLogger(name);
	}

	private void writeObject(final ObjectOutputStream aOutputStream) throws IOException {
		// perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject();
	}

	// endregion
}

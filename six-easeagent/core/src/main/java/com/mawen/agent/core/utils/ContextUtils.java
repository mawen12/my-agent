package com.mawen.agent.core.utils;

import com.google.auto.service.AutoService;
import com.mawen.agent.core.AppendBootstrapClassLoaderSearch;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.utils.SystemClock;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
@AutoService(AppendBootstrapClassLoaderSearch.class)
public class ContextUtils {

	private static final String BEGIN_TIME = com.mawen.agent.plugin.api.context.ContextUtils.class.getSimpleName() + ".beginTime";
	private static final String END_TIME = com.mawen.agent.plugin.api.context.ContextUtils.class.getSimpleName() + ".endTime";

	public static void setBeginTime(Context context) {
		context.put(BEGIN_TIME, SystemClock.now());
	}

	public static Long getBeginTime(Context context) {
		return context.get(BEGIN_TIME);
	}

	public static Long getEndTime(Context context) {
		Long endTime = context.remove(END_TIME);
		if (endTime == null) {
			return SystemClock.now();
		}
		return endTime;
	}

	public static Long getDuration(Context context) {
		return getEndTime(context) - getBeginTime(context);
	}

	public static Long getDuration(Context context, Object startKey) {
		Long now = SystemClock.now();
		return now - (Long) context.remove(startKey);
	}

	/**
	 * Get data from context
	 *
	 * @param context Store data
	 * @param key key is the type of data. Like {@code value.getClass()}
	 * @param <T> The type of data
	 * @return data
	 */
	public static <T> T getFormContext(Context context, Object key) {
		return context.get(key);
	}

	/**
	 * Remove data from context
	 *
	 * @param context data store
	 * @param key key is the type of data.
	 * @param <T> The type of data
	 * @return data
	 */
	public static <T> T removeFromContext(Context context, Object key) {
		return context.remove(key);
	}

	private ContextUtils(){}
}

package com.mawen.agent.plugin.api.context;

import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.utils.SystemClock;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class ContextUtils {
	private static final String BEGIN_TIME = ContextUtils.class.getSimpleName() + ".beginTime";
	private static final String END_TIME = ContextUtils.class.getSimpleName() + ".endTime";

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

}

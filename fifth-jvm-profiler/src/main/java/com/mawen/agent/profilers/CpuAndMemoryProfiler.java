package com.mawen.agent.profilers;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.mawen.agent.Profiler;
import com.mawen.agent.Reporter;
import com.mawen.agent.util.AgentLogger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class CpuAndMemoryProfiler extends ProfilerBase implements Profiler {
	private static final AgentLogger logger = AgentLogger.getLogger(CpuAndMemoryProfiler.class.getName());
	public static final String PROFILER_NAME = "CpuAndMemory";

	private static final String ATTRIBUTE_NAME_ProcessCpuLoad = "ProcessCpuLoad";
	private static final int ATTRIBUTE_INDEX_ProcessCpuLoad = 0;
	private static final String ATTRIBUTE_NAME_SystemCpuLoad = "SystemCpuLoad";
	private static final int ATTRIBUTE_INDEX_SystemCpuLoad = 1;
	private static final String ATTRIBUTE_NAME_ProcessCpuTime = "ProcessCpuTime";
	private static final int ATTRIBUTE_INDEX_ProcessCpuTime = 2;

	private long intervalMillis = Constants.DEFAULT_METRIC_INTERVAL;

	private MBeanServer platformMBeanServer;
	private ObjectName operatingSystemObjectName;
	private MemoryMXBean memoryMXBean;
	private Reporter reporter;

	public CpuAndMemoryProfiler(Reporter reporter) {
		this.reporter = reporter;

		init();
	}

	@Override
	public long getIntervalMillis() {
		return intervalMillis;
	}

	public void setIntervalMillis(long intervalMillis) {
		this.intervalMillis = intervalMillis;
	}

	@Override
	public void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public synchronized void profile() {
		Double processCpuLoad = null;
		Double systemCpuLoad = null;
		Long processCpuTime = null;

		AttributeList cpuAttributes = getCpuAttributes();
		if (cpuAttributes != null && cpuAttributes.size() > 0) {
			Attribute att = (Attribute) cpuAttributes.get(ATTRIBUTE_INDEX_ProcessCpuLoad);
			processCpuLoad = (Double) att.getValue();
			if (processCpuLoad == Double.NaN) {
				processCpuLoad = null;
			}

			att = (Attribute) cpuAttributes.get(IN)
		}
	}

	private void init() {
		try {
			platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			operatingSystemObjectName = ObjectName.getInstance("java.lang:type=OperatingSystem");
		}
		catch (Throwable ex) {
			logger.warn("Failed to get Operation System MBean", ex);
		}

		try {
			memoryMXBean = ManagementFactory.getMemoryMXBean();
		}
		catch (Throwable ex) {
			logger.warn("Failed to get Memory MBean", ex);
		}
	}

	private AttributeList getCpuAttributes() {
		try {
			String[] names = new String[]{ATTRIBUTE_NAME_ProcessCpuLoad, ATTRIBUTE_NAME_SystemCpuLoad, ATTRIBUTE_NAME_ProcessCpuTime};
			AttributeList list = platformMBeanServer.getAttributes(operatingSystemObjectName, names);
			if (list.size() != names.length) {
				logger.warn("Failed to get all attributes");
				return new AttributeList();
			}
		}
		catch (Throwable ex) {
			logger.warn("Failed to get CPU MBean attributes", ex);
			return null;
		}

	}
}

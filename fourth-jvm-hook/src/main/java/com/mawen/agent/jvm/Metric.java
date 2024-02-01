package com.mawen.agent.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class Metric {

	private static final long MB = 1048576L;

	public static void printMemoryInfo () {
		MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapMemory = memory.getHeapMemoryUsage();

		String info = String.format("\ninit: %s\t max: %s\t used: %s\t committed: %s\t use rate: %s\t",
				heapMemory.getInit() / MB + "MB",
				heapMemory.getMax() / MB + "MB", heapMemory.getUsed() / MB + "MB",
				heapMemory.getCommitted() / MB + "MB",
				heapMemory.getUsed() * 100 / heapMemory.getCommitted() + "%");

		System.out.println(info);

		MemoryUsage nonHeapMemory = memory.getNonHeapMemoryUsage();

		info = String.format("init: %s\t max: %s\t used: %s\t commited: %s\t use rate: %s\t",
				nonHeapMemory.getInit() / MB + "MB",
				nonHeapMemory.getMax() / MB + "MB", nonHeapMemory.getUsed() / MB + "MB",
				nonHeapMemory.getCommitted() / MB + "MB",
				nonHeapMemory.getUsed() * 100 / nonHeapMemory.getCommitted() + "%");
		System.out.println(info);
	}

	public static void printGCInfo() {
		List<GarbageCollectorMXBean> garbages = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean garbage : garbages) {
			String info = String.format("name:%s\t count:%s\t took:%s\t pool name:%s",
					garbage.getName(),
					garbage.getCollectionCount(),
					garbage.getCollectionTime(),
					Arrays.deepToString(garbage.getMemoryPoolNames()));
			System.out.println(info);
		}
	}

}

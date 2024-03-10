package com.mawen.agent.log4j2.impl;

import com.mawen.agent.log4j2.MDC;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class MDCTest {

	@Test
	public void put() {
		MDC.put("testA", "testB");
		assertNull(org.slf4j.MDC.get("testA"));
		assertNotNull(MDC.get("testA"));
	}

	@Test
	public void remove() {
		MDC.put("testA", "testB");
		assertNotNull(MDC.get("testA"));
		MDC.remove("testA");
		assertNull(MDC.get("testA"));
	}

	@Test
	public void get() {
		MDC.put("testA", "testB");
		assertNull(org.slf4j.MDC.get("testA"));
		assertNotNull(MDC.get("testA"));

		org.slf4j.MDC.put("testB", "testB");
		assertNotNull(org.slf4j.MDC.get("testB"));
		assertNull(MDC.get("testB"));
	}
}

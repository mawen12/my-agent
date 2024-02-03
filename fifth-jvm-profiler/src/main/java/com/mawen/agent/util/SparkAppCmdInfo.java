package com.mawen.agent.util;

import java.util.Arrays;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class SparkAppCmdInfo {

	private String appClass;
	private String appJar;
	private String[] args = new String[0];

	public String getAppClass() {
		return appClass;
	}

	public void setAppClass(String appClass) {
		this.appClass = appClass;
	}

	public String getAppJar() {
		return appJar;
	}

	public void setAppJar(String appJar) {
		this.appJar = appJar;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		if (args == null) {
			this.args = new String[0];
		} else {
			this.args = Arrays.copyOf(args, args.length);
		}
	}

	@Override
	public String toString() {
		return "SparkAppCmdInfo{" +
				"appClass='" + appClass + '\'' +
				", appJar='" + appJar + '\'' +
				", args=" + Arrays.toString(args) +
				'}';
	}
}

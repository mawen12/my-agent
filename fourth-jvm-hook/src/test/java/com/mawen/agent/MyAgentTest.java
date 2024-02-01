package com.mawen.agent;


import java.util.ArrayList;
import java.util.List;

class MyAgentTest {

	// add VM options: -javaagent:/Users/mawen/Documents/GitHub/mawen12/my-agent/fourth-jvm-hook/target/fourth-jvm-hook.jar
	public static void main(String[] args) {
		boolean is = true;
		while (is) {
			List<Object> list = new ArrayList<>();
			list.add("hello world");
		}
	}
}
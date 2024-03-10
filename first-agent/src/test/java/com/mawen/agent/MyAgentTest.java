package com.mawen.agent;

class MyAgentTest {

	// add VM options: -javaagent:/Users/mawen/Documents/GitHub/mawen12/my-agent/first-agent/target/first-agent.jar
	public static void main(String[] args) {

		System.out.println("This is main");

		System.out.println(System.getProperty("user.dir"));
	}

}
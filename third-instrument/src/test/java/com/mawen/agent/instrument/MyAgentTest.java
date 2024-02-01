package com.mawen.agent.instrument;


import com.mawen.agent.instrument.another.AnotherTest;

class MyAgentTest {

	// add VM options: -javaagent:/Users/mawen/Documents/GitHub/mawen12/my-agent/third-instrument/target/third-instrument.jar
	public static void main(String[] args) {
		MyAgentTest test = new MyAgentTest();
		test.fun1();
		test.fun2();

		AnotherTest test1 = new AnotherTest();
		test1.fun3();
		test1.fun4();
	}

	private void fun1() {
		System.out.println("This is fun 1.");
	}

	private void fun2() {
		System.out.println("This is fun 2.");
	}

}
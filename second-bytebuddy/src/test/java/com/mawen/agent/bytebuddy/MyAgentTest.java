package com.mawen.agent.bytebuddy;


class MyAgentTest {

	// add VM options: -javaagent:/Users/mawen/Documents/GitHub/mawen12/my-agent/second-bytebuddy/target/second-bytebuddy.jar
	public static void main(String[] args) throws InterruptedException {
		System.out.println(System.getProperty("user.dir"));
		MyAgentTest myAgentTest = new MyAgentTest();
		myAgentTest.fun1();
		myAgentTest.fun2();
	}

	private void fun1() throws InterruptedException {
		System.out.println("This is fun 1.");
		Thread.sleep(500);
	}

	private void fun2() throws InterruptedException {
		System.out.println("This is fun 2.");
		Thread.sleep(500);
	}
}
package com.mawen.agent.instrument.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class PerformMonitorTransformer implements ClassFileTransformer {

	private static final String PACKAGE_PREFIX = "com.mawen.agent.instrument.another";

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		String currentClassName = className.replaceAll("/", ".");

		// 仅仅提升这个包的类
		if (!currentClassName.startsWith(PACKAGE_PREFIX)) {
			return null;
		}

		System.out.println("now transform: [" + currentClassName + "]");
		try {
			CtClass ctClass = ClassPool.getDefault().get(currentClassName);
			CtBehavior[] methods = ctClass.getDeclaredBehaviors();
			for (CtBehavior method : methods) {
				enhanceMethod(method); // 提升方法
			}

			return ctClass.toBytecode();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 提升方法
	private void enhanceMethod(CtBehavior method) throws CannotCompileException {
		if (method.isEmpty()) {
			return;
		}
		String methodName = method.getName();
		if (methodName.equalsIgnoreCase("main")) { // 不提升main方法
			return;
		}

		ExprEditor editor = new ExprEditor(){
			@Override
			public void edit(MethodCall methodCall) throws CannotCompileException {
				methodCall.replace(genSource(methodName));
			}
		};
		method.instrument(editor);
	}

	private String genSource(String methodName) {
		StringBuilder sb = new StringBuilder();
		sb.append("{")
				.append("long start = System.nanoTime();\n") // 前置增强：打入时间戳
				.append("$_ = $proceed($$);\n") // 保留原有的代码处理逻辑
				.append("System.out.print(\"method:[" + methodName + "]\");").append("\n")
				.append("System.out.println(\" cost:[\" +(System.nanoTime() - start) + \"ns]\");") // 后置增强
				.append("}");
		return sb.toString();
	}
}

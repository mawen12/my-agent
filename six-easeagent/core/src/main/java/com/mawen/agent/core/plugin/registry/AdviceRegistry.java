package com.mawen.agent.core.plugin.registry;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.mawen.agent.core.plugin.Dispatcher;
import com.mawen.agent.core.plugin.matcher.MethodTransformation;
import com.mawen.agent.core.plugin.transformer.advice.AgentAdvice;
import com.mawen.agent.core.plugin.transformer.advice.AgentJavaConstantValue;
import com.mawen.agent.core.plugin.transformer.advice.MethodIdentityJavaConstant;
import com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.interceptor.AgentInterceptorChain;
import com.mawen.agent.plugin.utils.common.WeakConcurrentMap;
import lombok.Getter;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class AdviceRegistry {
	private static final Logger log = LoggerFactory.getLogger(AdviceRegistry.class);
	private static final ThreadLocal<WeakReference<ClassLoader>> CURRENT_CLASS_LOADER = new ThreadLocal<>();

	static Map<String, PointcutsUniqueId> methodsSet = new ConcurrentHashMap<>();

	public static Integer check(TypeDescription instrumentedType,
			MethodDescription instrumentedMethod,
			AgentAdvice.Dispatcher.Resolved.ForMethodEnter methodEnter,
			AgentAdvice.Dispatcher.Resolved.ForMethodExit methodExit) {
		String clazz = instrumentedType.getName();
		String method = instrumentedMethod.getName();
		String methodDescriptor = instrumentedMethod.getDescriptor();
		String key = clazz.concat(":").concat(method).concat(methodDescriptor);
		PointcutsUniqueId newIdentity = new PointcutsUniqueId();
		PointcutsUniqueId pointcutsUniqueId = methodsSet.putIfAbsent(key, newIdentity);

		Integer pointcutIndex;
		boolean merge = false;

		if (pointcutsUniqueId != null) {
			newIdentity.tryRelease();
			pointcutIndex = getPointcutIndex(methodEnter);
			if (pointcutsUniqueId.checkPointcutExist(pointcutIndex)) {
				if (pointcutsUniqueId.checkClassLoaderExist()) {
					return 0;
				} else {
					updateStackManipulation(methodEnter, pointcutsUniqueId.getUniqueId());
					updateStackManipulation(methodExit, pointcutsUniqueId.getUniqueId());
					return pointcutsUniqueId.getUniqueId();
				}
			} else {
				merge = true;
			}
		} else {
			pointcutsUniqueId = newIdentity;
			pointcutIndex = updateStackManipulation(methodEnter, pointcutsUniqueId.getUniqueId());
			updateStackManipulation(methodExit, pointcutsUniqueId.getUniqueId());
		}

		MethodTransformation methodTransformation = PluginRegistry.getMethodTransformation(pointcutIndex);
		if (methodTransformation == null) {
			log.error("MethodTransformation get fail for {}", pointcutIndex);
			return 0;
		}
		int uniqueId = pointcutsUniqueId.getUniqueId();
		AgentInterceptorChain chain = methodTransformation.getAgentInterceptorChain(uniqueId, clazz, method, methodDescriptor);

		try {
			pointcutsUniqueId.lock();
			AgentInterceptorChain previousChain = Dispatcher.getChain(uniqueId);
			if (previousChain == null) {
				Dispatcher.register(uniqueId, chain);
			}
			else {
				chain.merge(previousChain);
				Dispatcher.updateChain(uniqueId, chain);
			}
		}
		finally {
			pointcutsUniqueId.unlock();
		}

		if (merge) {
			return 0;
		}

		return uniqueId;
	}

	static Integer getPointcutIndex(AgentAdvice.Dispatcher.Resolved resolved) {
		int index = 0;
		Map<Integer, OffsetMapping> map = resolved.getOffsetMapping();
		for (Map.Entry<Integer, OffsetMapping> entry : map.entrySet()) {
			OffsetMapping om = entry.getValue();
			if (!(om instanceof OffsetMapping.ForStackManipulation f)) {
				continue;
			}

			if (!(f.getStackManipulation() instanceof AgentJavaConstantValue value)) {
				continue;
			}

			index = value.getPointcutIndex();
			break;
		}
		return index;
	}

	static Integer updateStackManipulation(AgentAdvice.Dispatcher.Resolved resolved, Integer value) {
		int index = 0;
		Map<Integer, OffsetMapping> map = resolved.getOffsetMapping();

		for (Map.Entry<Integer, OffsetMapping> entry : map.entrySet()) {
			OffsetMapping om = entry.getValue();
			if (!(om instanceof OffsetMapping.ForStackManipulation f)) {
				continue;
			}

			if (!(f.getStackManipulation() instanceof AgentJavaConstantValue oldValue)) {
				continue;
			}

			index = oldValue.getPointcutIndex();

			MethodIdentityJavaConstant constant = new MethodIdentityJavaConstant(value);
			StackManipulation stackManipulation = new AgentJavaConstantValue(constant, index);
			map.put(entry.getKey(), f.with(stackManipulation));
			break;
		}

		return index;
	}

	public static void setCurrentClassLoader(ClassLoader classLoader) {
		CURRENT_CLASS_LOADER.set(new WeakReference<>(classLoader));
	}

	public static ClassLoader getCurrentClassLoader() {
		return CURRENT_CLASS_LOADER.get().get();
	}

	public static void clearCurrentClassLoader() {
		CURRENT_CLASS_LOADER.remove();
	}

	private AdviceRegistry(){}

	@Getter
	private static class PointcutsUniqueId {
		static AtomicInteger index = new AtomicInteger(1);
		ReentrantLock lock = new ReentrantLock();
		int uniqueId;
		Map<Integer, Integer> pointcutIndexSet = new ConcurrentHashMap<>();
		WeakConcurrentMap<ClassLoader, Boolean> cache = new WeakConcurrentMap<>();

		public PointcutsUniqueId() {
			this.uniqueId = index.incrementAndGet();
		}

		public boolean checkPointcutExist(Integer pointcutIndex) {
			return this.pointcutIndexSet.putIfAbsent(pointcutIndex, pointcutIndex) != null;
		}

		public boolean checkClassLoaderExist() {
			ClassLoader loader = getCurrentClassLoader();
			return cache.putIfProbablyAbsent(loader, true) != null;
		}

		public void lock() {
			this.lock.lock();
		}

		public void unlock() {
			this.lock.unlock();
		}

		public void tryRelease() {
			int id = this.uniqueId;
			index.compareAndSet(id, id - 1);
		}
	}
}

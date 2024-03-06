package com.mawen.agent.core.plugin.transformer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.field.DynamicFieldAccessor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class DynamicFieldTransformer implements AgentBuilder.Transformer {

	private static final Logger log = LoggerFactory.getLogger(DynamicFieldTransformer.class);
	private static final Map<String, Cache<ClassLoader, Boolean>> FIELD_MAP = new ConcurrentHashMap<>();

	private final String fieldName;
	private final Class<?> accessor;
	private final AgentBuilder.Transformer.ForAdvice transformer;

	public DynamicFieldTransformer(String fieldName) {
		this(fieldName, DynamicFieldAccessor.class);
	}

	public DynamicFieldTransformer(String fieldName, Class<?> accessor) {
		this.fieldName = fieldName;
		this.accessor = accessor;
		this.transformer = new AgentBuilder.Transformer
				.ForAdvice(Advice.withCustomMapping())
				.include(getClass().getClassLoader())
				.advice(ElementMatchers.isConstructor(),
						DynamicFieldAdvice.DynamicInstanceInit.class.getName());
	}

	@Override
	public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
		if (check(typeDescription, this.accessor, classLoader) && this.fieldName != null) {
			try {
				builder = builder.defineField(this.fieldName, Object.class, Opcodes.ACC_PRIVATE)
						.implement(this.accessor)
						.intercept(FieldAccessor.ofField(this.fieldName));
			}
			catch (Exception e) {
				log.debug("Type:{} add extend field again!",typeDescription.getName());
			}
			return transformer.transform(builder, typeDescription, classLoader, javaModule);
		}
		return builder;
	}

	private static boolean check(TypeDescription typeDescription,  Class<?> accessor, ClassLoader classLoader) {
		String key = typeDescription.getCanonicalName().concat(accessor.getCanonicalName());

		Cache<ClassLoader, Boolean> checkCache = FIELD_MAP.get(key);
		if (checkCache == null) {
			Cache<ClassLoader, Boolean> cache = CacheBuilder.newBuilder().weakKeys().build();
			cache.put(classLoader, true);
			checkCache = FIELD_MAP.putIfAbsent(key, cache);
			if (checkCache == null) {
				return true;
			}
		}

		return checkCache.getIfPresent(classLoader) == null;
	}
}

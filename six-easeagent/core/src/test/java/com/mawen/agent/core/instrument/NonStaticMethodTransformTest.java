package com.mawen.agent.core.instrument;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicInteger;

import com.mawen.agent.core.Bootstrap;
import com.mawen.agent.core.instrument.utils.AgentAttachmentRule;
import com.mawen.agent.core.plugin.CommonInlineAdvice;
import com.mawen.agent.core.plugin.PluginLoader;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.field.AgentDynamicFieldAccessor;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.scaffold.TypeWriter;
import net.bytebuddy.matcher.ElementMatchers;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * failed
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/9
 */
public class NonStaticMethodTransformTest extends TransformTestBase {

	private static ClassLoader classLoader;
	private static String dumpFolder;
	private static final AtomicInteger globalIndex = new AtomicInteger(1000);

	@Rule
	public MethodRule agentAttachmentRule = new AgentAttachmentRule();

	@BeforeClass
	public static void setUp() {
		Agent.initializeContextSupplier = TestContext::new;
		classLoader = new ByteArrayClassLoader.ChildFirst(
				NonStaticMethodTransformTest.class.getClassLoader(),
				ClassFileLocator.ForClassLoader.readToNames(Foo.class, CommonInlineAdvice.class),
				ByteArrayClassLoader.PersistenceHandler.MANIFEST);

		String path = "target" + File.separator + "test-classes";
		File file = new File(path);
		dumpFolder = file.getAbsolutePath();
		System.out.println(dumpFolder);
		assertTrue(dumpFolder.endsWith("target" + File.separator + "test-classes"));
	}

	@Test
	@AgentAttachmentRule.Enforce
	public void testAdviceTransformer() throws Exception {
		System.setProperty(TypeWriter.DUMP_PROPERTY, dumpFolder);
		assertEquals(System.getProperty(TypeWriter.DUMP_PROPERTY), dumpFolder);

		assertThat(ByteBuddyAgent.install(), instanceOf(Instrumentation.class));
		var agentBuilder = Bootstrap.getAgentBuilder(null, true);

		var transformations = getMethodTransformations(globalIndex.incrementAndGet(), FOO, new FooProvider());

		var extendable = agentBuilder
				.type(named(Foo.class.getName()), ElementMatchers.is(classLoader))
				.transform(PluginLoader.compound(true, transformations));
		var transformer = extendable.installOnByteBuddyAgent();

		try {
			var type = classLoader.loadClass(Foo.class.getName());
			// check
			Object instance = type.getDeclaredConstructor(String.class).newInstance("kkk");
			AgentDynamicFieldAccessor.setDynamicFieldValue(instance, BAR);
			assertEquals(BAR, AgentDynamicFieldAccessor.getDynamicFieldValue(instance));
			assertThat(type.getDeclaredMethod(FOO, String.class).invoke(instance, "kkk"),
					CoreMatchers.is(QUX + BAR));
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		finally {
			assertThat(ByteBuddyAgent.getInstrumentation().removeTransformer(transformer), CoreMatchers.is(true));
		}
	}

	public interface FooInterface {
		String foo(String a);
	}


	public static class FooBase implements FooInterface {
		@Override
		public String foo(String a) {
			return a + "-base";
		}
	}

	public static class Foo extends FooBase {
		public String instanceT;
		static String classInitString = FOO;

		public static String fooStatic(String a) {
			return a;
		}

		public Foo(String instanceT) {
			this.instanceT = instanceT;
			System.out.println("init:" + this.instanceT);
		}

		public String foo(String a) {
			return a;
		}

		public int baz() {
			return (int) System.currentTimeMillis();
		}

		public String getInstanceT() {
			return instanceT;
		}

		public static String getClassInitString() {
			return classInitString;
		}
	}
}

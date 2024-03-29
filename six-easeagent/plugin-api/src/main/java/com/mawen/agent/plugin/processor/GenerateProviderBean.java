package com.mawen.agent.plugin.processor;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Supplier;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
class GenerateProviderBean {
	private final String packageName;
	private final String interceptorClass;
	private final String pluginClass;
	private final String point;
	private final String qualifier;
	private final String providerClassExtension;
	private final TypeElement interceptor;
	private final BeanUtils utils;

	GenerateProviderBean(TypeElement plugin, TypeElement interceptor,
	                     Map<String, String> to,
	                     BeanUtils utils) {
		this.interceptor = interceptor;
		this.packageName = utils.packageNameOf(interceptor);
		this.pluginClass = utils.classNameOf(plugin).canonicalName();
		this.point = to.get("value");
		this.qualifier = to.get("qualifier") == null ? "default" : to.get("qualifier");
		this.providerClassExtension = "$Provider" + to.get("seq");
		this.interceptorClass = utils.classNameOf(interceptor).simpleName();
		this.utils = utils;
	}

	public String getProviderClass() {
		return utils.classNameOf(interceptor).canonicalName() + this.providerClassExtension;
	}

	JavaFile apply() {
		var executes = utils.asTypeElement(() -> InterceptorProvider.class).getEnclosedElements();

		var methods = new LinkedHashSet<MethodSpec>();
		for (var e : executes) {
			var pExecute = (ExecutableElement) e;
			if (pExecute.toString().startsWith("getInterceptorProvider")) {
				// getInterceptorProvider method generate
				var returnType = ParameterizedTypeName.get(Supplier.class, Interceptor.class);
				// final MethodSpec getInterceptorProvider = MethodSpec.methodBuilder("getInterceptorProvider");
				final var getInterceptorProvider = MethodSpec.overriding(pExecute)
						.addModifiers(Modifier.PUBLIC)
						.returns(returnType)
						.addCode("return " + this.interceptorClass + "::new;")
						.build();
				methods.add(getInterceptorProvider);
			}
			else if (pExecute.toString().startsWith("getAdviceTo")) {
				// getAdviceTo method generate
				final var getAdviceTo = MethodSpec.overriding(pExecute)
						.addModifiers(Modifier.PUBLIC)
						.returns(String.class)
						.addStatement("return \"$L\" + \"$L\" + \"$L\"", this.point, ":", this.qualifier)
						.build();
				methods.add(getAdviceTo);
			}
			else if (pExecute.toString().startsWith("getPluginClassName")) {
				// getPluginClassName method generate
				final var getPluginClassName = MethodSpec.overriding(pExecute)
						.addModifiers(Modifier.PUBLIC)
						.returns(String.class)
						.addStatement("return \"$L\"", this.pluginClass)
						.build();
				methods.add(getPluginClassName);
			}
		}

		final var specBuild = TypeSpec
				.classBuilder(this.interceptorClass + this.providerClassExtension)
				.addSuperinterface(InterceptorProvider.class)
				.addModifiers(Modifier.PUBLIC);
		for (var method : methods) {
			specBuild.addMethod(method);
		}

		final var spec = specBuild.build();

		return JavaFile.builder(packageName, spec).build();
	}

}

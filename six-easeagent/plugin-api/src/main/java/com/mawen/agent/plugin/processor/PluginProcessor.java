package com.mawen.agent.plugin.processor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;
import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.annotation.AdvicesTo;
import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
@AutoService(Processor.class)
public class PluginProcessor extends AbstractProcessor {

	TreeSet<String> processAnnotations = new TreeSet<>();

	public PluginProcessor() {
		super();
		processAnnotations.add(AdviceTo.class.getCanonicalName());
		processAnnotations.add(AdvicesTo.class.getCanonicalName());
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return processAnnotations;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	private Set<TypeElement> process(Set<Class<? extends Annotation>> annotationClasses,
	                                 Elements elements,
	                                 RoundEnvironment roundEnv) {
		var services = new TreeSet<String>();
		var types = new HashSet<TypeElement>();
		var dstClass = Interceptor.class;

		var roundElements = new HashSet<Element>();
		for (var annotationClass : annotationClasses) {
			var es = roundEnv.getElementsAnnotatedWith(annotationClass);
			roundElements.addAll(es);
		}

		for (var e : roundElements) {
			if (!e.getKind().isClass() || e.getModifiers().contains(Modifier.ABSTRACT)) {
				continue;
			}
			var type = (TypeElement) e;
			types.add(type);
			services.add(elements.getBinaryName(type).toString());
		}
		if (services.isEmpty()) {
			return types;
		}
		writeToMetaInf(dstClass, services);

		return types;
	}

	private void writeToMetaInf(Class<?> dstClass, Set<String> services) {
		var fileName = "META-INF/services/" + dstClass.getCanonicalName();

		if (services.isEmpty()) {
			return;
		}

		var filer = processingEnv.getFiler();
		PrintWriter pw = null;
		try {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Writing " + fileName);
			var f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileName);
			pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), StandardCharsets.UTF_8));
			services.forEach(pw::println);
		}
		catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write generated files: " + e);
		}
		finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		final var utils = BeanUtils.of(processingEnv);
		var elements = processingEnv.getElementUtils();
		var plugins = searchPluginClass(roundEnv.getRootElements(), utils);
		if (plugins == null || plugins.isEmpty()) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Can't find AgentPlugin class!");
			return false;
		}
		var classes = new HashSet<Class<? extends Annotation>>();
		classes.add(AdvicesTo.class);
		classes.add(AdviceTo.class);
		var interceptors = process(classes, elements, roundEnv);
		// generate providerBean
		generateProviderBeans(plugins, interceptors, utils);

		return false;
	}

	LinkedHashMap<String, TypeElement> searchPluginClass(Set<? extends Element> elements, BeanUtils utils) {
		var findInterface = utils.getTypeElement(AgentPlugin.class.getCanonicalName());
		TypeElement found;

		var plugins = new ArrayList<TypeElement>();
		var visitor = new ElementVisitor8(utils);
		for (var e : elements) {
			found = e.accept(visitor, findInterface);
			if (found != null) {
				plugins.add(found);
			}
		}
		var pluginNames = new LinkedHashMap<String, TypeElement>();
		for (var p : plugins) {
			var className = utils.classNameOf(p);
			pluginNames.put(className.canonicalName(), p);
		}
		writeToMetaInf(AgentPlugin.class, pluginNames.keySet());

		return pluginNames;
	}

	private void generateProviderBeans(LinkedHashMap<String, TypeElement> plugins,
	                                   Set<TypeElement> interceptors, BeanUtils utils) {
		var providers = new TreeSet<String>();
		var points = new TreeSet<String>();
		for (var type : interceptors) {
			if (Objects.isNull(type.getAnnotation(AdviceTo.class))
			    && Objects.isNull(type.getAnnotation(AdvicesTo.class))) {
				continue;
			}
			var annotations = type.getAnnotationMirrors();
			var adviceToAnnotations = new HashSet<AnnotationMirror>();
			for (var annotation : annotations) {
				if (utils.isSameType(annotation.getAnnotationType(), AdviceTo.class.getCanonicalName())) {
					adviceToAnnotations.add(annotation);
					continue;
				}
				if (!utils.isSameType(annotation.getAnnotationType(), AdvicesTo.class.getCanonicalName())) {
					continue;
				}
				var values = annotation.getElementValues();
				var visitor = new RepeatedAnnotationVisitor();
				for (var e : values.entrySet()) {
					var key = e.getKey().getSimpleName().toString();
					if (key.equals("value")) {
						var av = e.getValue();
						var as = av.accept(visitor, AdvicesTo.class);
						adviceToAnnotations.addAll(as);
						break;
					}
				}
			}

			int seq = 0;
			var plugin = plugins.values().toArray(new TypeElement[0])[0];
			for (var annotation : adviceToAnnotations) {
				var values = annotation.getElementValues();
				var to = new HashMap<String, String>();
				for (var e : values.entrySet()) {
					var key = e.getKey().getSimpleName().toString();
					var av = e.getValue();
					String value;
					if (av.getValue() == null) {
						value = "default";
					}
					else {
						value = av.getValue().toString();
					}
					to.put(key, value);
					if (key.equals("value")) {
						points.add(value);
					}
					else if (key.equals("plugin") && plugins.get(value) != null) {
						plugin = plugins.get(value);
					}
				}
				to.put("seq", Integer.toString(seq));
				var gb = new GenerateProviderBean(plugin, type, to, utils);
				var file = gb.apply();
				try {
					file.toBuilder().indent("    ")
							.addFileComment("This is a generated file.")
							.build().writeTo(processingEnv.getFiler());
					providers.add(gb.getProviderClass());
					seq += 1;
				}
				catch (IOException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
				}
			}
		}

		writeToMetaInf(Points.class, points);
		writeToMetaInf(InterceptorProvider.class, providers);
	}
}

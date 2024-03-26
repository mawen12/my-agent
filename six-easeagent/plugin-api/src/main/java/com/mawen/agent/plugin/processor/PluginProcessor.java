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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;
import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.annotation.AdvicesTo;
import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;

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
		Set<String> services = new TreeSet<>();
		Set<TypeElement> types = new HashSet<>();
		Class<?> dstClass = Interceptor.class;

		Set<Element> roundElements = new HashSet<>();
		for (Class<? extends Annotation> annotationClass : annotationClasses) {
			Set<? extends Element> es = roundEnv.getElementsAnnotatedWith(annotationClass);
			roundElements.addAll(es);
		}

		for (Element e : roundElements) {
			if (!e.getKind().isClass() || e.getModifiers().contains(Modifier.ABSTRACT)) {
				continue;
			}
			TypeElement type = (TypeElement) e;
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
		String fileName = "META-INF/services/" + dstClass.getCanonicalName();

		if (services.isEmpty()) {
			return;
		}

		Filer filer = processingEnv.getFiler();
		PrintWriter pw = null;
		try {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Writing " + fileName);
			FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileName);
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
		final BeanUtils utils = BeanUtils.of(processingEnv);
		Elements elements = processingEnv.getElementUtils();
		LinkedHashMap<String, TypeElement> plugins = searchPluginClass(roundEnv.getRootElements(), utils);
		if (plugins == null || plugins.isEmpty()) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Can't find AgentPlugin class!");
			return false;
		}
		Set<Class<? extends Annotation>> classes = new HashSet<>();
		classes.add(AdvicesTo.class);
		classes.add(AdviceTo.class);
		Set<TypeElement> interceptors = process(classes, elements, roundEnv);
		// generate providerBean
		generateProviderBeans(plugins, interceptors, utils);

		return false;
	}

	LinkedHashMap<String, TypeElement> searchPluginClass(Set<? extends Element> elements, BeanUtils utils) {
		TypeElement findInterface = utils.getTypeElement(AgentPlugin.class.getCanonicalName());
		TypeElement found;

		List<TypeElement> plugins = new ArrayList<>();
		ElementVisitor8 visitor = new ElementVisitor8(utils);
		for (Element e : elements) {
			found = e.accept(visitor, findInterface);
			if (found != null) {
				plugins.add(found);
			}
		}
		LinkedHashMap<String, TypeElement> pluginNames = new LinkedHashMap<>();
		for (TypeElement p : plugins) {
			ClassName className = utils.classNameOf(p);
			pluginNames.put(className.canonicalName(), p);
		}
		writeToMetaInf(AgentPlugin.class, pluginNames.keySet());

		return pluginNames;
	}

	private void generateProviderBeans(LinkedHashMap<String, TypeElement> plugins,
	                                   Set<TypeElement> interceptors, BeanUtils utils) {
		Set<String> providers = new TreeSet<>();
		TreeSet<String> points = new TreeSet<>();
		for (TypeElement type : interceptors) {
			if (Objects.isNull(type.getAnnotation(AdviceTo.class))
			    && Objects.isNull(type.getAnnotation(AdvicesTo.class))) {
				continue;
			}
			List<? extends AnnotationMirror> annotations = type.getAnnotationMirrors();
			Set<AnnotationMirror> adviceToAnnotations = new HashSet<AnnotationMirror>();
			for (AnnotationMirror annotation : annotations) {
				if (utils.isSameType(annotation.getAnnotationType(), AdviceTo.class.getCanonicalName())) {
					adviceToAnnotations.add(annotation);
					continue;
				}
				if (!utils.isSameType(annotation.getAnnotationType(), AdvicesTo.class.getCanonicalName())) {
					continue;
				}
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
				RepeatedAnnotationVisitor visitor = new RepeatedAnnotationVisitor();
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
					String key = e.getKey().getSimpleName().toString();
					if (key.equals("value")) {
						AnnotationValue av = e.getValue();
						Set<AnnotationMirror> as = av.accept(visitor, AdvicesTo.class);
						adviceToAnnotations.addAll(as);
						break;
					}
				}
			}

			int seq = 0;
			TypeElement plugin = plugins.values().toArray(new TypeElement[0])[0];
			for (AnnotationMirror annotation : adviceToAnnotations) {
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
				Map<String, String> to = new HashMap<>();
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
					String key = e.getKey().getSimpleName().toString();
					AnnotationValue av = e.getValue();
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
				GenerateProviderBean gb = new GenerateProviderBean(plugin, type, to, utils);
				JavaFile file = gb.apply();
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

package com.mawen.agent.core.plugin.transformer.advice.support;

import net.bytebuddy.description.type.TypeDescription;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/7
 */
public class NoExceptionHandler extends Throwable{

	private static final long serialVersionUID = 1L;
	public static final TypeDescription DESCRIPTION = TypeDescription.ForLoadedType.of(NoExceptionHandler.class);

	private NoExceptionHandler() {
		throw new UnsupportedOperationException("This class only serves as a marker type and should not be instantiated");
	}

}

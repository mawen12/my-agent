package com.mawen.agent.config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class ValidateUtils {

	public static class ValidException extends RuntimeException {
		public ValidException(String message) {
			super(message);
		}
	}

	public interface Validator {
		void validate(String name, String value);
	}

	public static void validate(Configs configs,  String name, Validator... vs) {
		String value = configs.getString(name);
		for (var one : vs) {
			one.validate(name,value);
		}
	}

	public static final Validator HasText = ((name, value) -> {
		if (value == null || value.trim().isEmpty()) {
			throw new ValidException(String.format("Property[%s] has no non-empty value", name));
		}
	});

	public static final Validator Bool = ((name, value) -> {
		var upper = value.toUpperCase();
		if (upper.equals("TRUE") || upper.equals("FALSE")) {
			return;
		}
		throw new ValidException(String.format("Property[%s] has no boolean value", name));
	});

	public static final Validator NumberInt = ((name, value) -> {
		try {
			Integer.parseInt(value.trim());
		}
		catch (NumberFormatException e) {
			throw new ValidException(String.format("Property[%s] has no integer value", name));
		}
	});
}

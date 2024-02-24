package com.mawen.agent.plugin.utils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class NoNull {

	/**
	 * Checks that the specified object reference is not {@code null} and
	 * return a default value if it is.
	 *
	 * <p>This method is designed primarily for doing verify the return value in methods that
	 * returns a non-empty instance, as demonstrated below:
	 * <pre>{@code
	 *  public String getFoo() {
	 *      return NoNull.of(this.bar, "default");
	 *  }
	 * }</pre>
	 *
	 * @param o the object reference to check for nullity
	 * @param defaultValue default value to be used in the event
	 * @return {@code o} if not {@code null} else {@code defaultValue}
	 */
	public static <O> O of(O o, O defaultValue) {
		return o == null ? defaultValue : o;
	}
}
